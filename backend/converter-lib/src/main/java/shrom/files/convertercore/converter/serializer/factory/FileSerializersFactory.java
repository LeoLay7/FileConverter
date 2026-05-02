package shrom.files.convertercore.converter.serializer.factory;

import shrom.files.convertercore.converter.serializer.FileSerializer;
import shrom.files.convertercore.models.FileFormat;

public interface FileSerializersFactory {
    FileSerializer getSerializer(FileFormat fileFormat);
}
