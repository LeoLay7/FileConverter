package shrom.files.convertercore.converter.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shrom.files.convertercore.converter.impl.ConverterFacadeImpl;
import shrom.files.convertercore.converter.parser.factory.impl.FileParsersFactoryImpl;
import shrom.files.convertercore.converter.parser.impl.CsvParser;
import shrom.files.convertercore.converter.parser.impl.JsonParser;
import shrom.files.convertercore.converter.parser.impl.XmlParser;
import shrom.files.convertercore.converter.serializer.factory.impl.FileSerializersFactoryImpl;
import shrom.files.convertercore.converter.serializer.impl.CsvSerializer;
import shrom.files.convertercore.converter.serializer.impl.JsonSerializer;
import shrom.files.convertercore.converter.serializer.impl.XmlSerializer;
import shrom.files.convertercore.converter.transformation.factory.impl.ConverterFactoryImpl;
import shrom.files.convertercore.converter.transformation.impl.*;
import shrom.files.convertercore.models.ConversionRequest;
import shrom.files.convertercore.models.FileFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConverterFacadeTest {

    private ConverterFacadeImpl facade;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        facade = new ConverterFacadeImpl(
                new ConverterFactoryImpl(List.of(
                        new JsonToXmlConverter(),
                        new JsonToCsvConverter(),
                        new XmlToJsonConverter(),
                        new XmlToCsvConverter(),
                        new CsvToJsonConverter(),
                        new CsvToXmlConverter()
                )),
                new FileParsersFactoryImpl(List.of(
                        new JsonParser(),
                        new XmlParser(),
                        new CsvParser()
                )),
                new FileSerializersFactoryImpl(List.of(
                        new JsonSerializer(),
                        new XmlSerializer(),
                        new CsvSerializer()
                ))
        );
    }

    private InputStream resource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    // ── parse ─────────────────────────────────────────────────────────────────

    @Test
    void parse_jsonReturnsCorrectNode() throws IOException {
        JsonNode node = facade.parse(resource("json/sample1.json"), FileFormat.JSON);

        assertEquals("0001", node.get("id").asText());
        assertEquals("Cake", node.get("name").asText());
    }

    @Test
    void parse_xmlReturnsPlantArray() throws IOException {
        JsonNode node = facade.parse(resource("xml/sample1.xml"), FileFormat.XML);

        assertTrue(node.get("PLANT").isArray());
        assertEquals(36, node.get("PLANT").size());
    }

    @Test
    void parse_csvReturnsArrayNode() throws IOException {
        JsonNode node = facade.parse(resource("csv/sample1.csv"), FileFormat.CSV);

        assertTrue(node.isArray());
        assertEquals(5, node.size());
    }

    // ── serialize ─────────────────────────────────────────────────────────────

    @Test
    void serialize_jsonNodeToJson() throws IOException {
        JsonNode node = facade.parse(resource("json/sample1.json"), FileFormat.JSON);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.serialize(node, out, FileFormat.JSON);

        JsonNode result = jsonMapper.readTree(out.toByteArray());
        assertEquals("0001", result.get("id").asText());
    }

    @Test
    void serialize_jsonNodeToXml() throws IOException {
        JsonNode node = facade.parse(resource("json/sample1.json"), FileFormat.JSON);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.serialize(node, out, FileFormat.XML);

        String xml = out.toString();
        assertTrue(xml.contains("0001"));
        assertTrue(xml.contains("Cake"));
    }

    @Test
    void serialize_csvNodeToCsv() throws IOException {
        JsonNode node = facade.parse(resource("csv/sample1.csv"), FileFormat.CSV);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.serialize(node, out, FileFormat.CSV);

        String csv = out.toString();
        assertTrue(csv.contains("Login email"));
        assertTrue(csv.contains("rachel@example.com"));
    }

    // ── convert via ConversionRequest ─────────────────────────────────────────

    @Test
    void convert_xmlToJsonViaRequest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.convert(ConversionRequest.builder()
                .source(resource("xml/sample1.xml"))
                .target(out)
                .sourceFormat(FileFormat.XML)
                .targetFormat(FileFormat.JSON)
                .build());

        JsonNode result = jsonMapper.readTree(out.toByteArray());
        assertEquals("Bloodroot", result.get("PLANT").get(0).get("COMMON").asText());
    }

    @Test
    void convert_jsonToXmlViaRequest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.convert(ConversionRequest.builder()
                .source(resource("json/sample1.json"))
                .target(out)
                .sourceFormat(FileFormat.JSON)
                .targetFormat(FileFormat.XML)
                .build());

        String xml = out.toString();
        assertTrue(xml.contains("0001"));
        assertTrue(xml.contains("donut"));
    }

    @Test
    void convert_sameFormatThrows() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThrows(IllegalArgumentException.class, () ->
                facade.convert(ConversionRequest.builder()
                        .source(resource("json/sample1.json"))
                        .target(out)
                        .sourceFormat(FileFormat.JSON)
                        .targetFormat(FileFormat.JSON)
                        .build()));
    }

    // ── convert via stream overload ───────────────────────────────────────────

    @Test
    void convert_csvToJsonViaStreams() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        facade.convert(resource("csv/sample1.csv"), out, FileFormat.CSV, FileFormat.JSON);

        JsonNode result = jsonMapper.readTree(out.toByteArray());
        assertTrue(result.isArray());
        assertEquals(5, result.size());
    }
}
