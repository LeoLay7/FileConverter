package shrom.files.convertercore.converter.parser.factory;

import shrom.files.convertercore.converter.parser.FileParser;
import shrom.files.convertercore.models.FileFormat;

public interface FileParsersFactory {
    FileParser getParser(FileFormat fileFormat);
}
