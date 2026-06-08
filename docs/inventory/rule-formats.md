# Форматы правил для frontend

Документ описывает актуальный JSON-формат правил, который используется в `rule-service`, `facade-service` и `tree-converter`.

## Где получать справочники

Rule Service предоставляет API для построения UI-форм:

- `GET /api/rule-capabilities` - все возможности правил одним ответом.
- `GET /api/rule-capabilities/rule-types` - список типов правил и требования к полям.
- `GET /api/rule-capabilities/target-types` - список целевых типов для `TYPE_CAST`.

Swagger Rule Service:

```text
http://localhost:8083/swagger-ui/index.html
```

## RuleSet

RuleSet хранит метаданные набора правил и список правил.

```json
{
  "ruleSetId": "6a3721c7-8780-4bde-9dd9-0c8fc182de7b",
  "name": "CSV users to JSON",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "rules": [],
  "createdAt": "2026-06-03T10:00:00Z",
  "updatedAt": "2026-06-03T10:00:00Z"
}
```

При создании или обновлении через `POST /api/rule-sets` и `PUT /api/rule-sets/{ruleSetId}` используется `RuleSetSaveRequestDto`:

```json
{
  "name": "CSV users to JSON",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "rules": []
}
```

Поддерживаемые форматы файлов:

- `JSON`
- `XML`
- `CSV`

## Rule

Базовая структура одного правила:

```json
{
  "order": 1,
  "type": "COPY",
  "sourcePath": "name",
  "targetPath": "fullName",
  "targetType": null,
  "value": null,
  "itemRule": null,
  "enabled": true,
  "options": {}
}
```

Поля:

- `order` - порядок выполнения правила. Меньшее значение выполняется раньше.
- `type` - тип правила.
- `sourcePath` - путь к исходному полю.
- `targetPath` - путь, куда записать значение.
- `targetType` - целевой тип для `TYPE_CAST`.
- `value` - зарезервировано для будущего `CONSTANT`, в MVP не используется.
- `itemRule` - вложенное правило для `LIST`.
- `enabled` - если `false`, правило пропускается. Если `null`, в `tree-converter` считается как `true`.
- `options` - зарезервировано для будущих настроек, в MVP не используется.

## Path notation

Пути задаются через dot notation:

```text
name
user.name
items
items.price
root.name
.
```

Особенности:

- `root` в начале пути игнорируется. `root.name` и `name` эквивалентны.
- `.` означает текущий объект или текущий элемент списка.
- Для полей внутри массива нужно использовать правило `LIST`.
- Если CSV парсится в массив строк-объектов, правило к полям CSV обычно оформляется через `LIST` с `sourcePath: "."`.

## Типы правил

### COPY

Копирует значение из `sourcePath` в `targetPath`. Исходное поле остается на месте.

Требования к полям:

| Поле | Требование |
|---|---|
| `sourcePath` | required |
| `targetPath` | required |
| `targetType` | forbidden |
| `value` | forbidden |
| `itemRule` | forbidden |

Пример:

```json
{
  "order": 1,
  "type": "COPY",
  "sourcePath": "name",
  "targetPath": "fullName",
  "enabled": true
}
```

### RENAME

Переносит значение из `sourcePath` в `targetPath`. После переноса исходное поле удаляется.

Требования к полям:

| Поле | Требование |
|---|---|
| `sourcePath` | required |
| `targetPath` | required |
| `targetType` | forbidden |
| `value` | forbidden |
| `itemRule` | forbidden |

Пример:

```json
{
  "order": 1,
  "type": "RENAME",
  "sourcePath": "name",
  "targetPath": "fullName",
  "enabled": true
}
```

### DELETE

Удаляет поле по `sourcePath`.

Требования к полям:

| Поле | Требование |
|---|---|
| `sourcePath` | required |
| `targetPath` | forbidden |
| `targetType` | forbidden |
| `value` | forbidden |
| `itemRule` | forbidden |

Пример:

```json
{
  "order": 1,
  "type": "DELETE",
  "sourcePath": "password",
  "enabled": true
}
```

### TYPE_CAST

Приводит значение по `sourcePath` к типу `targetType`.

Требования к полям:

| Поле | Требование |
|---|---|
| `sourcePath` | required |
| `targetPath` | forbidden |
| `targetType` | required |
| `value` | forbidden |
| `itemRule` | forbidden |

Поддерживаемые `targetType`:

- `STRING`
- `INTEGER`
- `DECIMAL`
- `BOOLEAN`

Пример:

```json
{
  "order": 1,
  "type": "TYPE_CAST",
  "sourcePath": "age",
  "targetType": "INTEGER",
  "enabled": true
}
```

Для boolean поддерживаются значения:

- `true`, `false`
- `1`, `0`
- `yes`, `no`
- `y`, `n`

### LIST

Применяет вложенное `itemRule` к каждому элементу массива.

Требования к полям:

| Поле | Требование |
|---|---|
| `sourcePath` | required |
| `targetPath` | forbidden |
| `targetType` | forbidden |
| `value` | forbidden |
| `itemRule` | required |

Пример для массива объектов:

```json
{
  "order": 1,
  "type": "LIST",
  "sourcePath": "items",
  "itemRule": {
    "order": 1,
    "type": "RENAME",
    "sourcePath": "name",
    "targetPath": "title",
    "enabled": true
  },
  "enabled": true
}
```

Пример для CSV, который после парсинга становится корневым массивом:

```json
{
  "order": 1,
  "type": "LIST",
  "sourcePath": ".",
  "itemRule": {
    "order": 1,
    "type": "RENAME",
    "sourcePath": "name",
    "targetPath": "fullName",
    "enabled": true
  },
  "enabled": true
}
```

Пример для списка примитивов:

```json
{
  "order": 1,
  "type": "LIST",
  "sourcePath": "prices",
  "itemRule": {
    "order": 1,
    "type": "TYPE_CAST",
    "sourcePath": ".",
    "targetType": "DECIMAL",
    "enabled": true
  },
  "enabled": true
}
```

### CONSTANT

`CONSTANT` есть в enum `RuleTypeDto`, но в MVP недоступен.

Rule Service возвращает его в capabilities с `available: false`.

Сохранять RuleSet с `CONSTANT` сейчас нельзя. Валидация вернет ошибку:

```text
CONSTANT rule is not available in MVP
```

Задел под будущий формат:

```json
{
  "order": 1,
  "type": "CONSTANT",
  "targetPath": "status",
  "value": "ACTIVE",
  "enabled": true
}
```

## Полная таблица требований к полям

| type | available | sourcePath | targetPath | targetType | value | itemRule |
|---|---:|---|---|---|---|---|
| `COPY` | yes | required | required | forbidden | forbidden | forbidden |
| `RENAME` | yes | required | required | forbidden | forbidden | forbidden |
| `DELETE` | yes | required | forbidden | forbidden | forbidden | forbidden |
| `TYPE_CAST` | yes | required | forbidden | required | forbidden | forbidden |
| `LIST` | yes | required | forbidden | forbidden | forbidden | required |
| `CONSTANT` | no | forbidden | required | optional | required | forbidden |

## Пример полного RuleSet для CSV -> JSON

```json
{
  "name": "CSV users to JSON",
  "sourceFormat": "CSV",
  "targetFormat": "JSON",
  "rules": [
    {
      "order": 1,
      "type": "LIST",
      "sourcePath": ".",
      "itemRule": {
        "order": 1,
        "type": "RENAME",
        "sourcePath": "name",
        "targetPath": "fullName",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 2,
      "type": "LIST",
      "sourcePath": ".",
      "itemRule": {
        "order": 1,
        "type": "TYPE_CAST",
        "sourcePath": "age",
        "targetType": "INTEGER",
        "enabled": true
      },
      "enabled": true
    },
    {
      "order": 3,
      "type": "LIST",
      "sourcePath": ".",
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

## Важные ограничения MVP

- Вложенное правило внутри `LIST` сейчас одно, поле `itemRule`, не массив.
- Для нескольких операций над одним списком нужно создать несколько `LIST` правил с разным `order`.
- `COPY`, `RENAME`, `DELETE`, `TYPE_CAST` не применяются напрямую к корневому массиву. Для корневого массива используйте `LIST` с `sourcePath: "."`.
- `options` пока не используется.
- `value` пока не используется.
- `CONSTANT` пока недоступен.
- Индексы массивов в path, например `items.0.name`, не поддерживаются как отдельная стабильная возможность MVP.

## Ошибки валидации

Типовые ошибки Rule Service:

```text
Rule set name must not be blank
Source format must not be null
Target format must not be null
rules[0].type must not be null
rules[0].sourcePath must not be blank
rules[0].targetPath must not be blank
rules[0].targetType must not be null
rules[0].itemRule must not be null
CONSTANT rule is not available in MVP
```

HTTP-код для ошибок валидации:

```text
400 Bad Request
```

Код ошибки:

```text
RULE_VALIDATION_ERROR
```
