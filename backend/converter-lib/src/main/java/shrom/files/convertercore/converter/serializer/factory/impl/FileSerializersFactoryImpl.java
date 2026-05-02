package shrom.files.convertercore.converter.serializer.factory.impl;

import org.springframework.stereotype.Component;
import shrom.files.convertercore.converter.serializer.FileSerializer;
import shrom.files.convertercore.converter.serializer.factory.FileSerializersFactory;
import shrom.files.convertercore.models.FileFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileSerializersFactoryImpl implements FileSerializersFactory {

    private final Map<FileFormat, FileSerializer> serializers;

    public FileSerializersFactoryImpl(List<FileSerializer> serializerList) {
        serializers = new HashMap<>();
        for (FileSerializer serializer : serializerList) {
            serializers.put(serializer.getFileFormat(), serializer);
        }
    }

    @Override
    public FileSerializer getSerializer(FileFormat fileFormat) {
        return serializers.get(fileFormat);
    }
}
