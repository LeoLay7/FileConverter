package shrom.files.convertercore.converter.transformation;

import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Converter {
    void convert(InputStream inputStream, OutputStream outputStream) throws IOException;

    FileFormat getFromFileFormat();
    FileFormat getToFileFormat();
}
