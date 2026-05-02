package shrom.files.convertercore.converter.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.OutputStream;

public interface FileSerializer {
    void serialize(JsonNode node, OutputStream outputStream) throws IOException;
    FileFormat getFileFormat();
}
