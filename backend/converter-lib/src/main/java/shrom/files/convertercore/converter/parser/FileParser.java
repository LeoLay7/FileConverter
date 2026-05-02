package shrom.files.convertercore.converter.parser;

import com.fasterxml.jackson.databind.JsonNode;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;

public interface FileParser {
    JsonNode parse(InputStream inputStream) throws IOException;

    FileFormat getFileFormat();
}
