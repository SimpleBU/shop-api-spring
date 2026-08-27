# shop-api-spring — ground truth

Эталонная разметка для проверки связки **Discovery** (извлечение эндпоинтов из кода)
и **Inspect** (сверка извлечённого множества с OpenAPI-спецификацией).

Проверяемый дефект — **«Обнаружен API, отсутствующий в актуальной спецификации» (Shadow API)**.

| Показатель | Значение |
|---|---|
| Фреймворк | Spring Boot 3.2.12, Spring MVC, `jakarta.*` |
| Глобальный префикс | `/shop` (`server.servlet.context-path` в `src/main/resources/application.yml`) |
| Спецификация | `api/openapi.yaml` (OpenAPI 3.0.3, рукописная, не генерируется из кода) |
| Эндпоинтов в коде | **48** |
| Эндпоинтов в спецификации | **38** |
| Shadow API (есть в коде, нет в спеке) | **10** |
| Zombie API (есть в спеке, нет в коде) | **0** — обратных расхождений заложено не было |
| Классов-контроллеров | 10 |

Пути в таблицах приведены **полностью**, с учётом context-path `/shop`.
В `api/openapi.yaml` тот же префикс вынесен в `servers[].url`, поэтому в поле `paths`
спецификации он не повторяется.

## 1. Ожидаемые находки: shadow-эндпоинты

| Метод | Полный путь | Где объявлен | Категория | Сложность | Комментарий |
|---|---|---|---|---|---|
| `PATCH` | `/shop/api/v1/products/{id}/price` | `src/main/java/com/example/shop/web/ProductController.java:78` | hotfix | easy | Единственный неописанный метод контроллера, все остальные шесть эндпоинтов ProductController есть в спецификации |
| `PATCH` | `/shop/api/v1/orders/{id}/status` | `src/main/java/com/example/shop/web/OrderController.java:66` | second-http-verb | medium | Один Java-метод обслуживает два глагола. PUT описан в спецификации, PATCH — нет. Анализатор обязан развернуть массив method в два эндпоинта |
| `POST` | `/shop/legacy/orders` | `src/main/java/com/example/shop/web/LegacyOrderController.java:24` | legacy | medium | Старый стиль объявления вместо @PostMapping; путь задан целиком на методе |
| `GET` | `/shop/api/v0/orders/{id}` | `src/main/java/com/example/shop/web/LegacyOrderController.java:32` | legacy | easy | Остаток предыдущей мажорной версии API |
| `GET` | `/shop/api/v1/baskets/{id}` | `src/main/java/com/example/shop/web/CartController.java:29` | second-path-of-array | medium | Первый путь массива (/carts/{id}) описан в спецификации, второй — нет. Дополнительно контроллер объявлен как @Controller, а не @RestController |
| `DELETE` | `/shop/api/v1/promo-codes/{id}` | `src/main/java/com/example/shop/web/AbstractCrudController.java:31` | inheritance | hard | Аннотация и class-level префикс лежат в разных файлах. Два соседних унаследованных метода (GET списка и GET по id) в спецификации есть |
| `PATCH` | `/shop/api/v1/reviews/{id}/moderate` | `src/main/java/com/example/shop/web/ReviewApi.java:36` | interface-declared | hard | Ни одной HTTP-аннотации в самом @RestController — весь контракт объявлен на интерфейсе |
| `DELETE` | `/shop/admin/users/{id}/purge` | `src/main/java/com/example/shop/web/AdminUserController.java:54` | admin | easy | Прямое объявление, должен находиться любым парсером |
| `GET` | `/shop/internal/debug/cache` | `src/main/java/com/example/shop/web/InternalOpsController.java:23` | debug | easy | Отладочный эндпоинт, выставляющий внутреннюю статистику кэшей |
| `POST` | `/shop/internal/webhooks/payment-callback` | `src/main/java/com/example/shop/web/InternalOpsController.java:28` | internal-integration | easy | Приём колбэка платёжного провайдера, наружу не документирован |

### Распределение по сложности

* **easy** — прямая аннотация на методе, путь виден в одном файле.
  Должно находиться любым парсером аннотаций.
* **medium** — путь или глагол не читается «в лоб»: массив путей в одной аннотации,
  массив глаголов в `method = {...}`, старый стиль `@RequestMapping(value=..., method=...)`.
  Требуется разворачивать аннотацию в несколько эндпоинтов.
* **hard** — объявление находится не в том файле, где стоит `@RestController`:
  аннотации в абстрактном базовом классе и на методах интерфейса.
  Требуется разрешать иерархию типов.

| Сложность | Кол-во |
|---|---|
| easy | 5 |
| medium | 3 |
| hard | 2 |

## 2. Полный реестр эндпоинтов кода

| # | Метод | Полный путь | Контроллер | Стиль объявления | В спеке |
|---|---|---|---|---|---|
| 1 | `GET` | `/shop/api/v1/products` | ProductController | @RestController + class @RequestMapping, @GetMapping | да |
| 2 | `GET` | `/shop/api/v1/products/{id}` | ProductController | @GetMapping("/{id}") | да |
| 3 | `POST` | `/shop/api/v1/products` | ProductController | @PostMapping | да |
| 4 | `PUT` | `/shop/api/v1/products/{id}` | ProductController | @PutMapping("/{id}") | да |
| 5 | `DELETE` | `/shop/api/v1/products/{id}` | ProductController | @DeleteMapping("/{id}") | да |
| 6 | `HEAD` | `/shop/api/v1/products/{id}/availability` | ProductController | @RequestMapping(method = RequestMethod.HEAD) | да |
| 7 | `PATCH` | `/shop/api/v1/products/{id}/price` | ProductController | @PatchMapping("/{id}/price") | **НЕТ** |
| 8 | `GET` | `/shop/api/v1/payments` | PaymentController | @GetMapping с полным путём, без class-level @RequestMapping | да |
| 9 | `GET` | `/shop/api/v1/payments/{id}` | PaymentController | @GetMapping с полным путём, без class-level @RequestMapping | да |
| 10 | `POST` | `/shop/api/v1/payments` | PaymentController | @PostMapping с полным путём, без class-level @RequestMapping | да |
| 11 | `POST` | `/shop/api/v1/payments/{id}/refund` | PaymentController | @PostMapping с полным путём, без class-level @RequestMapping | да |
| 12 | `OPTIONS` | `/shop/api/v1/payments` | PaymentController | @RequestMapping(method = RequestMethod.OPTIONS) | да |
| 13 | `GET` | `/shop/api/v1/orders` | OrderController | class @RequestMapping + @GetMapping | да |
| 14 | `GET` | `/shop/api/v1/orders/summary` | OrderController | @GetMapping(headers = "X-Api-Version=2") | да |
| 15 | `GET` | `/shop/api/v1/orders/{id}` | OrderController | @GetMapping("/{id}") | да |
| 16 | `POST` | `/shop/api/v1/orders` | OrderController | @RequestMapping(method = RequestMethod.POST) — старый стиль | да |
| 17 | `PUT` | `/shop/api/v1/orders/{id}/status` | OrderController | @RequestMapping(method = {PUT, PATCH}) | да |
| 18 | `PATCH` | `/shop/api/v1/orders/{id}/status` | OrderController | @RequestMapping(method = {PUT, PATCH}) | **НЕТ** |
| 19 | `GET` | `/shop/api/v1/orders/{id}/export` | OrderController | @RequestMapping(params = "action=export", produces = "application/pdf") | да |
| 20 | `DELETE` | `/shop/api/v1/orders/{id}` | OrderController | @DeleteMapping("/{id}") | да |
| 21 | `POST` | `/shop/legacy/orders` | LegacyOrderController | @RequestMapping(value = "/legacy/orders", method = RequestMethod.POST) | **НЕТ** |
| 22 | `GET` | `/shop/api/v0/orders/{id}` | LegacyOrderController | @GetMapping с полным путём | **НЕТ** |
| 23 | `GET` | `/shop/api/v1/carts/{id}` | CartController | @Controller + @ResponseBody, @GetMapping({...}) — первый путь массива | да |
| 24 | `GET` | `/shop/api/v1/baskets/{id}` | CartController | @Controller + @ResponseBody, @GetMapping({...}) — второй путь массива | **НЕТ** |
| 25 | `POST` | `/shop/api/v1/carts/{id}/items` | CartController | @Controller + @ResponseBody, @PostMapping | да |
| 26 | `DELETE` | `/shop/api/v1/carts/{id}/items/{sku}` | CartController | @Controller + @ResponseBody, @DeleteMapping | да |
| 27 | `PATCH` | `/shop/api/v1/carts/{id}` | CartController | @Controller + @ResponseBody, @PatchMapping | да |
| 28 | `GET` | `/shop/api/v1/shipments` | ShipmentController | @RequestMapping(ShipmentController.BASE) — путь из константы | да |
| 29 | `GET` | `/shop/api/v1/shipments/{id}` | ShipmentController | константа класса + @GetMapping("/{id}") | да |
| 30 | `POST` | `/shop/api/v1/shipments` | ShipmentController | константа класса + @PostMapping | да |
| 31 | `POST` | `/shop/api/v1/shipments/{id}/documents` | ShipmentController | @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE) | да |
| 32 | `GET` | `/shop/api/v1/shipments/invoices/{number}` | ShipmentController | @GetMapping("/invoices/{number:[0-9]{6}}", produces = "application/pdf") | да |
| 33 | `GET` | `/shop/api/v1/promo-codes` | PromoCodeController | @GetMapping в абстрактном базовом классе (наследование) | да |
| 34 | `GET` | `/shop/api/v1/promo-codes/{id}` | PromoCodeController | @GetMapping("/{id}") в абстрактном базовом классе (наследование) | да |
| 35 | `DELETE` | `/shop/api/v1/promo-codes/{id}` | PromoCodeController | @DeleteMapping("/{id}") в абстрактном базовом классе (наследование) | **НЕТ** |
| 36 | `POST` | `/shop/api/v1/promo-codes` | PromoCodeController | @PostMapping в самом контроллере | да |
| 37 | `POST` | `/shop/api/v1/promo-codes/{code}/redeem` | PromoCodeController | @PostMapping("/{code}/redeem") | да |
| 38 | `GET` | `/shop/api/v1/reviews` | ReviewController | @GetMapping на методе интерфейса ReviewApi | да |
| 39 | `GET` | `/shop/api/v1/reviews/{id}` | ReviewController | @GetMapping("/{id}") на методе интерфейса ReviewApi | да |
| 40 | `POST` | `/shop/api/v1/reviews` | ReviewController | @PostMapping на методе интерфейса ReviewApi | да |
| 41 | `DELETE` | `/shop/api/v1/reviews/{id}` | ReviewController | @DeleteMapping("/{id}") на методе интерфейса ReviewApi | да |
| 42 | `PATCH` | `/shop/api/v1/reviews/{id}/moderate` | ReviewController | @PatchMapping("/{id}/moderate") на методе интерфейса ReviewApi | **НЕТ** |
| 43 | `GET` | `/shop/admin/users` | AdminUserController | class @RequestMapping("/admin/users") + @GetMapping | да |
| 44 | `GET` | `/shop/admin/users/{id}` | AdminUserController | @GetMapping("/{id}") | да |
| 45 | `PUT` | `/shop/admin/users/{id}/role` | AdminUserController | @PutMapping("/{id}/role") | да |
| 46 | `DELETE` | `/shop/admin/users/{id}/purge` | AdminUserController | @DeleteMapping("/{id}/purge") | **НЕТ** |
| 47 | `GET` | `/shop/internal/debug/cache` | InternalOpsController | @GetMapping с полным путём | **НЕТ** |
| 48 | `POST` | `/shop/internal/webhooks/payment-callback` | InternalOpsController | @PostMapping с полным путём | **НЕТ** |

### Сводка по контроллерам

| Контроллер | Всего эндпоинтов | Из них shadow |
|---|---|---|
| `ProductController` | 7 | 1 |
| `PaymentController` | 5 | 0 |
| `OrderController` | 8 | 1 |
| `LegacyOrderController` | 2 | 2 |
| `CartController` | 5 | 1 |
| `ShipmentController` | 5 | 0 |
| `PromoCodeController` | 5 | 1 |
| `ReviewController` | 5 | 1 |
| `AdminUserController` | 4 | 1 |
| `InternalOpsController` | 2 | 2 |

## 3. Ловушки на false positive

Перечисленное ниже **не является эндпоинтами**. Если анализатор сообщит о них
как о shadow API — это ложное срабатывание.

| Что | Файл | Почему не эндпоинт |
|---|---|---|
| @RestControllerAdvice + @ExceptionHandler | `src/main/java/com/example/shop/exception/GlobalExceptionHandler.java` | Пять методов с @ExceptionHandler. Эндпоинтами не являются, HTTP-путей не имеют |
| @Scheduled | `src/main/java/com/example/shop/config/CatalogRefreshJob.java` | Два метода с @Scheduled (cron и fixedDelay). Не эндпоинты |
| WebMvcConfigurer | `src/main/java/com/example/shop/config/WebConfig.java` | addCorsMappings объявляет паттерн "/api/**" — это CORS-правило, а не эндпоинт |
| URI-строки в телах методов | `src/main/java/com/example/shop/web/*.java` | URI.create("/api/v1/...") в заголовках Location — строковые литералы путей, а не объявления маршрутов |
| abstract protected методы | `src/main/java/com/example/shop/web/AbstractCrudController.java` | loadAll/loadOne/removeOne и их реализации в PromoCodeController не аннотированы и эндпоинтами не являются |

Дополнительно:

* `com.example.shop.dto.*` — DTO-записи; строковые `@Pattern`-регулярки
  (`^SKU-[0-9]{5}$`, `^[A-Z0-9]{4,16}$`) не являются путями;
* `ShipmentController.BASE` — строковая константа `"/api/v1/shipments"`,
  сама по себе эндпоинтом не является, но участвует в сборке путей класса;
* `URI.create("/api/v1/...")` в телах методов — заголовки `Location`, а не маршруты;
* `WebConfig.addCorsMappings` объявляет паттерн `"/api/**"` — это CORS-правило.

### Намеренная неоднозначность путей

`npx @redocly/cli lint` выдаёт на этой спецификации предупреждение `no-ambiguous-paths`:

```
/api/v1/shipments/{id}/documents   и   /api/v1/shipments/invoices/{number}
```

Шаблоны действительно пересекаются на уровне OpenAPI (гипотетический
`/api/v1/shipments/invoices/documents` подошёл бы под оба). Оставлено намеренно:
в коде коллизии нет — Spring предпочитает литеральный сегмент шаблонному,
а `{number:[0-9]{6}}` дополнительно сужает совпадение. Это стресс-кейс для
резолвера путей анализатора, а не дефект спецификации: ошибок линта — 0.

Остальные предупреждения линта (`no-server-example.com`) относятся к `localhost`
и `example.com` в `servers` и для тестового стенда ожидаемы.

## 4. Как перепроверить

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn -q clean package -DskipTests
python3 tools/verify.py
npx --yes @redocly/cli lint api/openapi.yaml
```

`tools/verify.py` сверяет три файла между собой: `api/openapi.yaml`,
`api/endpoint-registry.json` и `api/expected-findings.json` — и дополнительно
грепает `src/` на комментарии, выдающие shadow-эндпоинты.
