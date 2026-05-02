package shrom.files.convertercore.converter;

import com.fasterxml.jackson.databind.JsonNode;
import shrom.files.convertercore.models.ConversionRequest;
import shrom.files.convertercore.models.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Главный интерфейс библиотеки converter-lib.
 *
 * <p>Поддерживает две стратегии конвертации:
 * <ul>
 *   <li><b>Стратегия А</b> — прямой streaming-pipeline без промежуточного дерева (без правил).</li>
 *   <li><b>Стратегия Б</b> — конвертация через UIM (JsonNode) с применением правил трансформации.</li>
 * </ul>
 *
 * Остальные методы — удобные перегрузки для типовых сценариев.
 */
public interface ConverterFacade {

    void convert(ConversionRequest request) throws IOException;

    void convert(InputStream source, OutputStream target,
                 FileFormat from, FileFormat to) throws IOException;

    /**
     * Парсит входной поток в универсальную внутреннюю модель (UIM).
     * Используется, когда сервис хочет работать с деревом напрямую (валидация, обогащение).
     */
    JsonNode parse(InputStream source, FileFormat format) throws IOException;

    /**
     * Сериализует UIM в целевой формат.
     */
    void serialize(JsonNode uim, OutputStream target, FileFormat format) throws IOException;
}
