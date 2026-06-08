# Test Plan: JSON -> XML и XML -> JSON со всеми типами правил

Документ описывает два демонстрационных тест-кейса для проверки конвертации и применения всех доступных в MVP типов правил:

- `COPY`;
- `RENAME`;
- `DELETE`;
- `TYPE_CAST`;
- `LIST`.

Правило `CONSTANT` не включается в тест-кейсы, так как оно присутствует как задел в enum, но недоступно в MVP.

## Предварительные условия

Должны быть запущены:

- file-service;
- rule-service;
- facade-service;
- tree-converter;
- PostgreSQL;
- MinIO/S3;
- Kafka.

В Postman удобно завести переменные:

```text
fileServiceUrl=http://localhost:<порт file-service>
ruleServiceUrl=http://localhost:<порт rule-service>
facadeServiceUrl=http://localhost:<порт facade-service>
jsonProductFileId=<заполнить после загрузки JSON>
xmlProductFileId=<заполнить после загрузки XML>
jsonToXmlAllRulesRuleSetId=<заполнить после создания RuleSet>
xmlToJsonAllRulesRuleSetId=<заполнить после создания RuleSet>
```

Если сервисы запущены с авторизацией, добавить header:

```text
Authorization: Bearer <accessToken>
```

## Sample-файлы

### Sample 1: JSON-файл для JSON -> XML

Файл уже подготовлен:

```text
docs/samples/product_all_rules.json
```

Содержимое:

```json
{
  "id": "1001",
  "name": "Laptop",
  "category": "electronics",
  "price": "1299.50",
  "available": "true",
  "stock": "42",
  "internalCode": "INT-SECRET-001",
  "owner": {
    "name": "Ivan Petrov",
    "email": "ivan.petrov@example.com",
    "phone": "+79990000000"
  },
  "tags": [
    {
      "id": "1",
      "name": "computers",
      "priority": "10",
      "internalCode": "TAG-SECRET-1"
    },
    {
      "id": "2",
      "name": "technology",
      "priority": "20",
      "internalCode": "TAG-SECRET-2"
    },
    {
      "id": "3",
      "name": "gadgets",
      "priority": "30",
      "internalCode": "TAG-SECRET-3"
    }
  ],
  "categories": [
    "laptops",
    "electronics",
    "computers"
  ]
}
```

Загрузка:

```http
POST {{fileServiceUrl}}/api/files
Content-Type: multipart/form-data
```

Form-data:

```text
file   = product_all_rules.json, type File
format = JSON, type Text
kind   = SOURCE, type Text
```

Сохранить из ответа:

```text
jsonProductFileId=<fileId>
```

### Sample 2: XML-файл для XML -> JSON

Файл уже подготовлен:

```text
docs/samples/product_all_rules.xml
```

Содержимое:

```xml
<product>
    <id>1001</id>
    <name>Laptop</name>
    <category>electronics</category>
    <price>1299.50</price>
    <available>true</available>
    <stock>42</stock>
    <internalCode>INT-SECRET-001</internalCode>
    <owner>
        <name>Ivan Petrov</name>
        <email>ivan.petrov@example.com</email>
        <phone>+79990000000</phone>
    </owner>
    <tags>
        <id>1</id>
        <name>computers</name>
        <priority>10</priority>
        <internalCode>TAG-SECRET-1</internalCode>
    </tags>
    <tags>
        <id>2</id>
        <name>technology</name>
        <priority>20</priority>
        <internalCode>TAG-SECRET-2</internalCode>
    </tags>
    <tags>
        <id>3</id>
        <name>gadgets</name>
        <priority>30</priority>
        <internalCode>TAG-SECRET-3</internalCode>
    </tags>
    <categories>laptops</categories>
    <categories>electronics</categories>
    <categories>computers</categories>
</product>
```

Загрузка:

```http
POST {{fileServiceUrl}}/api/files
Content-Type: multipart/form-data
```

Form-data:

```text
file   = product_all_rules.xml, type File
format = XML, type Text
kind   = SOURCE, type Text
```

Сохранить из ответа:

```text
xmlProductFileId=<fileId>
```

## RuleSet 1: JSON -> XML со всеми типами правил

Назначение RuleSet:

- `COPY`: скопировать `owner.email` в новое поле `ownerEmail`;
- `RENAME`: переименовать корневое поле `name` в `productName`;
- `DELETE`: удалить корневое поле `internalCode`;
- `TYPE_CAST`: привести `price` к `DECIMAL`;
- `TYPE_CAST`: привести `available` к `BOOLEAN`;
- `TYPE_CAST`: привести `stock` к `INTEGER`;
- `LIST + RENAME`: внутри каждого элемента `tags` переименовать `name` в `label`;
- `LIST + TYPE_CAST`: внутри каждого элемента `tags` привести `id` к `INTEGER`;
- `LIST + TYPE_CAST`: внутри каждого элемента `tags` привести `priority` к `INTEGER`;
- `LIST + DELETE`: внутри каждого элемента `tags` удалить `internalCode`.

Создание RuleSet:

```http
POST {{ruleServiceUrl}}/api/rule-sets
Content-Type: application/json
```

Body:

```json
{
  "name": "TEST JSON product all rules to XML",
  "sourceFormat": "JSON",
  "targetFormat": "XML",
  "rules": [
    {
      "order": 1,
      "type": "COPY",
      "sourcePath": "owner.email",
      "targetPath": "ownerEmail",
      "enabled": true
    },
    {
      "order": 2,
      "type": "RENAME",
      "sourcePath": "name",
      "targetPath": "productName",
      "enabled": true
    },
    {
      "order": 3,
      "type": "DELETE",
      "sourcePath": "internalCode",
      "enabled": true
    },
    {
      "order": 4,
      "type": "TYPE_CAST",
      "sourcePath": "price",
      "targetType": "DECIMAL",
      "enabled": true
    },
    {
      "order": 5,
      "type": "TYPE_CAST",
      "sourcePath": "available",
      "targetType": "BOOLEAN",
      "enabled": true
    },
    {
      "order": 6,
      "type": "TYPE_CAST",
      "sourcePath": "stock",
      "targetType": "INTEGER",
      "enabled": true
    },
    {
      "order": 7,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "RENAME",
        "sourcePath": "name",
        "targetPath": "label",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 8,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "TYPE_CAST",
        "sourcePath": "id",
        "targetType": "INTEGER",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 9,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "TYPE_CAST",
        "sourcePath": "priority",
        "targetType": "INTEGER",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 10,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "DELETE",
        "sourcePath": "internalCode",
        "enabled": true
      },
      "enabled": true
    }
  ]
}
```

Сохранить из ответа:

```text
jsonToXmlAllRulesRuleSetId=<ruleSetId>
```

## Запуск теста 1: JSON -> XML

Запрос:

```http
POST {{facadeServiceUrl}}/api/transform
Content-Type: application/json
```

Body:

```json
{
  "sourceFileId": "{{jsonProductFileId}}",
  "targetFormat": "XML",
  "ruleSetId": "{{jsonToXmlAllRulesRuleSetId}}"
}
```

Сохранить из ответа:

```text
jsonToXmlJobId=<jobId>
```

Проверка статуса:

```http
GET {{facadeServiceUrl}}/api/jobs/{{jsonToXmlJobId}}
```

Повторять до:

```text
status=DONE
```

Сохранить:

```text
jsonToXmlResultFileId=<resultFileId>
```

Скачать результат:

```http
GET {{fileServiceUrl}}/api/files/{{jsonToXmlResultFileId}}/content
```

Ожидаемые проверки результата:

- XML валидный;
- есть `<ownerEmail>ivan.petrov@example.com</ownerEmail>`;
- есть `<productName>Laptop</productName>`;
- нет корневого `<name>Laptop</name>`;
- нет `<internalCode>INT-SECRET-001</internalCode>`;
- есть `<price>1299.50</price>` или `<price>1299.5</price>`;
- есть `<available>true</available>`;
- есть `<stock>42</stock>`;
- внутри каждого `tags` больше нет `name`;
- внутри каждого `tags` есть `label`;
- внутри каждого `tags` больше нет `internalCode`;
- `categories` сериализованы как повторяющиеся XML-элементы:

```xml
<categories>laptops</categories>
<categories>electronics</categories>
<categories>computers</categories>
```

Важно по `categories`:

Такой результат корректен. Массив примитивов в XML представляется повторяющимися одноименными элементами.

## RuleSet 2: XML -> JSON со всеми типами правил

Назначение RuleSet такое же, как в тесте 1, но исходный файл имеет формат XML, а результат должен быть JSON.

Создание RuleSet:

```http
POST {{ruleServiceUrl}}/api/rule-sets
Content-Type: application/json
```

Body:

```json
{
  "name": "TEST XML product all rules to JSON",
  "sourceFormat": "XML",
  "targetFormat": "JSON",
  "rules": [
    {
      "order": 1,
      "type": "COPY",
      "sourcePath": "owner.email",
      "targetPath": "ownerEmail",
      "enabled": true
    },
    {
      "order": 2,
      "type": "RENAME",
      "sourcePath": "name",
      "targetPath": "productName",
      "enabled": true
    },
    {
      "order": 3,
      "type": "DELETE",
      "sourcePath": "internalCode",
      "enabled": true
    },
    {
      "order": 4,
      "type": "TYPE_CAST",
      "sourcePath": "price",
      "targetType": "DECIMAL",
      "enabled": true
    },
    {
      "order": 5,
      "type": "TYPE_CAST",
      "sourcePath": "available",
      "targetType": "BOOLEAN",
      "enabled": true
    },
    {
      "order": 6,
      "type": "TYPE_CAST",
      "sourcePath": "stock",
      "targetType": "INTEGER",
      "enabled": true
    },
    {
      "order": 7,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "RENAME",
        "sourcePath": "name",
        "targetPath": "label",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 8,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "TYPE_CAST",
        "sourcePath": "id",
        "targetType": "INTEGER",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 9,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "TYPE_CAST",
        "sourcePath": "priority",
        "targetType": "INTEGER",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 10,
      "type": "LIST",
      "sourcePath": "tags",
      "itemRule": {
        "order": 1,
        "type": "DELETE",
        "sourcePath": "internalCode",
        "enabled": true
      },
      "enabled": true
    }
  ]
}
```

Сохранить из ответа:

```text
xmlToJsonAllRulesRuleSetId=<ruleSetId>
```

## Запуск теста 2: XML -> JSON

Запрос:

```http
POST {{facadeServiceUrl}}/api/transform
Content-Type: application/json
```

Body:

```json
{
  "sourceFileId": "{{xmlProductFileId}}",
  "targetFormat": "JSON",
  "ruleSetId": "{{xmlToJsonAllRulesRuleSetId}}"
}
```

Сохранить из ответа:

```text
xmlToJsonJobId=<jobId>
```

Проверка статуса:

```http
GET {{facadeServiceUrl}}/api/jobs/{{xmlToJsonJobId}}
```

Повторять до:

```text
status=DONE
```

Сохранить:

```text
xmlToJsonResultFileId=<resultFileId>
```

Скачать результат:

```http
GET {{fileServiceUrl}}/api/files/{{xmlToJsonResultFileId}}/content
```

Ожидаемый JSON должен содержать:

```json
{
  "id": "1001",
  "category": "electronics",
  "price": 1299.5,
  "available": true,
  "stock": 42,
  "owner": {
    "name": "Ivan Petrov",
    "email": "ivan.petrov@example.com",
    "phone": "+79990000000"
  },
  "tags": [
    {
      "id": 1,
      "priority": 10,
      "label": "computers"
    },
    {
      "id": 2,
      "priority": 20,
      "label": "technology"
    },
    {
      "id": 3,
      "priority": 30,
      "label": "gadgets"
    }
  ],
  "categories": [
    "laptops",
    "electronics",
    "computers"
  ],
  "ownerEmail": "ivan.petrov@example.com",
  "productName": "Laptop"
}
```

Допустимые отличия:

- порядок полей в JSON может отличаться;
- decimal может быть сериализован как `1299.5` вместо `1299.50`.

Обязательные проверки:

- `productName = Laptop`;
- корневого поля `name` нет;
- `ownerEmail = ivan.petrov@example.com`;
- `internalCode` удален на корневом уровне;
- `price` является числом;
- `available` является boolean;
- `stock` является числом;
- `tags` является массивом из трех объектов;
- в каждом элементе `tags` есть `label`;
- в каждом элементе `tags` нет `name`;
- в каждом элементе `tags` нет `internalCode`;
- `categories` является массивом из трех строк.

## Итоговое покрытие правил

| Тип правила | Где проверяется |
|---|---|
| `COPY` | `owner.email -> ownerEmail` |
| `RENAME` | `name -> productName`, `tags[].name -> tags[].label` |
| `DELETE` | `internalCode`, `tags[].internalCode` |
| `TYPE_CAST` | `price -> DECIMAL`, `available -> BOOLEAN`, `stock -> INTEGER`, `tags[].id -> INTEGER`, `tags[].priority -> INTEGER` |
| `LIST` | применение правил к каждому элементу массива `tags` |

## Что смотреть в логах tree-converter

При успешном прохождении должны быть видны этапы:

```text
TreeTransform: start jobId=...
TreeTransform: source metadata loaded ...
TreeTransform: source file downloaded ...
ConverterFacade: parse start format=...
ConverterFacade: parse finished ...
TreeTransform: applying rules ...
RuleEngine: start applying ...
RuleEngine: before rule ...
RuleEngine: LIST item index=0 before itemRule ...
RuleEngine: LIST item index=0 after itemRule ...
RuleEngine: finished applying rules ...
ConverterFacade: serialize start format=...
TreeTransform: result uploaded ...
TreeTransform: job completed successfully ...
```

Если появится `FAILED`, сначала нужно смотреть:

- какой `sourcePath` указан в ошибке;
- какой `tree=` был после парсинга;
- какие поля реально присутствуют в объекте перед правилом;
- какой delimiter был определен для CSV, если тестируется CSV.

