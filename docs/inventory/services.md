# ТЗ по сервисам

Этот документ описывает целевые сервисы из архитектуры и MVP-реализацию на 3 дня. Если сервис не нужен физически в MVP, его контракт все равно фиксируется, чтобы не потерять архитектурную линию.

## 1. Web UI

### Назначение

Пользовательский интерфейс для загрузки файла, просмотра дерева, настройки правил и скачивания результата.

### MVP-функции

- загрузка файла;
- выбор исходного формата, если автоопределение не сработало;
- выбор целевого формата;
- отображение дерева полей;
- добавление правила из формы;
- отображение текущего JSON rule-set;
- запуск конвертации;
- скачивание результата;
- показ ошибок backend.

### Экран MVP

- Панель файла: input file, source format, target format, upload.
- Дерево: раскрываемые узлы, имя, тип, путь.
- Редактор правил: тип операции, sourcePath, targetPath, targetType.
- Действия: add rule, convert, download.
- Превью: textarea/result panel.

### Ограничения MVP

- Без авторизации.
- Без полноценного drag-and-drop.
- Без визуального конструктора целевой схемы.

### API-зависимости

- `POST /api/files`
- `GET /api/files/{fileId}/tree`
- `POST /api/transform`
- `GET /api/jobs/{jobId}`
- `GET /api/files/{fileId}/download`

### Критерии готовности

- пользователь может выполнить CSV->JSON и JSON->CSV без Postman;
- ошибки правил видны в интерфейсе;
- результат скачивается с корректным расширением.

## 2. API Gateway

### Назначение

Единая точка входа HTTP, маршрутизация запросов к сервисам, CORS, ограничения размера файлов.

### MVP-реализация

Физически можно не поднимать. Его роль выполняет Spring Boot backend с включенным CORS для UI.

### Целевые обязанности

- маршрутизация `/api/files/**` в File Service;
- маршрутизация `/api/rules/**` в Rule Service;
- маршрутизация `/api/transform/**`, `/api/jobs/**` в API Facade;
- TLS/HTTPS на периметре;
- лимит размера upload;
- rate limiting после MVP.

### Критерии готовности для MVP

- CORS настроен;
- все API доступны по единному base URL;
- максимальный размер файла явно ограничен настройкой.

## 3. File Service

### Назначение

Прием, хранение и выдача исходных и результирующих файлов.

### MVP-функции

- принять multipart-файл;
- сохранить файл в локальное хранилище;
- вернуть `fileId`, имя, размер, формат;
- отдать поток файла по `fileId`;
- сохранить результат конвертации;
- отдать результат как attachment.

### Целевая реализация

- MinIO/S3;
- bucket для исходных файлов;
- bucket для результатов;
- presigned URLs;
- очистка временных файлов по TTL.

### Модель файла

- `fileId: UUID`
- `originalName`
- `contentType`
- `format: JSON | XML | CSV`
- `size`
- `storageKey`
- `createdAt`
- `kind: SOURCE | RESULT`

### API MVP

`POST /api/files`

Request: `multipart/form-data`

Response:

```json
{
  "fileId": "uuid",
  "originalName": "users.csv",
  "format": "CSV",
  "size": 1234
}
```

`GET /api/files/{fileId}/download`

Response: binary attachment.

### Ошибки

- unsupported format;
- file too large;
- empty file;
- file not found;
- storage unavailable.

## 4. Rule Service

### Назначение

Управление правилами и наборами правил конвертации.

### MVP-функции

- принимать rule-set прямо в запросе конвертации;
- валидировать правила;
- опционально сохранять rule-set in-memory по `ruleSetId`;
- отдавать примеры rule-set для демо.

### Целевые функции

- CRUD rule-set;
- версии rule-set;
- публикация/черновик;
- привязка rule-set к пользователю;
- PostgreSQL;
- кэширование популярных наборов в Redis.

### Модель rule-set

- `id`
- `name`
- `sourceFormat`
- `targetFormat`
- `rules[]`
- `createdAt`
- `updatedAt`

### API MVP

Можно не выделять отдельный контроллер. Достаточно включить `rules` в `POST /api/transform`.

Если хватает времени:

- `POST /api/rule-sets`
- `GET /api/rule-sets/{id}`
- `GET /api/rule-sets/examples`

### Критерии готовности

- неверный тип правила не проходит валидацию;
- отсутствующий `sourcePath` дает понятную ошибку;
- порядок правил сохраняется.

## 5. API Facade

### Назначение

Оркестрация пользовательского сценария: принять команду конвертации, создать задачу, вернуть статус и результат.

### MVP-функции

- принять `TransformRequest`;
- создать `jobId`;
- синхронно вызвать `TransformExecutor`;
- сохранить статус `DONE` или `FAILED`;
- вернуть `resultFileId`;
- отдать статус по `jobId`.

### Целевая реализация

- запись задачи в Facade DB;
- публикация события в Kafka;
- прием события завершения от Tree Transformer;
- статусы задач;
- retry policy.

### Статусы задач

- `CREATED`
- `RUNNING`
- `DONE`
- `FAILED`

### API MVP

`POST /api/transform`

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

## 6. Tree Transformer

### Назначение

Выполнение ETL: parse -> transform -> serialize.

### MVP-функции

- прочитать исходный файл через `ConverterFacade.parse`;
- построить/принять `JsonNode`;
- применить `RuleEngine`;
- сериализовать через `ConverterFacade.serialize`;
- вернуть байты результата.

### Операции правил MVP

- `copy/include`: скопировать значение из `sourcePath` в `targetPath`;
- `rename`: переименовать поле в том же родителе или скопировать в новый путь и удалить старый;
- `delete`: удалить поле;
- `typeCast`: привести тип значения;
- пакетная обработка массивов объектов для CSV и JSON-массивов.

### Ограничения MVP

- Нет JavaScript custom rules.
- Нет сложного изменения вложенности с индексами массивов.
- CSV сериализуется корректно для плоских массивов объектов.
- XML результат может иметь технический root, если иначе Jackson не сериализует дерево стабильно.

### Критерии готовности

- есть тест на `rename + typeCast + delete`;
- есть тест на CSV->JSON с правилами;
- при ошибке правила возвращается путь и причина.

## 7. User Service

### Назначение

Аутентификация, авторизация, пользователи, права доступа к файлам и наборам правил.

### MVP-реализация

Не реализовывать физически. Использовать анонимного пользователя `demo`.

### Целевые функции

- регистрация/логин;
- JWT;
- владелец файла;
- владелец rule-set;
- ограничение доступа к результатам;
- аудит запусков конвертации.

### Критерии для MVP

- в моделях можно предусмотреть `ownerId`, но не требовать его в API;
- файлы доступны только в рамках локального демо.

## 8. Kafka, Redis, PostgreSQL, MinIO

### MVP

Не обязательны. Их лучше не поднимать, если цель - работающий MVP за 3 дня.

### Целевая роль

- Kafka: очередь заданий и событий завершения;
- Redis: кэш rule-set и статусов;
- PostgreSQL: долговременное хранение правил и задач;
- MinIO: файловое хранилище.

### Как подготовить без реализации

- завести интерфейсы портов;
- назвать DTO событий;
- описать docker-compose как future profile, если останется время.

