package shrom.files.convertercore.converter.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.parser.FileParser;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvParser implements FileParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final char delimiter;

    public CsvParser() {
        this(',');
    }

    public CsvParser(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public JsonNode parse(InputStream inputStream) throws IOException {
        ArrayNode result = mapper.createArrayNode();

        try (CsvReader<CsvRecord> reader = CsvReader.builder()
                .fieldSeparator(delimiter)
                .ofCsvRecord(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<String> headers = null;

            for (CsvRecord record : reader) {
                if (headers == null) {
                    headers = record.getFields();
                    continue;
                }
                ObjectNode row = mapper.createObjectNode();
                List<String> fields = record.getFields();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < fields.size() ? fields.get(i) : "");
                }
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public FileFormat getFileFormat() {
        return FileFormat.CSV;
    }
}
