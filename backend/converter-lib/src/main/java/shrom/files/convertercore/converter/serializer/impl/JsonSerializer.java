package shrom.files.convertercore.converter.serializer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.serializer.FileSerializer;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JsonSerializer implements FileSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void serialize(JsonNode node, OutputStream outputStream) throws IOException {
        mapper.writeValue(outputStream, node);
    }

    @Override
    public FileFormat getFileFormat() {
        return FileFormat.JSON;
    }
}
