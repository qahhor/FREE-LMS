# CBU Currency Rates Module

Модуль курсов валют Центрального банка Республики Узбекистан для Smartup LMS.

## Описание

Этот модуль предоставляет актуальные курсы валют от ЦБ РУз (cbu.uz) в виде:
- Виджета для дашборда
- API для интеграции в курсы
- Калькулятора конвертации валют

## Возможности

- **Текущие курсы**: Все валюты, котируемые ЦБ РУз
- **Исторические данные**: Курсы за любую дату
- **Конвертер валют**: Конвертация между любыми валютами через UZS
- **Виджет**: Готовый компонент для дашборда
- **Автообновление**: Кэширование с автоматическим обновлением

## API Endpoints

### Получить все курсы
```http
GET /api/v1/modules/cbu/rates
```

Ответ:
```json
{
  "rates": [
    {
      "code": "USD",
      "ccy": "USD",
      "nameRu": "Доллар США",
      "nameUz": "AQSH dollari",
      "nameEn": "US Dollar",
      "nominal": "1",
      "rate": "12650.45",
      "diff": "+15.23",
      "date": "2024-11-27"
    }
  ],
  "date": "2024-11-27",
  "source": "cbu.uz",
  "cached": true
}
```

### Получить курс конкретной валюты
```http
GET /api/v1/modules/cbu/rates/USD
```

### Получить исторические курсы
```http
GET /api/v1/modules/cbu/rates/date/2024-11-25
```

### Конвертация валют
```http
POST /api/v1/modules/cbu/convert
Content-Type: application/json

{
  "fromCurrency": "USD",
  "toCurrency": "UZS",
  "amount": 100
}
```

Ответ:
```json
{
  "fromCurrency": "USD",
  "toCurrency": "UZS",
  "originalAmount": 100,
  "convertedAmount": 1265045.00,
  "exchangeRate": 12650.45,
  "date": "2024-11-27"
}
```

### Быстрая конвертация
```http
GET /api/v1/modules/cbu/convert?from=USD&to=UZS&amount=100
```

### Данные для виджета
```http
GET /api/v1/modules/cbu/widget
```

## Интеграция в курсы

Модуль можно использовать в образовательных курсах по:
- Экономике и финансам
- Международной торговле
- Бухгалтерскому учёту
- Банковскому делу

### Пример использования в уроке

```html
<div class="cbu-currency-widget" data-currencies="USD,EUR,RUB">
  <!-- Виджет автоматически загрузит курсы -->
</div>

<script>
  // Или программно
  fetch('/api/v1/modules/cbu/widget')
    .then(res => res.json())
    .then(data => {
      console.log('USD:', data.statistics.usdRate);
    });
</script>
```

## Конфигурация

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| displayCurrencies | Список валют для виджета | USD, EUR, RUB, GBP |
| refreshInterval | Интервал обновления (мин) | 60 |
| showDiff | Показывать изменение | true |
| showConverter | Показывать конвертер | true |
| baseCurrency | Базовая валюта | UZS |

## Источник данных

Данные получаются из официального API Центрального банка Республики Узбекистан:
- **URL**: https://cbu.uz/uz/arkhiv-kursov-valyut/json/
- **Обновление**: Ежедневно около 18:00 UZT
- **Формат**: JSON

## Лицензия

MIT License - Smartup LMS
