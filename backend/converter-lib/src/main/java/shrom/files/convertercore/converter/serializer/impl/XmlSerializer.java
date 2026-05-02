package shrom.files.convertercore.converter.serializer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.serializer.FileSerializer;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class XmlSerializer implements FileSerializer {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public void serialize(JsonNode node, OutputStream outputStream) throws IOException {
        xmlMapper.writeValue(outputStream, node);
    }

    @Override
    public FileFormat getFileFormat() {
        return FileFormat.XML;
    }
}
