package org.codejive.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

public class TestProperties {

    @Test
    void testLoad() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p, aMapWithSize(7));
        assertThat(
                p.keySet(),
                contains("one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.rawKeySet(),
                contains(
                        "one", "two", "three", "\\ with\\ spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.values(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\u1234"));
        assertThat(
                p.rawValues(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "\\u1234"));
    }

    @Test
    void testStore() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(f)));
    }

    @Test
    void testStoreHeader() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw, "A header line");
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-storeheader.properties"))));
    }

    @Test
    void testGet() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.get("one"), equalTo("simple"));
        assertThat(p.get("two"), equalTo("value containing spaces"));
        assertThat(p.get("three"), equalTo("and escapes\n\t\r\f"));
        assertThat(p.get(" with spaces"), equalTo("everywhere  "));
        assertThat(p.get("altsep"), equalTo("value"));
        assertThat(p.get("multiline"), equalTo("one two  three"));
        assertThat(p.get("key.4"), equalTo("\u1234"));
    }

    @Test
    void testGetProperty() throws IOException, URISyntaxException {
        Properties pdef = Properties.loadProperties(getResource("/test.properties"));
        Properties p = new Properties(pdef);
        p.setProperty("two", "a different two");
        p.setProperty("altsep", "");
        p.setProperty("five", "5", "a new comment");
        assertThat(p, aMapWithSize(3));
        assertThat(p.keySet(), contains("two", "altsep", "five"));
        assertThat(p.stringPropertyNames(), hasSize(8));
        assertThat(
                p.stringPropertyNames(),
                contains(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "key.4",
                        "five"));
        assertThat(p.getProperty("one"), equalTo("simple"));
        assertThat(p.getPropertyComment("one"), contains("! comment3"));
        assertThat(p.getProperty("two"), equalTo("a different two"));
        assertThat(p.getPropertyComment("two"), empty());
        assertThat(p.getProperty("three"), equalTo("and escapes\n\t\r\f"));
        assertThat(
                p.getPropertyComment("three"),
                contains("# another comment", "! and a comment", "! block"));
        assertThat(p.getProperty(" with spaces"), equalTo("everywhere  "));
        assertThat(p.getProperty("altsep"), equalTo(""));
        assertThat(p.getProperty("multiline"), equalTo("one two  three"));
        assertThat(p.getProperty("key.4"), equalTo("\u1234"));
        assertThat(p.getProperty("five"), equalTo("5"));
        assertThat(p.getPropertyComment("five"), contains("# a new comment"));
        StringWriter sw = new StringWriter();
        p.list(new PrintWriter(sw));
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-getproperty.properties"))));
    }

    @Test
    void testGetRaw() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getRaw("one"), equalTo("simple"));
        assertThat(p.getRaw("two"), equalTo("value containing spaces"));
        assertThat(p.getRaw("three"), equalTo("and escapes\\n\\t\\r\\f"));
        assertThat(p.getRaw(" with spaces"), equalTo("everywhere  "));
        assertThat(p.getRaw("altsep"), equalTo("value"));
        assertThat(p.getRaw("multiline"), equalTo("one \\\n    two  \\\n\tthree"));
        assertThat(p.getRaw("key.4"), equalTo("\\u1234"));
    }

    @Test
    void testGetComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getComment("one"), contains("! comment3"));
        assertThat(p.getComment("two"), empty());
        assertThat(
                p.getComment("three"), contains("# another comment", "! and a comment", "! block"));
    }

    @Test
    void testSetComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.setComment("one", "new single comment");
        p.setComment("two", "new multi", "line", "comment");
        p.setComment("three", Collections.emptyList());
        assertThat(p.getComment("one"), contains("! new single comment"));
        assertThat(p.getComment("two"), contains("# new multi", "# line", "# comment"));
        assertThat(p.getComment("three"), empty());
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-comment.properties"))));
    }

    @Test
    void testPut() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put(" with spaces", "everywhere  ");
        p.put("altsep", "value");
        p.put("multiline", "one two  three");
        p.put("key.4", "\u1234");
        assertThat(p, aMapWithSize(7));
        assertThat(
                p.keySet(),
                contains("one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.rawKeySet(),
                contains(
                        "one", "two", "three", "\\ with\\ spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.values(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\u1234"));
        assertThat(
                p.rawValues(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\\u1234"));
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-put.properties"))));
    }

    @Test
    void testSetProperty() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.setProperty("one", "simple", "! comment3");
        p.setProperty("two", "value containing spaces");
        p.setProperty(
                "three", "and escapes\n\t\r\f", "# another comment", "! and a comment", "! block");
        p.setProperty(" with spaces", "everywhere  ");
        p.setProperty("altsep", "value");
        p.setProperty("multiline", "one two  three");
        p.setProperty("key.4", "\u1234");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-setproperty.properties"))));
    }

    @Test
    void testPutRaw() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.putRaw("one", "simple");
        p.putRaw("two", "value containing spaces");
        p.putRaw("three", "and escapes\\n\\t\\r\\f");
        p.putRaw("\\ with\\ spaces", "everywhere  ");
        p.putRaw("altsep", "value");
        p.putRaw("multiline", "one \\\n    two  \\\n\tthree");
        p.putRaw("key.4", "\\u1234");
        assertThat(p, aMapWithSize(7));
        assertThat(
                p.keySet(),
                contains("one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.rawKeySet(),
                contains(
                        "one", "two", "three", "\\ with\\ spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.values(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\u1234"));
        assertThat(
                p.rawValues(),
                contains(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "\\u1234"));
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-putraw.properties"))));
    }

    @Test
    void testPutReplaceFirst() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("one", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(
                sw.toString(), equalTo(readAll(getResource("/test-putreplacefirst.properties"))));
    }

    @Test
    void testPutReplaceMiddle() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("two", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(
                sw.toString(), equalTo(readAll(getResource("/test-putreplacemiddle.properties"))));
    }

    @Test
    void testPutReplaceLast() throws IOException, URISyntaxException {
        Properties p = new Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("three", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-putreplacelast.properties"))));
    }

    @Test
    void testPutNew() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-putnew.properties"))));
    }

    @Test
    void testPutFirstWithHeader() throws IOException, URISyntaxException {
        try (StringReader sr = new StringReader("# A header comment")) {
            Properties p = Properties.loadProperties(sr);
            p.put("first", "dummy");
            StringWriter sw = new StringWriter();
            p.store(sw);
            assertThat(
                    sw.toString(),
                    equalTo(readAll(getResource("/test-putfirstwithheader.properties"))));
        }
    }

    @Test
    void testRemoveFirst() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("one");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removefirst.properties"))));
    }

    @Test
    void testRemoveMiddle() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("three");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removemiddle.properties"))));
    }

    @Test
    void testRemoveLast() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("key.4");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removelast.properties"))));
    }

    @Test
    void testRemoveAll() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("one");
        p.remove("two");
        p.remove("three");
        p.remove(" with spaces");
        p.remove("altsep");
        p.remove("multiline");
        p.remove("key.4");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removeall.properties"))));
    }

    @Test
    void testRemoveMiddleIterator() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        Iterator iter = p.keySet().iterator();
        while (iter.hasNext()) {
            if (iter.next().equals("three")) {
                iter.remove();
                break;
            }
        }
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removemiddle.properties"))));
    }

    @Test
    void testClear() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.clear();
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-clear.properties"))));
    }

    @Test
    void testRemoveComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.setComment("one");
        assertThat(p.getComment("one"), empty());
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(readAll(getResource("/test-removecomment.properties"))));
    }

    @Test
    public void testInteropLoad() throws IOException, URISyntaxException {
        java.util.Properties p = new java.util.Properties();
        try (Reader br = Files.newBufferedReader(getResource("/test.properties"))) {
            p.load(br);
        }
        assertThat(p, aMapWithSize(7));
        assertThat(
                p.keySet(),
                containsInAnyOrder(
                        "one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p.values(),
                containsInAnyOrder(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\u1234"));
    }

    @Test
    void testInteropStore() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        StringWriter sw = new StringWriter();
        p.asJUProperties().store(sw, null);
        assertThat(sw.toString(), containsString("one=simple\n"));
        assertThat(sw.toString(), containsString("two=value containing spaces\n"));
        assertThat(sw.toString(), containsString("three=and escapes\\n\\t\\r\\f\n"));
        assertThat(sw.toString(), containsString("\\ with\\ spaces=everywhere  \n"));
        assertThat(sw.toString(), containsString("altsep=value\n"));
        assertThat(sw.toString(), containsString("multiline=one two  three\n"));
        assertThat(sw.toString(), containsString("key.4=\u1234\n"));
    }

    @Test
    void testInteropPutLoad() throws IOException, URISyntaxException {
        java.util.Properties p = new java.util.Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put(" with spaces", "everywhere  ");
        p.put("altsep", "value");
        p.put("multiline", "one two  three");
        p.put("key.4", "\u1234");
        StringWriter sw = new StringWriter();
        p.store(sw, null);
        assertThat(sw.toString(), containsString("one=simple\n"));
        assertThat(sw.toString(), containsString("two=value containing spaces\n"));
        assertThat(sw.toString(), containsString("three=and escapes\\n\\t\\r\\f\n"));
        assertThat(sw.toString(), containsString("\\ with\\ spaces=everywhere  \n"));
        assertThat(sw.toString(), containsString("altsep=value\n"));
        assertThat(sw.toString(), containsString("multiline=one two  three\n"));
        assertThat(sw.toString(), containsString("key.4=\u1234\n"));
        java.util.Properties p2 = new java.util.Properties();
        p2.load(new StringReader(sw.toString()));
        assertThat(p2, aMapWithSize(7));
        assertThat(
                p2.keySet(),
                containsInAnyOrder(
                        "one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(
                p2.values(),
                containsInAnyOrder(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "\u1234"));
    }

    private Path getResource(String name) throws URISyntaxException {
        return Paths.get(getClass().getResource(name).toURI());
    }

    private String readAll(Path f) throws IOException {
        return new String(Files.readAllBytes(f));
    }
}
