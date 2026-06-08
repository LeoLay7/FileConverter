# Отчёт о выполненной работе — Спринты 1 и 2

## Спринт 1 — Исследование и архитектура

### Что сделано

Спроектирована логическая архитектура системы в нотации C4 (уровень контейнеров). Определён состав сервисов, их зоны ответственности и потоки взаимодействия.

### Архитектура системы

Система разбита на четыре зоны:

**Клиентский слой**
- Web UI (React / Vue) — пользовательский интерфейс
- API Gateway (Nginx / Spring Cloud Gateway) — единая точка входа HTTP

**Платформа: учётные записи и хранение файлов**
- User Service (Spring Boot) — аутентификация и управление пользователями
- File Service (Spring Boot) — загрузка и выдача файлов через S3-совместимое хранилище (MinIO)

**Конвертация: оркестрация, правила, исполнитель**
- API Facade (Spring Boot) — приём задач, хранение статусов, обмен с Kafka
- Rule Service (Spring Boot) — хранение и жизненный цикл правил конвертации
- Tree Transformer (Spring Boot) — выполнение конвертации JSON / XML / CSV через внутреннюю древовидную модель

**Хранилища и асинхронная доставка**
- Rule DB (PostgreSQL) — конфигурация правил
- Facade DB (PostgreSQL) — задачи и статусы
- Redis — кэш типовых чтений
- Kafka — асинхронная очередь задач

Типовой сценарий: клиент загружает файл → File Service возвращает file_id → клиент инициирует задачу через Facade → Facade публикует сообщение в Kafka → Tree Transformer потребляет задачу, читает файл из File Service, пишет результат обратно → Facade получает событие завершения → клиент скачивает результат.

### Диаграмма контейнеров (C4, PlantUML)

```plantuml
@startuml File_Converter_Architecture
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

hide stereotype

skinparam linetype ortho
skinparam handwritten false
top to bottom direction

title File Converter Service — контейнеры по зонам ответственности

AddBoundaryTag("access", $bgColor="#EFF6FF", $borderColor="#93C5FD", $legendText="клиентский слой")
AddBoundaryTag("platform", $bgColor="#F0FDF4", $borderColor="#86EFAC", $legendText="пользователи и файлы")
AddBoundaryTag("pipeline", $bgColor="#FAF5FF", $borderColor="#D8B4FE", $legendText="сценарий конвертации")
AddBoundaryTag("persistence", $bgColor="#FFFBEB", $borderColor="#FCD34D", $legendText="данные и очередь")

Person(user, "Пользователь", "Работа с файлами и правилами")

System_Boundary(svc, "File Converter Service") {
    Container_Boundary(z1, "Доступ: UI и вход в API", "access") {
        Container(web_ui, "Web UI", "React / Vue", "Интерфейс")
        Container(api_gw, "API Gateway", "Nginx / Spring Cloud Gateway", "Вход HTTP")
    }

    Container_Boundary(z2, "Платформа: учётные записи и хранение файлов", "platform") {
        Container(user_svc, "User Service", "Spring Boot", "Пользователи, аутентификация")
        Container(file_svc, "File Service", "Spring Boot", "Файлы в S3, ссылки на скачивание")
    }

    Container_Boundary(z3, "Конвертация: оркестрация, правила, исполнитель", "pipeline") {
        Container(facade, "API Facade", "Spring Boot", "Задачи, статусы, обмен с Kafka")
        Container(rule_svc, "Rule Service", "Spring Boot", "Правила и наборы правил")
        Container(transformer, "Tree Transformer", "Spring Boot", "JSON, XML, CSV как дерево")
    }

    Container_Boundary(z4, "Хранилища и асинхронная доставка", "persistence") {
        ContainerDb(rule_db, "Rule DB", "PostgreSQL", "Правила")
        ContainerDb(facade_db, "Facade DB", "PostgreSQL", "Задачи")
        ContainerDb(redis, "Redis", "Redis", "Кэш")
        ContainerQueue(kafka, "Kafka", "Apache Kafka", "Очередь задач")
    }
}

System_Ext(minio, "MinIO", "S3-совместимое хранилище")

Lay_R(web_ui, api_gw)
Lay_R(user_svc, file_svc)
Lay_R(facade, rule_svc)
Lay_R(rule_svc, transformer)
Lay_R(rule_db, facade_db)
Lay_R(facade_db, redis)
Lay_R(redis, kafka)
Lay_D(api_gw, facade)
Lay_D(rule_svc, rule_db)
Lay_D(facade, facade_db)
Lay_D(transformer, kafka)
Lay_R(file_svc, minio)

Rel(user, web_ui, "")
Rel(web_ui, api_gw, "")

Rel(api_gw, user_svc, "")
Rel(api_gw, file_svc, "")
Rel(api_gw, facade, "")
Rel(api_gw, rule_svc, "")

Rel(facade, rule_svc, "")
Rel(facade, kafka, "")
Rel(kafka, transformer, "")
Rel(transformer, file_svc, "")
Rel(transformer, kafka, "")
Rel(kafka, facade, "")

Rel(file_svc, minio, "")

Rel(rule_svc, rule_db, "")
Rel(facade, facade_db, "")
Rel(facade, redis, "")
Rel(rule_svc, redis, "")

@enduml
```

---

## Спринт 2 — Парсеры и сериализаторы

### Что сделано

Реализована библиотека `converter-lib` — переиспользуемый модуль конвертации форматов данных, оформленный как Spring Boot Auto-Configuration. Библиотека реализует полный цикл: чтение входного файла → внутренняя древовидная модель (UIM) → запись в целевой формат.

### Стек

- Java 17
- Spring Boot 4.0.6
- Jackson Databind 2.21.2 — парсинг и сериализация JSON
- Jackson Dataformat XML 2.21.2 — парсинг и сериализация XML
- FastCSV 4.2.0 — парсинг и сериализация CSV
- Lombok — сокращение шаблонного кода
- Maven Wrapper 3.9.14

### Внутренняя модель данных (UIM)

В качестве универсальной внутренней модели (Universal Internal Model) используется `com.fasterxml.jackson.databind.JsonNode`. Это позволяет:
- представлять любую иерархическую структуру (объект, массив, примитив) единым типом;
- переиспользовать зрелую экосистему Jackson для обхода и модификации дерева;
- не вводить собственный тип узла дерева на данном этапе.

### Структура модуля

```
converter-lib/
└── src/main/java/shrom/files/convertercore/
    ├── ConverterLibAutoConfiguration.java   # Spring Boot auto-config
    ├── models/
    │   ├── FileFormat.java                  # enum: JSON, XML, CSV
    │   └── ConversionRequest.java           # record-запрос на конвертацию
    └── converter/
        ├── ConverterFacade.java             # главный интерфейс библиотеки
        ├── impl/
        │   └── ConverterFacadeImpl.java     # реализация фасада
        ├── parser/
        │   ├── FileParser.java              # интерфейс парсера
        │   ├── factory/
        │   │   ├── FileParsersFactory.java
        │   │   └── impl/FileParsersFactoryImpl.java
        │   └── impl/
        │       ├── JsonParser.java
        │       ├── XmlParser.java
        │       └── CsvParser.java
        ├── serializer/
        │   ├── FileSerializer.java          # интерфейс сериализатора
        │   ├── factory/
        │   │   ├── FileSerializersFactory.java
        │   │   └── impl/FileSerializersFactoryImpl.java
        │   └── impl/
        │       ├── JsonSerializer.java
        │       ├── XmlSerializer.java
        │       └── CsvSerializer.java
        └── transformation/
            ├── Converter.java               # интерфейс прямого конвертера
            ├── factory/
            │   ├── ConverterFactory.java
            │   └── impl/ConverterFactoryImpl.java
            └── impl/
                ├── JsonToXmlConverter.java
                ├── JsonToCsvConverter.java
                ├── XmlToJsonConverter.java
                ├── XmlToCsvConverter.java
                ├── CsvToJsonConverter.java
                └── CsvToXmlConverter.java
```

### Ключевые компоненты

**ConverterFacade** — главный интерфейс библиотеки. Предоставляет два режима работы:
- Стратегия А: прямой streaming-pipeline `convert(InputStream, OutputStream, FileFormat, FileFormat)` — без промежуточного дерева, минимальные накладные расходы.
- Стратегия Б: раздельные `parse()` / `serialize()` — для случаев, когда сервис хочет работать с деревом напрямую (валидация, применение правил трансформации).
- Перегрузка через `ConversionRequest` — удобный builder-стиль для передачи всех параметров одним объектом.

**ConversionRequest** — Java record с полями `source`, `target`, `sourceFormat`, `targetFormat`. Строится через Lombok `@Builder`.

**FileFormat** — enum с тремя значениями: `JSON`, `XML`, `CSV`.

**Парсеры** (`FileParser`):
- `JsonParser` — использует `ObjectMapper.readTree()`, возвращает `JsonNode`.
- `XmlParser` — использует `XmlMapper.readTree()`, XML-атрибуты и элементы отображаются в поля объекта.
- `CsvParser` — использует FastCSV; первая строка трактуется как заголовки, каждая последующая строка становится `ObjectNode` в результирующем `ArrayNode`. Поддерживает настраиваемый разделитель (по умолчанию `,`).

**Сериализаторы** (`FileSerializer`):
- `JsonSerializer` — `ObjectMapper.writeValue()`.
- `XmlSerializer` — `XmlMapper.writeValue()`.
- `CsvSerializer` — FastCSV; принимает `ArrayNode` или одиночный `ObjectNode`; заголовки берутся из полей первого элемента.

**Прямые конвертеры** (`Converter`) — шесть реализаций для всех пар форматов (JSON↔XML, JSON↔CSV, XML↔CSV). Каждый конвертер самодостаточен: читает входной поток, строит промежуточный `JsonNode`, пишет в выходной поток. `ConverterFactoryImpl` хранит конвертеры в двумерной `Map<FileFormat, Map<FileFormat, Converter>>` и выбрасывает `IllegalArgumentException` при совпадении форматов.

**Фабрики** (`FileParsersFactoryImpl`, `FileSerializersFactoryImpl`, `ConverterFactoryImpl`) — принимают списки реализаций через конструктор (Spring DI), строят `Map` по ключу `FileFormat`. Это позволяет добавлять новые форматы без изменения фабрики.

**Auto-configuration** — `ConverterLibAutoConfiguration` аннотирован `@AutoConfiguration` + `@ComponentScan`, зарегистрирован в `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Любой Spring Boot-сервис, добавивший библиотеку в зависимости, получает все бины автоматически.

### Тестирование

Написаны три тестовых класса, покрывающих все уровни библиотеки:

**FileParserTest** — юнит-тесты парсеров:
- `JsonParser`: проверка полей верхнего уровня, вложенных массивов, выброс `IOException` на невалидном вводе.
- `XmlParser`: размер списка (36 элементов), поля первого и последнего элемента.
- `CsvParser`: размер массива (5 строк), все поля первой и последней строки.

**ConverterTest** — тесты прямых конвертеров:
- JSON→XML: результат парсится `XmlMapper`, проверяются поля.
- XML→JSON: результат парсится `ObjectMapper`, проверяется массив из 36 элементов.
- JSON→CSV: проверяется наличие заголовков и значений.
- CSV→JSON: проверяется структура массива и значения первой строки.
- CSV→XML и XML→CSV: тесты написаны, помечены `@Disabled` (требуют доработки сериализации вложенных структур).

**ConverterFacadeTest** — интеграционные тесты фасада:
- `parse()` для всех трёх форматов.
- `serialize()` JSON→JSON, JSON→XML, CSV→CSV.
- `convert()` через `ConversionRequest`: XML→JSON, JSON→XML.
- `convert()` через stream-перегрузку: CSV→JSON.
- Проверка выброса `IllegalArgumentException` при совпадении форматов.

Тестовые ресурсы:
- `json/sample1.json` — объект с вложенными массивами (пончик с начинками).
- `xml/sample1.xml` — каталог из 36 растений (`CATALOG/PLANT[]`).
- `csv/sample1.csv` — 5 строк пользователей, разделитель `;`.
