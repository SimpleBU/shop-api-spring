# shop-api-spring

Тестовый стенд для проверки связки **Discovery** (извлечение HTTP-эндпоинтов из исходников)
и **Inspect** (сверка извлечённого множества с OpenAPI-спецификацией).

Проверяемый дефект — **«Обнаружен API, отсутствующий в актуальной спецификации» (Shadow API)**.

Доменная область — интернет-магазин: каталог, корзины, заказы, платежи, доставка,
отзывы, промокоды, пользователи и админка.

## Что заложено

| | |
|---|---|
| Spring Boot | 3.2.12 (Spring MVC, `jakarta.*`) |
| Java | 17 (`maven.compiler.release=17`) |
| Зависимости | `spring-boot-starter-web`, `spring-boot-starter-validation` — и всё |
| Хранилище | in-memory `ConcurrentHashMap` в `@Service`-бинах, данные-заглушки |
| Глобальный префикс | `/shop` (`server.servlet.context-path`) |
| Эндпоинтов в коде | **48** в 10 контроллерах |
| Эндпоинтов в спецификации | **38** |
| Shadow API | **10** |
| Zombie API | **0** |

Специально **не** подключены: springdoc-openapi, swagger-annotations, Lombok, MapStruct,
Spring Security, любая БД. Спецификация написана руками и лежит в `api/openapi.yaml`;
на сборке она не генерируется и рантайм её не перетирает.

## Разнообразие способов объявления эндпоинтов

Стенд намеренно собран так, чтобы простой построчный парсер аннотаций дал неполный результат.

| Приём | Где |
|---|---|
| `@RestController` + class-level `@RequestMapping` + `@GetMapping("/{id}")` | `ProductController` |
| Контроллер **без** class-level `@RequestMapping`, полный путь в каждом методе | `PaymentController` |
| Старый стиль `@RequestMapping(value = ..., method = RequestMethod.POST)` | `LegacyOrderController`, `OrderController` |
| Массив путей `@GetMapping({"/carts/{id}", "/baskets/{id}"})` | `CartController` |
| Массив глаголов `@RequestMapping(method = {PUT, PATCH})` | `OrderController` |
| `@Controller` + `@ResponseBody` на методах вместо `@RestController` | `CartController` |
| Базовый путь из **константы класса** | `ShipmentController.BASE` |
| Аннотации в **абстрактном базовом классе** | `AbstractCrudController` → `PromoCodeController` |
| Аннотации **только на интерфейсе**, реализация чистая | `ReviewApi` → `ReviewController` |
| `consumes = MULTIPART_FORM_DATA_VALUE` и `produces = "application/pdf"` | `ShipmentController`, `OrderController` |
| **Regex** в `@PathVariable`: `{number:[0-9]{6}}` | `ShipmentController` |
| `params = "action=export"` и `headers = "X-Api-Version=2"` | `OrderController` |
| Явные `HEAD` и `OPTIONS` | `ProductController`, `PaymentController` |
| Глобальный префикс через context-path | `application.yml` |

Ловушки на false positive: `@RestControllerAdvice` + `@ExceptionHandler`
(`GlobalExceptionHandler`), `@Scheduled` (`CatalogRefreshJob`), `WebMvcConfigurer`
(`WebConfig`). Эндпоинтами они не являются.

## Плоские DTO без иерархий типов

В графе типов DTO нет ни одной самоссылки и ни одного цикла: ни поле, ссылающееся на
собственный тип, ни пара «интерфейс ↔ реализации».

`PaymentMethod` — плоская запись с полем-дискриминатором `kind` (CARD / WALLET) вместо
sealed-интерфейса с двумя реализациями. Значение `kind` определяет, какая группа полей
несёт данные, поля другой группы остаются `null`:

* `CARD` — `last4`, `expiry`, `brand`;
* `WALLET` — `provider`, `walletId`.

Следствие для спецификации: конструкции `oneOf` и `discriminator` в `api/openapi.yaml`
**отсутствуют** — схема описывает ровно тот плоский объект, который отдаёт и принимает код.

## Структура

```
shop-api-spring/
├── pom.xml                                  parent = spring-boot-starter-parent 3.2.12
├── api/
│   ├── openapi.yaml                         OpenAPI 3.0.3, рукописная, 38 операций
│   ├── endpoint-registry.json               машиночитаемый реестр всех 48 эндпоинтов кода
│   ├── expected-findings.json               ground truth: 10 shadow-эндпоинтов
│   └── expected-findings.md                 полная таблица + разбор ловушек
├── tools/verify.py                          сверка спеки, реестра и ground truth
└── src/main/java/com/example/shop/
    ├── web/          10 контроллеров + абстрактный базовый класс + интерфейс
    ├── dto/          DTO-записи с jakarta-валидацией и enum'ами, без иерархий типов
    ├── service/      in-memory сервисы
    ├── model/        внутренние типы (аудит, резервы склада, генератор id)
    ├── config/       WebConfig, CatalogRefreshJob
    └── exception/    доменные исключения + @RestControllerAdvice
```

## Сборка и проверка

```bash
mvn -q clean package -DskipTests
```

```bash
python3 tools/verify.py
```

```bash
npx --yes @redocly/cli lint api/openapi.yaml
```

Линт проходит с **0 ошибок**. Предупреждения ожидаемы: `no-server-example.com`
(в `servers` стоят `localhost` и `example.com`) и одно `no-ambiguous-paths` на паре
`/api/v1/shipments/{id}/documents` — `/api/v1/shipments/invoices/{number}`; последнее
оставлено намеренно как стресс-кейс для резолвера путей, подробности —
в [api/expected-findings.md](api/expected-findings.md).

`verify.py` требует PyYAML и проверяет, что:

* каждая операция спецификации присутствует в реестре с `inSpec = true` (нет zombie API);
* каждый эндпоинт реестра с `inSpec = true` действительно описан в спецификации;
* число shadow-эндпоинтов в `expected-findings.json` равно `totalEndpointsInCode - totalEndpointsInSpec`;
* ни один shadow-эндпоинт не описан в спецификации;
* в `src/` нет комментариев, выдающих shadow-эндпоинты;
* у каждой операции спецификации есть `operationId`, `tags`, ответ 2xx и минимум два кода ошибок.

Приложение можно запустить (`java -jar target/shop-api-spring-1.4.0.jar`), но для анализа
это не требуется — стенд статический.

## Ожидаемый результат анализатора

10 находок «Shadow API». Полный список с указанием файла, строки, стиля объявления,
категории и сложности — в [api/expected-findings.md](api/expected-findings.md)
и [api/expected-findings.json](api/expected-findings.json).
