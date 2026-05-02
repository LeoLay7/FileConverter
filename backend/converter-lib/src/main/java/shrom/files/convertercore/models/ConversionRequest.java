package shrom.files.convertercore.models;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import shrom.files.convertercore.converter.ConverterFacade;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Описывает один запрос на конвертацию.
 * Используется как единая точка входа в {@link ConverterFacade#convert(ConversionRequest)}.
 */
@Builder
public record ConversionRequest(
        InputStream source,
        OutputStream target,
        FileFormat sourceFormat,
        FileFormat targetFormat
) { }
