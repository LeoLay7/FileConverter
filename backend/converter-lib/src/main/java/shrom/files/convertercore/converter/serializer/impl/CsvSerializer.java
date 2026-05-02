package shrom.files.convertercore.converter.serializer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.serializer.FileSerializer;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvSerializer implements FileSerializer {

    @Override
    public void serialize(JsonNode node, OutputStream outputStream) throws IOException {
        List<JsonNode> rows = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(rows::add);
        } else if (node.isObject()) {
            rows.add(node);
        } else {
            throw new IOException("JsonNode must be an object or array of objects for CSV serialization");
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
    public FileFormat getFileFormat() {
        return FileFormat.CSV;
    }
}
