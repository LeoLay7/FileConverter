package shrom.files.convertercore.converter.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import shrom.files.convertercore.converter.parser.impl.CsvParser;
import shrom.files.convertercore.converter.parser.impl.JsonParser;
import shrom.files.convertercore.converter.parser.impl.XmlParser;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FileParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static void logNode(String label, JsonNode node) throws Exception {
        log.info("[{}] parsed JsonNode:\n{}", label, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node));
    }

    private InputStream resource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    // ── JSON ──────────────────────────────────────────────────────────────────

    @Test
    void jsonParser_parsesTopLevelFields() throws Exception {
        JsonNode node = new JsonParser().parse(resource("json/sample1.json"));
        logNode("jsonParser_parsesTopLevelFields", node);

        assertAll(
                () -> assertEquals("0001", node.get("id").asText()),
                () -> assertEquals("donut", node.get("type").asText()),
                () -> assertEquals("Cake", node.get("name").asText()),
                () -> assertEquals(0.55, node.get("ppu").asDouble(), 0.001),
                () -> assertTrue(node.get("available").asBoolean())
        );
    }

    @Test
    void jsonParser_parsesNestedArray() throws Exception {
        JsonNode node = new JsonParser().parse(resource("json/sample1.json"));
        logNode("jsonParser_parsesNestedArray", node);

        JsonNode batters = node.path("batters").path("batter");
        assertAll(
                () -> assertTrue(batters.isArray()),
                () -> assertEquals(2, batters.size()),
                () -> assertEquals("Regular", batters.get(0).get("type").asText()),
                () -> assertEquals("Chocolate", batters.get(1).get("type").asText())
        );
    }

    @Test
    void jsonParser_throwsOnXmlInput() {
        assertThrows(IOException.class, () ->
                new JsonParser().parse(resource("xml/sample1.xml")));
    }

    // ── XML ───────────────────────────────────────────────────────────────────

    @Test
    void xmlParser_parsesPlantListSize() throws Exception {
        JsonNode node = new XmlParser().parse(resource("xml/sample1.xml"));
        logNode("xmlParser_parsesPlantListSize", node);

        JsonNode plants = node.get("PLANT");
        assertTrue(plants.isArray());
        assertEquals(36, plants.size());
    }

    @Test
    void xmlParser_parsesFirstPlantFields() throws Exception {
        JsonNode node = new XmlParser().parse(resource("xml/sample1.xml"));
        logNode("xmlParser_parsesFirstPlantFields", node);

        JsonNode first = node.get("PLANT").get(0);
        assertAll(
                () -> assertEquals("Bloodroot", first.get("COMMON").asText()),
                () -> assertEquals("Sanguinaria canadensis", first.get("BOTANICAL").asText()),
                () -> assertEquals("4", first.get("ZONE").asText()),
                () -> assertEquals("$2.44", first.get("PRICE").asText())
        );
    }

    @Test
    void xmlParser_parsesLastPlantFields() throws Exception {
        JsonNode node = new XmlParser().parse(resource("xml/sample1.xml"));
        logNode("xmlParser_parsesLastPlantFields", node);

        JsonNode last = node.get("PLANT").get(35);
        assertAll(
                () -> assertEquals("Cardinal Flower", last.get("COMMON").asText()),
                () -> assertEquals("Lobelia cardinalis", last.get("BOTANICAL").asText())
        );
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    @Test
    void csvParser_returnsArrayOfCorrectSize() throws Exception {
        JsonNode node = new CsvParser(';').parse(resource("csv/sample1.csv"));
        logNode("csvParser_returnsArrayOfCorrectSize", node);

        assertTrue(node.isArray());
        assertEquals(5, node.size());
    }

    @Test
    void csvParser_parsesFirstRowFields() throws Exception {
        JsonNode node = new CsvParser(';').parse(resource("csv/sample1.csv"));
        logNode("csvParser_parsesFirstRowFields", node);
        JsonNode first = node.get(0);

        assertAll(
                () -> assertEquals("rachel@example.com", first.get("Login email").asText()),
                () -> assertEquals("9012", first.get("Identifier").asText()),
                () -> assertEquals("Rachel", first.get("First name").asText()),
                () -> assertEquals("Booker", first.get("Last name").asText()),
                () -> assertEquals("Sales", first.get("Department").asText()),
                () -> assertEquals("Manchester", first.get("Location").asText())
        );
    }

    @Test
    void csvParser_parsesLastRow() throws Exception {
        JsonNode node = new CsvParser(';').parse(resource("csv/sample1.csv"));
        logNode("csvParser_parsesLastRow", node);
        JsonNode last = node.get(4);

        assertAll(
                () -> assertEquals("jamie@example.com", last.get("Login email").asText()),
                () -> assertEquals("Engineering", last.get("Department").asText())
        );
    }
}
