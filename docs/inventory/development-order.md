# Очередность разработки сервисов и библиотек

Документ фиксирует порядок реализации MVP на 3 дня. Главный принцип: сначала собрать работающий сквозной путь, затем улучшать границы сервисов. Для учебной защиты важнее показать полный цикл `upload -> tree -> rules -> transform -> download`, чем физически поднять все компоненты целевой микросервисной схемы.

## 1. Общая стратегия

Целевая архитектура остается такой:

`Web UI -> API Gateway -> File Service / Rule Service / API Facade -> Kafka -> Tree Transformer -> File Service`

Физический MVP-срез на 3 дня:

`Web UI -> MVP Backend -> tree-converter-lib + rule-engine + schema-tree + local storage`

В коде MVP нужно сохранить логические границы через пакеты и интерфейсы, чтобы позже без переписывания вынести их в отдельные сервисы.

## 2. Очередность по приоритетам

### Этап 0. Проверка основы проекта

**Цель:** убедиться, что уже готовая библиотека конвертации реально собирается и пригодна для backend.

Компоненты:

- `tree-converter-lib`;
- тестовые ресурсы JSON/XML/CSV.

Задачи:

- прогнать тесты `tree-converter-lib`;
- проверить публичный API `ConverterFacade`;
- проверить parse/serialize для JSON, XML, CSV;
- зафиксировать ограничения CSV/XML;
- привести название артефакта и документацию к единому виду, если будет время.

Результат:

- понятно, какие пары форматов можно уверенно показывать;
- библиотека готова к подключению в MVP backend.

Блокирует:

- `schema-tree-lib`;
- `rule-engine-lib`;
- Tree Transformer;
- MVP Backend.

## 3. Библиотеки и общие модули

### 1. `tree-converter-lib`

**Очередность:** первая.

**Почему первая:** это ядро чтения и записи файлов. Без нее нельзя строить дерево, применять правила к нормализованной модели и отдавать результат.

**Минимум для MVP:**

- `parse(InputStream, FileFormat): JsonNode`;
- `serialize(JsonNode, OutputStream, FileFormat)`;
- стабильная работа JSON->JSON через parse/serialize;
- стабильная работа CSV->JSON и JSON->CSV для плоских таблиц;
- XML->JSON для демонстрации вложенного дерева.

**Что можно отложить:**

- идеальную поддержку XML->CSV и CSV->XML для вложенных структур;
- streaming-конвертацию без промежуточного дерева;
- расширенные настройки CSV.

### 2. `schema-tree-lib`

**Очередность:** вторая.

**Почему:** UI должен показывать структуру файла сразу после upload. Эта библиотека зависит только от `JsonNode`, поэтому ее можно быстро сделать и тестировать изолированно.

**Минимум для MVP:**

- построение `SchemaNode` из `JsonNode`;
- типы узлов: object, array, string, integer, decimal, boolean, null, unknown;
- path в dot notation;
- sampleValue для примитивов;
- схлопывание массивов объектов в одну схему.

**Используют:**

- MVP Backend endpoint `GET /api/files/{fileId}/tree`;
- Web UI.

### 3. `rule-engine-lib`

**Очередность:** третья.

**Почему:** это главный функциональный риск проекта. Правила должны работать до того, как начнется активная разработка UI.

**Минимум для MVP:**

- модели `RuleSet`, `Rule`, `RuleType`, `TargetType`;
- сортировка правил по `order`;
- операции `COPY`, `RENAME`, `DELETE`, `TYPE_CAST`;
- path resolver;
- применение правил к объектам и массивам объектов;
- понятные ошибки с path и order правила.

**Используют:**

- Tree Transformer;
- MVP Backend transform endpoint;
- Rule Service в целевой архитектуре для валидации.

### 4. `file-storage-core`

**Очередность:** четвертая, параллельно с MVP Backend.

**Почему:** нужен простой способ сохранить исходники и результаты, но не стоит тратить время на MinIO в первые дни.

**Минимум для MVP:**

- интерфейс `FileStoragePort`;
- `LocalFileStorageAdapter`;
- metadata по `fileId`;
- безопасное сохранение файла по UUID;
- выдача файла как stream.

**Используют:**

- File Service в целевой архитектуре;
- MVP Backend в физическом срезе.

### 5. `api-contracts`

**Очередность:** вести параллельно с backend.

**Почему:** DTO должны стабилизироваться до активной разработки UI.

**Минимум для MVP:**

- `FileUploadResponse`;
- `SchemaTreeResponse`;
- `TransformRequest`;
- `TransformResponse`;
- `JobStatusResponse`;
- `ErrorResponse`;
- DTO правил.

**Форма реализации:**

- для скорости можно держать DTO в backend;
- отдельный Maven-модуль создавать только если не ломает темп.

## 4. Очередность сервисов в MVP

### 1. MVP Backend / API Facade first

**Очередность:** первый сервис.

**Почему:** он связывает уже готовую библиотеку, storage, tree builder и rule engine в работающий API. Без него UI и остальные сервисы не проверить.

**Что реализует физически в MVP:**

- upload файла;
- получение дерева;
- запуск конвертации;
- статус задачи;
- скачивание результата;
- in-memory jobs;
- local file storage;
- применение правил синхронно.

**Какие логические роли объединяет:**

- API Facade;
- File Service;
- Rule Service;
- Tree Transformer.

**Порядок endpoint внутри сервиса:**

1. `GET /actuator/health` или простой health.
2. `POST /api/files`.
3. `GET /api/files/{fileId}/tree`.
4. `POST /api/transform`.
5. `GET /api/jobs/{jobId}`.
6. `GET /api/files/{fileId}/download`.

**Критерий готовности:**

- через curl/Postman выполняется весь сценарий без UI.

### 2. Tree Transformer как внутренний модуль

**Очередность:** второй сервисный компонент, но сначала как пакет внутри backend.

**Почему:** его алгоритм нужен сразу, а отдельный процесс с Kafka можно отложить.

**MVP-реализация:**

- `TransformExecutor`;
- `DefaultTransformExecutor`;
- использует `ConverterFacade`, `RuleEngine`, `FileStoragePort`;
- возвращает `resultFileId`.

**Когда выносить в отдельный сервис:**

- после стабильной работы синхронного transform API;
- когда появятся Kafka-события и хранение job в БД.

### 3. File Service как порт + локальный адаптер

**Очередность:** третий компонент.

**Почему:** полноценный File Service с MinIO не нужен для демонстрации, но контракт хранения нужен с первого дня.

**MVP-реализация:**

- `FileStoragePort`;
- `StoredFile`;
- `LocalFileStorageAdapter`;
- controller endpoints в MVP Backend.

**Целевой вынос:**

- заменить local adapter на S3/MinIO;
- вынести endpoints в `file-service`;
- добавить presigned URLs.

### 4. Rule Service как модели + валидатор

**Очередность:** четвертый компонент.

**Почему:** в MVP правила можно передавать прямо в transform request; отдельный CRUD правил не обязателен.

**MVP-реализация:**

- DTO rule-set;
- `RuleSetValidator`;
- примеры rule-set в документации/ресурсах;
- опциональный in-memory repository.

**Целевой вынос:**

- CRUD;
- PostgreSQL;
- версии правил;
- привязка к пользователям;
- Redis cache.

### 5. Web UI

**Очередность:** после готовности API upload/tree/transform.

**Почему:** UI без стабильных контрактов будет переписываться. Но каркас можно начать параллельно после фиксации DTO.

**Порядок разработки UI:**

1. базовый layout рабочего экрана;
2. upload;
3. отображение дерева;
4. форма добавления правила;
5. список правил;
6. запуск конвертации;
7. download;
8. preview результата, если хватает времени.

**Критерий готовности:**

- главный демо-сценарий выполняется из браузера.

### 6. API Gateway

**Очередность:** после MVP UI/backend.

**Почему:** для 3 дней отдельный gateway почти не добавляет ценности, если backend один.

**MVP-замена:**

- CORS в backend;
- единый base URL `/api`.

**Когда нужен:**

- когда сервисы физически разделены;
- когда появляется auth, TLS, rate limit, маршрутизация.

### 7. User Service

**Очередность:** после защиты MVP.

**Почему:** авторизация не входит в критический путь учебного MVP "файловый конвертер".

**MVP-замена:**

- demo-user;
- поле `ownerId` можно предусмотреть, но не использовать.

**Когда нужен:**

- при сохранении пользовательских rule-set;
- при разграничении доступа к файлам;
- при истории задач.

### 8. Kafka, PostgreSQL, Redis, MinIO

**Очередность:** после демонстрируемого MVP.

**Почему:** инфраструктура съест время, но не является обязательной для доказательства работы ETL-логики.

**MVP-замены:**

- Kafka -> прямой вызов `TransformExecutor`;
- PostgreSQL -> in-memory repositories;
- Redis -> без кэша;
- MinIO -> local file storage.

**Когда добавлять:**

1. PostgreSQL для jobs и rules.
2. MinIO для файлов.
3. Kafka для асинхронной обработки.
4. Redis для кэша.

## 5. Порядок работ по дням

### День 1

1. Проверить `tree-converter-lib`.
2. Создать MVP Backend skeleton.
3. Подключить `tree-converter-lib`.
4. Реализовать `FileStoragePort` и local storage.
5. Реализовать upload.
6. Реализовать `schema-tree-lib`.
7. Реализовать endpoint дерева.

Готовность дня: `POST /api/files` и `GET /api/files/{fileId}/tree` работают.

### День 2

1. Реализовать `rule-engine-lib`.
2. Добавить модели transform/job.
3. Реализовать `TransformExecutor`.
4. Реализовать `POST /api/transform`.
5. Реализовать status/download.
6. Добавить тесты правил и transform.

Готовность дня: полный сценарий работает через API.

### День 3

1. Реализовать Web UI.
2. Подключить upload/tree/rules/transform/download.
3. Добавить демо-файлы и rule-set.
4. Добавить README запуска.
5. Стабилизировать ошибки.
6. Подготовить короткий сценарий защиты.

Готовность дня: полный сценарий работает из браузера.

## 6. Матрица зависимостей

| Компонент | Зависит от | Блокирует |
|---|---|---|
| `tree-converter-lib` | нет | tree, rules, transform |
| `schema-tree-lib` | `tree-converter-lib`, `JsonNode` | UI tree |
| `rule-engine-lib` | `JsonNode` | transform |
| `file-storage-core` | нет | upload/download/transform |
| MVP Backend | converter, schema, rules, storage | UI |
| Web UI | API contracts, backend | демо |
| File Service | storage port | целевое разделение |
| Rule Service | rule models/validator | целевое хранение правил |
| Tree Transformer service | transform executor, Kafka | async target architecture |
| API Gateway | физически разделенные сервисы | production-like routing |
| User Service | auth decisions | multi-user mode |

## 7. Что нельзя ставить раньше критического пути

Не начинать до рабочего API-сценария:

- Kafka;
- PostgreSQL migrations;
- MinIO;
- Redis;
- JWT/auth;
- сложный drag-and-drop UI;
- JavaScript custom rules.

Эти задачи полезны, но для 3-дневного MVP они увеличивают риск не успеть показать главное.

## 8. Рекомендуемый порядок коммитов/контрольных точек

1. `converter-lib verified`
2. `backend skeleton and local storage`
3. `file upload and schema tree api`
4. `rule engine`
5. `transform and download api`
6. `web ui upload and tree`
7. `web ui rules and convert`
8. `demo data and docs`

