package shrom.files.convertercore.converter.parser.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.parser.FileParser;
import shrom.files.convertercore.converter.parser.factory.FileParsersFactory;
import shrom.files.convertercore.converter.parser.impl.CsvParser;
import shrom.files.convertercore.converter.parser.impl.JsonParser;
import shrom.files.convertercore.converter.parser.impl.XmlParser;
import shrom.files.convertercore.models.FileFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileParsersFactoryImpl implements FileParsersFactory {
    private final Map<FileFormat, FileParser> fileParsers;

    public FileParsersFactoryImpl(List<FileParser> parsers) {
        fileParsers = new HashMap<>();
        for (FileParser parser : parsers) {
            fileParsers.put(parser.getFileFormat(), parser);
        }
    }

    @Override
    public FileParser getParser(FileFormat fileFormat) {
        return fileParsers.get(fileFormat);
    }
}
