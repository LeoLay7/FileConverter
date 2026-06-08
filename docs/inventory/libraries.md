# Библиотеки и общие модули

## 1. `tree-converter-lib`

### Статус

Уже есть в репозитории как `tree-converter-lib`. Реализует парсинг и сериализацию JSON, XML, CSV через внутреннюю модель `JsonNode`.

### Назначение

Переиспользуемое ядро для всех сервисов, которым нужно читать/писать форматы данных.

### Используется в MVP

- MVP backend / Facade;
- Tree Transformer;
- тесты преобразования.

### Что должно быть публичным API

- `ConverterFacade.parse(InputStream, FileFormat): JsonNode`
- `ConverterFacade.serialize(JsonNode, OutputStream, FileFormat)`
- `ConverterFacade.convert(InputStream, OutputStream, FileFormat, FileFormat)`
- `FileFormat`
- `ConversionRequest`

### Что доработать перед MVP

- проверить, что artifactId и package names единообразны (`converter-core` vs `tree-converter-lib`);
- добавить стабильную обработку `same source/target format`, потому что для rule engine нужно уметь JSON->JSON после изменения дерева;
- проверить CSV delimiter: для демо лучше поддержать `,` и `;`;
- для XML определить root element результата;
- отключенные тесты CSV->XML/XML->CSV либо починить, либо явно вынести из обязательного демо.

## 2. `rule-engine-lib`

### Назначение

Общая библиотека применения правил к `JsonNode`. Ее можно создать внутри backend как пакет, но логически лучше держать отдельно, потому что в целевой архитектуре она нужна Tree Transformer и тестам.

### Основные классы

- `RuleEngine`
- `RuleSet`
- `Rule`
- `RuleType`
- `TargetType`
- `PathResolver`
- `TypeCaster`
- `RuleValidationException`
- `RuleExecutionException`

### Функции

- валидация набора правил;
- последовательное применение правил по `order`;
- чтение значения по path;
- запись значения по path;
- удаление поля;
- переименование;
- приведение типов;
- применение к массивам объектов.

### Минимальные операции

```json
{
  "rules": [
    {
      "order": 10,
      "type": "COPY",
      "sourcePath": "name",
      "targetPath": "fullName"
    },
    {
      "order": 20,
      "type": "TYPE_CAST",
      "sourcePath": "age",
      "targetType": "INTEGER"
    },
    {
      "order": 30,
      "type": "DELETE",
      "sourcePath": "active"
    }
  ]
}
```

### Критерии готовности

- не мутирует исходный `JsonNode` без явного решения;
- возвращает новый `JsonNode`;
- имеет unit-тесты на каждую операцию;
- ошибки содержат `rule.order`, `rule.type`, `path`, `message`.

## 3. `schema-tree-lib`

### Назначение

Построение дерева полей для UI из `JsonNode`.

### Основные классы

- `SchemaTreeBuilder`
- `SchemaNode`
- `SchemaNodeType`
- `SampleValueFormatter`

### DTO узла

```json
{
  "name": "age",
  "path": "users.age",
  "type": "string",
  "sampleValue": "42",
  "children": []
}
```

### Особенности массивов

Для массива объектов строить объединенную схему по первым N элементам, например N=20. Для MVP можно брать первый элемент, но это надо явно указать в UI/README.

### Критерии готовности

- JSON object отображается как дерево;
- CSV array of objects отображается как root -> columns;
- XML после Jackson отображается без падений;
- массивы не раздувают дерево на сотни повторяющихся элементов.

## 4. `api-contracts`

### Назначение

Общие DTO для backend, UI-клиента и будущих сервисов.

### Что вынести

- `FileFormatDto`
- `FileUploadResponse`
- `TransformRequest`
- `TransformResponse`
- `JobStatusResponse`
- `ErrorResponse`
- `RuleSetDto`
- `RuleDto`
- `SchemaNodeDto`

### MVP-подход

Если нет времени на отдельный Maven-модуль, держать DTO в backend, но документировать их в `contracts-and-data.md` и OpenAPI.

## 5. `file-storage-lib`

### Назначение

Абстракция хранения файлов, чтобы локальное MVP-хранилище потом заменить на MinIO.

### Интерфейс

```java
public interface FileStoragePort {
    StoredFile save(String originalName, FileFormat format, InputStream content);
    InputStream open(UUID fileId);
    StoredFile metadata(UUID fileId);
}
```

### Реализации

- MVP: `LocalFileStorageAdapter`;
- target: `S3FileStorageAdapter`.

### Критерии готовности

- файл сохраняется с безопасным именем;
- original filename не используется как storage path;
- download возвращает корректный content type и filename.

## 6. `job-core`

### Назначение

Единые модели задач конвертации.

### Модели

- `Job`
- `JobStatus`
- `TransformCommand`
- `TransformResult`
- `JobRepositoryPort`

### MVP

In-memory map.

### Target

PostgreSQL + Kafka events.

## 7. Что не выносить в библиотеки сейчас

- UI-компоненты, пока интерфейс один.
- MinIO/Kafka adapters, пока они не используются.
- JavaScript custom rules, пока нет песочницы исполнения.
- Авторизацию, пока User Service отложен.

