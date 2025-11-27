package com.freelms.modules.cbu;

import com.freelms.modules.cbu.dto.*;
import com.freelms.modules.cbu.service.CbuCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Smartup LMS Module - CBU Currency Rates Plugin
 *
 * This plugin provides currency exchange rates from the Central Bank of Uzbekistan.
 *
 * Features:
 * - Dashboard widget showing main currency rates (USD, EUR, RUB)
 * - Currency converter
 * - Historical rates
 * - Automatic cache updates
 *
 * Usage in courses:
 * - Display current exchange rates in financial courses
 * - Currency conversion exercises
 * - Real-time data for economics education
 */
@Component
@Slf4j
public class CbuCurrencyPlugin {

    public static final String PLUGIN_ID = "cbu-currency-rates";
    public static final String PLUGIN_VERSION = "1.0.0";
    public static final String PLUGIN_NAME = "CBU Currency Rates";
    public static final String PLUGIN_DESCRIPTION = "Курсы валют Центрального банка Республики Узбекистан";

    @Autowired
    private CbuCurrencyService currencyService;

    private boolean active = false;
    private Map<String, String> configuration = new HashMap<>();

    /**
     * Initialize the plugin
     */
    public void initialize(Map<String, String> config) {
        this.configuration = config;
        log.info("Initializing CBU Currency Plugin v{}", PLUGIN_VERSION);
    }

    /**
     * Activate the plugin
     */
    public void activate() {
        this.active = true;
        log.info("CBU Currency Plugin activated");
    }

    /**
     * Deactivate the plugin
     */
    public void deactivate() {
        this.active = false;
        log.info("CBU Currency Plugin deactivated");
    }

    /**
     * Execute a plugin method
     */
    public Object execute(String method, Object... args) {
        if (!active) {
            throw new IllegalStateException("Plugin is not active");
        }

        return switch (method) {
            case "getAllRates" -> currencyService.getAllRates();
            case "getWidgetData" -> currencyService.getWidgetData();
            case "getRate" -> currencyService.getRateByCode((String) args[0]);
            case "convert" -> {
                CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                        .fromCurrency((String) args[0])
                        .toCurrency((String) args[1])
                        .amount((BigDecimal) args[2])
                        .build();
                yield currencyService.convert(request);
            }
            case "getHistoricalRates" -> currencyService.getRatesForDate((LocalDate) args[0]);
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }

    /**
     * Get plugin metadata
     */
    public Map<String, Object> getMetadata() {
        return Map.of(
                "id", PLUGIN_ID,
                "version", PLUGIN_VERSION,
                "name", PLUGIN_NAME,
                "description", PLUGIN_DESCRIPTION,
                "author", "Smartup LMS Team",
                "website", "https://cbu.uz",
                "category", "WIDGET",
                "permissions", List.of("read:currency_rates"),
                "hooks", List.of("dashboard.widget", "course.embed"),
                "endpoints", List.of(
                        "/api/v1/modules/cbu/rates",
                        "/api/v1/modules/cbu/convert",
                        "/api/v1/modules/cbu/widget"
                )
        );
    }

    /**
     * Get plugin capabilities
     */
    public Map<String, Boolean> getCapabilities() {
        return Map.of(
                "hasDashboardWidget", true,
                "hasSettingsPage", true,
                "hasApiEndpoints", true,
                "hasScheduledTasks", true,
                "hasEventHandlers", false,
                "supportsMultiLanguage", true
        );
    }

    /**
     * Get plugin health
     */
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", active ? "UP" : "DOWN");
        health.put("active", active);
        health.put("lastCheck", Instant.now().toString());

        try {
            CurrencyRatesResponse rates = currencyService.getAllRates();
            health.put("ratesAvailable", !rates.getRates().isEmpty());
            health.put("ratesCount", rates.getRates().size());
            health.put("lastUpdate", rates.getDate());
            health.put("cached", rates.isCached());
        } catch (Exception e) {
            health.put("error", e.getMessage());
        }

        return health;
    }

    /**
     * Get configuration schema
     */
    public Map<String, Object> getConfigurationSchema() {
        return Map.of(
                "displayCurrencies", Map.of(
                        "type", "array",
                        "title", "Отображаемые валюты",
                        "description", "Список кодов валют для отображения в виджете",
                        "default", List.of("USD", "EUR", "RUB", "GBP"),
                        "items", Map.of("type", "string")
                ),
                "refreshInterval", Map.of(
                        "type", "integer",
                        "title", "Интервал обновления (минуты)",
                        "description", "Как часто обновлять курсы валют",
                        "default", 60,
                        "minimum", 15,
                        "maximum", 1440
                ),
                "showDiff", Map.of(
                        "type", "boolean",
                        "title", "Показывать изменение курса",
                        "description", "Отображать изменение курса по сравнению с предыдущим днем",
                        "default", true
                ),
                "showConverter", Map.of(
                        "type", "boolean",
                        "title", "Показывать конвертер",
                        "description", "Отображать калькулятор конвертации валют",
                        "default", true
                ),
                "baseCurrency", Map.of(
                        "type", "string",
                        "title", "Базовая валюта",
                        "description", "Валюта по умолчанию для конвертации",
                        "default", "UZS",
                        "enum", List.of("UZS", "USD", "EUR")
                )
        );
    }

    /**
     * Get widget HTML/data for embedding
     */
    public Map<String, Object> getWidgetEmbed() {
        CurrencyWidgetData data = currencyService.getWidgetData();

        return Map.of(
                "type", "currency-rates",
                "title", "Курсы валют ЦБ РУз",
                "titleUz", "O'zbekiston Respublikasi Markaziy banki valyuta kurslari",
                "titleEn", "CBU Exchange Rates",
                "data", data,
                "refreshable", true,
                "refreshUrl", "/api/v1/modules/cbu/widget",
                "size", Map.of("minWidth", 300, "minHeight", 200, "defaultWidth", 400, "defaultHeight", 300)
        );
    }

    public boolean isActive() {
        return active;
    }
}
