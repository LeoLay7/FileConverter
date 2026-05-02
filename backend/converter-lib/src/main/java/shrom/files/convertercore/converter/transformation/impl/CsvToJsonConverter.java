package shrom.files.convertercore.converter.transformation.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.transformation.Converter;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvToJsonConverter implements Converter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final char delimiter;

    public CsvToJsonConverter() {
        this(',');
    }

    public CsvToJsonConverter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
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

        mapper.writeValue(outputStream, result);
    }

    @Override
    public FileFormat getFromFileFormat() {
        return FileFormat.CSV;
    }

    @Override
    public FileFormat getToFileFormat() {
        return FileFormat.JSON;
    }
}
