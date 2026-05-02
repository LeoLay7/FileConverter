package shrom.files.convertercore.converter.transformation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.transformation.Converter;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class XmlToJsonConverter implements Converter {

    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        JsonNode node = xmlMapper.readTree(inputStream);
        jsonMapper.writeValue(outputStream, node);
    }

    @Override
    public FileFormat getFromFileFormat() {
        return FileFormat.XML;
    }

    @Override
    public FileFormat getToFileFormat() {
        return FileFormat.JSON;
    }
}
