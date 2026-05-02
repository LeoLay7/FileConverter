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
public class JsonToXmlConverter implements Converter {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        JsonNode node = jsonMapper.readTree(inputStream);
        xmlMapper.writeValue(outputStream, node);
    }

    @Override
    public FileFormat getFromFileFormat() {
        return FileFormat.JSON;
    }

    @Override
    public FileFormat getToFileFormat() {
        return FileFormat.XML;
    }
}
