package shrom.files.convertercore.converter.transformation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.transformation.Converter;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonToCsvConverter implements Converter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        JsonNode root = mapper.readTree(inputStream);

        // Поддерживаем как массив объектов, так и одиночный объект
        List<JsonNode> rows = new ArrayList<>();
        if (root.isArray()) {
            root.forEach(rows::add);
        } else if (root.isObject()) {
            rows.add(root);
        } else {
            throw new IOException("JSON root must be an object or array of objects for CSV conversion");
        }

        if (rows.isEmpty()) return;

        List<String> headers = new ArrayList<>();
        rows.get(0).fieldNames().forEachRemaining(headers::add);

        try (CsvWriter writer = CsvWriter.builder()
                .build(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            writer.writeRecord(headers.toArray(String[]::new));

            for (JsonNode row : rows) {
                String[] values = headers.stream()
                        .map(h -> {
                            JsonNode val = row.get(h);
                            return val == null || val.isNull() ? "" : val.asText();
                        })
                        .toArray(String[]::new);
                writer.writeRecord(values);
            }
        }
    }

    @Override
    public FileFormat getFromFileFormat() {
        return FileFormat.JSON;
    }

    @Override
    public FileFormat getToFileFormat() {
        return FileFormat.CSV;
    }
}
