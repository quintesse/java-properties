package org.codejive.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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