package shrom.files.convertercore.converter.transformation.factory;

import shrom.files.convertercore.converter.transformation.Converter;
import shrom.files.convertercore.models.FileFormat;

public interface ConverterFactory {
    Converter getConverter(FileFormat from, FileFormat to);
}
