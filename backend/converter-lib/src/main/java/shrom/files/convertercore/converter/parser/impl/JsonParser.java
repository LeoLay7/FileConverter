package shrom.files.convertercore.converter.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.parser.FileParser;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonParser implements FileParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode parse(InputStream inputStream) throws IOException {
        return mapper.readTree(inputStream);
    }

    @Override
    public FileFormat getFileFormat() {
        return FileFormat.JSON;
    }
}
