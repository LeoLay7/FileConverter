package shrom.files.convertercore.converter.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import shrom.files.convertercore.converter.transformation.impl.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    private InputStream resource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    private JsonNode toJson(byte[] bytes) throws IOException {
        return new ObjectMapper().readTree(bytes);
    }

    private JsonNode toXml(byte[] bytes) throws IOException {
        return new XmlMapper().readTree(bytes);
    }

    private String asString(byte[] bytes) {
        return new String(bytes);
    }

    // ── JSON → XML ────────────────────────────────────────────────────────────

    @Test
    void jsonToXml_outputIsValidXml() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JsonToXmlConverter().convert(resource("json/sample1.json"), out);

        JsonNode node = toXml(out.toByteArray());
        assertAll(
                () -> assertEquals("0001", node.get("id").asText()),
                () -> assertEquals("Cake", node.get("name").asText()),
                () -> assertEquals("donut", node.get("type").asText())
        );
    }

    // ── XML → JSON ────────────────────────────────────────────────────────────

    @Test
    void xmlToJson_outputIsValidJson() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new XmlToJsonConverter().convert(resource("xml/sample1.xml"), out);

        JsonNode node = toJson(out.toByteArray());
        JsonNode plants = node.get("PLANT");
        assertAll(
                () -> assertTrue(plants.isArray()),
                () -> assertEquals(36, plants.size()),
                () -> assertEquals("Bloodroot", plants.get(0).get("COMMON").asText())
        );
    }

    // ── JSON → CSV ────────────────────────────────────────────────────────────

    @Test
    void jsonToCsv_outputContainsHeaders() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JsonToCsvConverter().convert(resource("json/sample1.json"), out);

        String csv = asString(out.toByteArray());
        // JSON-объект — одна строка данных, заголовки из полей верхнего уровня
        assertTrue(csv.contains("id"));
        assertTrue(csv.contains("type"));
        assertTrue(csv.contains("name"));
    }

    @Test
    void jsonToCsv_outputContainsValues() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JsonToCsvConverter().convert(resource("json/sample1.json"), out);

        String csv = asString(out.toByteArray());
        assertTrue(csv.contains("0001"));
        assertTrue(csv.contains("donut"));
        assertTrue(csv.contains("Cake"));
    }

    // ── CSV → JSON ────────────────────────────────────────────────────────────

    @Test
    void csvToJson_outputIsArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvToJsonConverter(';').convert(resource("csv/sample1.csv"), out);

        JsonNode node = toJson(out.toByteArray());
        assertTrue(node.isArray());
        assertEquals(5, node.size());
    }

    @Test
    void csvToJson_firstRowValues() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvToJsonConverter(';').convert(resource("csv/sample1.csv"), out);

        JsonNode first = toJson(out.toByteArray()).get(0);
        assertAll(
                () -> assertEquals("rachel@example.com", first.get("Login email").asText()),
                () -> assertEquals("Rachel", first.get("First name").asText()),
                () -> assertEquals("Manchester", first.get("Location").asText())
        );
    }

    // ── CSV → XML ────────────────────────────────────────────────────────────

    @Test
    @Disabled
    void csvToXml_outputIsValidXml() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvToXmlConverter(';').convert(resource("csv/sample1.csv"), out);

        JsonNode node = toXml(out.toByteArray());
        assertNotNull(node);
        assertFalse(node.isEmpty());
    }

    @Test
    @Disabled
    void csvToXml_containsFirstRowData() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvToXmlConverter(';').convert(resource("csv/sample1.csv"), out);

        String xml = asString(out.toByteArray());
        assertTrue(xml.contains("rachel@example.com"));
        assertTrue(xml.contains("Manchester"));
    }

    // ── XML → CSV ────────────────────────────────────────────────────────────

    @Test
    @Disabled
    void xmlToCsv_outputContainsHeaders() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new XmlToCsvConverter().convert(resource("xml/sample1.xml"), out);

        String csv = asString(out.toByteArray());
        System.out.println("Содержимое: " + csv);
        assertTrue(csv.contains("COMMON"));
        assertTrue(csv.contains("BOTANICAL"));
        assertTrue(csv.contains("PRICE"));
    }

    @Test
    @Disabled
    void xmlToCsv_outputContainsPlantData() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new XmlToCsvConverter().convert(resource("xml/sample1.xml"), out);

        String csv = asString(out.toByteArray());
        System.out.println("Содержимое: " + csv);
        assertTrue(csv.contains("Bloodroot"));
        assertTrue(csv.contains("Cardinal Flower"));
    }
}
