# Контракты, модели и форматы

## 1. Форматы файлов

Поддерживаемые значения:

- `JSON`
- `XML`
- `CSV`

Определение формата:

- по ручному выбору пользователя;
- по расширению файла;
- по content type;
- fallback: попытка парсинга, если хватает времени.

## 2. Schema tree

DTO:

```json
{
  "fileId": "uuid",
  "format": "CSV",
  "root": {
    "name": "root",
    "path": "root",
    "type": "array",
    "sampleValue": null,
    "children": [
      {
        "name": "name",
        "path": "root.name",
        "type": "string",
        "sampleValue": "Ivan",
        "children": []
      }
    ]
  }
}
```

Типы узлов:

- `object`
- `array`
- `string`
- `integer`
- `decimal`
- `boolean`
- `null`
- `unknown`

## 3. RuleSet

```json
{
  "name": "csv-users-to-json",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "rules": [
    {
      "order": 10,
      "type": "COPY",
      "sourcePath": "root.name",
      "targetPath": "root.fullName"
    },
    {
      "order": 20,
      "type": "TYPE_CAST",
      "sourcePath": "root.age",
      "targetType": "INTEGER"
    },
    {
      "order": 30,
      "type": "DELETE",
      "sourcePath": "root.active"
    }
  ]
}
```

## 4. Rule

Поля:

- `order: integer` - порядок выполнения;
- `type: string` - тип операции;
- `sourcePath: string` - путь источника;
- `targetPath: string | null` - путь результата;
- `targetType: string | null` - целевой тип;
- `value: any | null` - значение для constant;
- `enabled: boolean` - опционально, по умолчанию true.

Типы операций MVP:

- `COPY`
- `RENAME`
- `DELETE`
- `TYPE_CAST`
- `CONSTANT`, если останется время.

Целевые типы:

- `STRING`
- `INTEGER`
- `DECIMAL`
- `BOOLEAN`

## 5. Path conventions

Внешний формат пути - dot notation:

- `root.name`
- `root.items.price`
- `root.customer.address.city`

Для массивов объектов в MVP правило применяется к каждому объекту массива, если сегмент пути соответствует полю элемента.

Пример:

Исходный CSV после парсинга:

```json
[
  { "name": "Ivan", "age": "21" },
  { "name": "Anna", "age": "22" }
]
```

В UI показывается как:

- `root.name`
- `root.age`

Правило `TYPE_CAST root.age INTEGER` применяется ко всем строкам.

## 6. API

### Upload file

`POST /api/files`

Request: `multipart/form-data`

Fields:

- `file`
- `format`, optional

Response:

```json
{
  "fileId": "4f191e2b-6a3f-4e5d-9d03-2b74c25b65b6",
  "originalName": "users.csv",
  "format": "CSV",
  "size": 1024
}
```

### Get tree

`GET /api/files/{fileId}/tree`

Response:

```json
{
  "fileId": "uuid",
  "format": "CSV",
  "root": {}
}
```

### Transform

`POST /api/transform`

Request:

```json
{
  "sourceFileId": "uuid",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "ruleSet": {
    "rules": []
  }
}
```

Response:

```json
{
  "jobId": "uuid",
  "status": "DONE",
  "resultFileId": "uuid",
  "downloadUrl": "/api/files/{resultFileId}/download"
}
```

### Job status

`GET /api/jobs/{jobId}`

Response:

```json
{
  "jobId": "uuid",
  "status": "DONE",
  "resultFileId": "uuid",
  "error": null
}
```

### Download

`GET /api/files/{fileId}/download`

Response: binary attachment.

## 7. ErrorResponse

```json
{
  "code": "RULE_PATH_NOT_FOUND",
  "message": "Path root.age was not found",
  "details": {
    "path": "root.age",
    "ruleOrder": 20
  }
}
```

Коды:

- `UNSUPPORTED_FORMAT`
- `INVALID_FILE`
- `FILE_NOT_FOUND`
- `RULE_VALIDATION_ERROR`
- `RULE_PATH_NOT_FOUND`
- `TYPE_CAST_ERROR`
- `TRANSFORM_FAILED`

## 8. Job model

```json
{
  "jobId": "uuid",
  "sourceFileId": "uuid",
  "resultFileId": "uuid",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "status": "DONE",
  "createdAt": "2026-06-01T12:00:00Z",
  "finishedAt": "2026-06-01T12:00:03Z",
  "error": null
}
```

## 9. Kafka events для будущей версии

`TransformRequested`

```json
{
  "jobId": "uuid",
  "sourceFileId": "uuid",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "ruleSetId": "uuid"
}
```

`TransformCompleted`

```json
{
  "jobId": "uuid",
  "status": "DONE",
  "resultFileId": "uuid",
  "error": null
}
```

