package shrom.files.convertercore.converter.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.parser.FileParser;
import shrom.files.convertercore.models.FileFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class XmlParser implements FileParser {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public JsonNode parse(InputStream inputStream) throws IOException {
        return xmlMapper.readTree(inputStream);
    }

    @Override
    public FileFormat getFileFormat() {
        return FileFormat.XML;
    }
}
