package shrom.files.convertercore.converter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.ConverterFacade;
import shrom.files.convertercore.converter.parser.factory.FileParsersFactory;
import shrom.files.convertercore.converter.serializer.factory.FileSerializersFactory;
import shrom.files.convertercore.converter.transformation.factory.ConverterFactory;
import shrom.files.convertercore.models.ConversionRequest;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
@RequiredArgsConstructor
public class ConverterFacadeImpl implements ConverterFacade {
    private final ConverterFactory converterFactory;
    private final FileParsersFactory fileParsersFactory;
    private final FileSerializersFactory fileSerializersFactory;

    @Override
    public void convert(ConversionRequest request) throws IOException {
        converterFactory.getConverter(request.sourceFormat(), request.targetFormat()).convert(
                request.source(),
                request.target()
        );
    }

    @Override
    public void convert(InputStream source, OutputStream target, FileFormat from, FileFormat to) throws IOException {
        converterFactory.getConverter(from, to).convert(
                source,
                target
        );
    }

    @Override
    public JsonNode parse(InputStream source, FileFormat format) throws IOException {
        return fileParsersFactory.getParser(format).parse(source);
    }

    @Override
    public void serialize(JsonNode uim, OutputStream target, FileFormat format) throws IOException {
        fileSerializersFactory.getSerializer(format).serialize(uim, target);
    }
}
