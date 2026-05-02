package shrom.files.convertercore.converter.transformation.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.transformation.Converter;
import shrom.files.convertercore.converter.transformation.factory.ConverterFactory;
import shrom.files.convertercore.converter.transformation.impl.*;
import shrom.files.convertercore.models.FileFormat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConverterFactoryImpl implements ConverterFactory {
    private final Map<FileFormat, Map<FileFormat, Converter>> convertersMap;

    public ConverterFactoryImpl(List<Converter> converters) {
        convertersMap = new HashMap<>();
        for (Converter converter : converters) {
            FileFormat from = converter.getFromFileFormat();
            FileFormat to = converter.getToFileFormat();

            if (!convertersMap.containsKey(from)) {
                convertersMap.put(from, new HashMap<>());
            }
            convertersMap.get(from).put(to, converter);
        }
    }

    @Override
    public Converter getConverter(FileFormat from, FileFormat to) {
        if (from == to) {
            throw new IllegalArgumentException("Source and target formats must differ");
        }
        return convertersMap.get(from).get(to);
    }
}
