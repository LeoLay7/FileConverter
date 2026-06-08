# Валидация `tree-converter-lib`

Дата проверки: 2026-06-01.

## Итог

`tree-converter-lib` пригодна как основа MVP для сценария:

`parse file -> JsonNode -> build schema tree -> apply rules -> serialize result`

Библиотеку можно подключать к MVP backend, но с ограничениями по CSV-разделителю, same-format конвертации и парам `CSV->XML` / `XML->CSV`.

## Проверка сборки и тестов

Команда:

```powershell
.\mvnw.cmd test
```

Результат:

- build: success;
- tests run: 29;
- failures: 0;
- errors: 0;
- skipped: 4.

Skipped-тесты находятся в `ConverterTest`:

- `csvToXml_outputIsValidXml`;
- `csvToXml_containsFirstRowData`;
- `xmlToCsv_outputContainsHeaders`;
- `xmlToCsv_outputContainsPlantData`.

Вывод: основная часть библиотеки стабильна, но прямые конвертеры `CSV->XML` и `XML->CSV` нельзя считать подтвержденными тестами.

## Публичный API

Основной интерфейс: `ConverterFacade`.

Доступные методы:

```java
void convert(ConversionRequest request) throws IOException;

void convert(InputStream source, OutputStream target,
             FileFormat from, FileFormat to) throws IOException;

JsonNode parse(InputStream source, FileFormat format) throws IOException;

void serialize(JsonNode uim, OutputStream target, FileFormat format) throws IOException;
```

Для MVP наиболее важны:

- `parse(...)` - загрузить файл в UIM на базе Jackson `JsonNode`;
- `serialize(...)` - записать измененный `JsonNode` в целевой формат.

Именно эти методы нужны для rule engine. Прямой `convert(...)` полезен для конвертации без правил, но не является главным путем MVP.

## Подтвержденные сценарии

### JSON

Подтверждено тестами:

- JSON парсится в `JsonNode`;
- поля верхнего уровня читаются корректно;
- вложенные массивы читаются корректно;
- JSON сериализуется обратно в JSON;
- JSON сериализуется в XML;
- JSON конвертируется в CSV для объекта или массива объектов.

Рекомендация для MVP:

- JSON можно использовать как основной формат для демонстрации вложенных структур.

### XML

Подтверждено тестами:

- XML парсится в `JsonNode`;
- повторяющиеся XML-элементы становятся массивом;
- XML конвертируется в JSON;
- XML сериализация из `JsonNode` работает на простом JSON-дереве.

Ограничения:

- XML root/result naming не настроены явно;
- XML->CSV тесты есть, но отключены;
- сложные XML-атрибуты/namespace не проверены.

Рекомендация для MVP:

- использовать XML->JSON и XML->tree;
- не делать XML->CSV главным демо-сценарием.

### CSV

Подтверждено тестами:

- CSV с разделителем `;` корректно парсится при явном создании `new CsvParser(';')`;
- CSV представляется как `ArrayNode`, где каждая строка - `ObjectNode`;
- CSV->JSON работает при явном создании `new CsvToJsonConverter(';')`;
- CSV сериализуется из массива объектов или одиночного объекта.

Ограничения:

- Spring-компонент `CsvParser()` по умолчанию использует разделитель `,`;
- Spring-компонент `CsvToJsonConverter()` по умолчанию использует разделитель `,`;
- тестовый `sample1.csv` использует `;`;
- `ConverterFacadeTest.parse_csvReturnsArrayNode` проверяет только размер массива, поэтому не ловит ошибку структуры при неверном разделителе;
- CSV serializer берет заголовки только из первого объекта;
- вложенные объекты и массивы при CSV-сериализации превращаются через `asText()`, то есть для сложных структур CSV будет неполным.

Рекомендация для MVP:

- для демо CSV использовать файлы с `,` как разделителем;
- либо до backend добавить настройку delimiter в библиотеку;
- CSV показывать на плоских таблицах.

## Важные технические наблюдения

## 1. Same-format прямой convert запрещен

`ConverterFactoryImpl.getConverter(from, to)` выбрасывает `IllegalArgumentException`, если `from == to`.

Для MVP это нормально, если rule engine идет через:

`parse(sourceFormat) -> apply rules -> serialize(targetFormat)`

Так можно сделать JSON->JSON, CSV->CSV, XML->XML после применения правил без использования прямого `convert(...)`.

## 2. Фабрики возвращают null для неизвестного формата

`FileParsersFactoryImpl.getParser(...)`, `FileSerializersFactoryImpl.getSerializer(...)` и `ConverterFactoryImpl.getConverter(...)` не проверяют отсутствие реализации явно.

Риск:

- при неверной регистрации бина можно получить `NullPointerException`.

Для MVP:

- оборачивать вызовы фасада в сервисном слое и возвращать понятную ошибку `UNSUPPORTED_FORMAT`;
- позже добавить явные исключения в фабрики.

## 3. CSV delimiter нужно стабилизировать

Сейчас delimiter задается через конструктор `CsvParser(char delimiter)` и `CsvToJsonConverter(char delimiter)`, но auto-configuration создаст default bean с `,`.

Для MVP есть два варианта:

1. Быстрый: использовать CSV-файлы с запятой.
2. Лучше: добавить property `converter.csv.delimiter` и создавать `CsvParser`/CSV-конвертеры через конфигурацию.

Рекомендуемый быстрый путь для 3 дней: использовать `,` в demo-data, а поддержку `;` внести как P1.

## 4. XML/CSV пары не готовы как главный сценарий

Наличие классов `CsvToXmlConverter` и `XmlToCsvConverter` не равно готовности. Тесты по ним отключены.

Для MVP:

- основной сценарий: `CSV->JSON`;
- дополнительный: `JSON->CSV`;
- дополнительный: `XML->JSON`;
- избегать обещания стабильного `XML->CSV` и `CSV->XML` на защите MVP.

## Решение по готовности

Статус: условно готова к подключению в MVP backend.

Можно использовать сразу:

- `ConverterFacade.parse`;
- `ConverterFacade.serialize`;
- JSON parsing/serialization;
- XML parsing/tree;
- CSV parsing для `,` delimiter;
- CSV serialization плоских объектов.

Перед или во время разработки backend желательно:

- добавить demo CSV с запятой;
- не использовать прямой `convert(...)` для rule-based сценариев;
- в backend валидировать формат и возвращать понятные ошибки;
- добавить отдельный тест на `parse CSV через ConverterFacade` с реальным delimiter из demo-data.

## Рекомендация для следующего шага

Следующий пункт разработки: `schema-tree-lib`.

Причина: `tree-converter-lib` уже дает `JsonNode`; теперь нужно построить стабильный DTO дерева для UI:

- `name`;
- `path`;
- `type`;
- `sampleValue`;
- `children`;
- схлопывание массивов объектов в единую схему.

