package com.freelms.modules.cbu;

import com.freelms.modules.cbu.dto.*;
import com.freelms.modules.cbu.service.CbuCurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Smartup LMS Module - CBU Currency Rates Controller
 *
 * REST API for accessing CBU currency exchange rates.
 */
@RestController
@RequestMapping("/api/v1/modules/cbu")
@RequiredArgsConstructor
@Tag(name = "CBU Currency Rates", description = "Currency exchange rates from Central Bank of Uzbekistan")
public class CbuCurrencyController {

    private final CbuCurrencyService currencyService;
    private final CbuCurrencyPlugin plugin;

    // ==================== Currency Rates ====================

    @GetMapping("/rates")
    @Operation(summary = "Get all currency rates", description = "Returns all current exchange rates from CBU")
    public ResponseEntity<CurrencyRatesResponse> getAllRates() {
        return ResponseEntity.ok(currencyService.getAllRates());
    }

    @GetMapping("/rates/{code}")
    @Operation(summary = "Get rate by currency code", description = "Returns exchange rate for a specific currency (USD, EUR, RUB, etc.)")
    public ResponseEntity<CurrencyRateDto> getRateByCode(@PathVariable String code) {
        return currencyService.getRateByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rates/date/{date}")
    @Operation(summary = "Get historical rates", description = "Returns exchange rates for a specific date")
    public ResponseEntity<CurrencyRatesResponse> getRatesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(currencyService.getRatesForDate(date));
    }

    // ==================== Widget ====================

    @GetMapping("/widget")
    @Operation(summary = "Get widget data", description = "Returns data formatted for dashboard widget display")
    public ResponseEntity<CurrencyWidgetData> getWidgetData() {
        return ResponseEntity.ok(currencyService.getWidgetData());
    }

    @GetMapping("/widget/embed")
    @Operation(summary = "Get widget embed code", description = "Returns widget configuration for embedding")
    public ResponseEntity<Map<String, Object>> getWidgetEmbed() {
        return ResponseEntity.ok(plugin.getWidgetEmbed());
    }

    // ==================== Currency Conversion ====================

    @PostMapping("/convert")
    @Operation(summary = "Convert currency", description = "Convert amount from one currency to another")
    public ResponseEntity<CurrencyConversionResult> convert(@RequestBody CurrencyConversionRequest request) {
        return ResponseEntity.ok(currencyService.convert(request));
    }

    @GetMapping("/convert")
    @Operation(summary = "Quick convert", description = "Quick currency conversion via query parameters")
    public ResponseEntity<CurrencyConversionResult> quickConvert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .amount(amount)
                .build();

        return ResponseEntity.ok(currencyService.convert(request));
    }

    // ==================== Plugin Info ====================

    @GetMapping("/info")
    @Operation(summary = "Get plugin info", description = "Returns plugin metadata and configuration")
    public ResponseEntity<Map<String, Object>> getPluginInfo() {
        return ResponseEntity.ok(plugin.getMetadata());
    }

    @GetMapping("/health")
    @Operation(summary = "Get plugin health", description = "Returns plugin health status")
    public ResponseEntity<Map<String, Object>> getPluginHealth() {
        return ResponseEntity.ok(plugin.getHealth());
    }

    @GetMapping("/capabilities")
    @Operation(summary = "Get plugin capabilities", description = "Returns plugin capabilities")
    public ResponseEntity<Map<String, Boolean>> getCapabilities() {
        return ResponseEntity.ok(plugin.getCapabilities());
    }

    @GetMapping("/config/schema")
    @Operation(summary = "Get configuration schema", description = "Returns JSON schema for plugin configuration")
    public ResponseEntity<Map<String, Object>> getConfigSchema() {
        return ResponseEntity.ok(plugin.getConfigurationSchema());
    }
}
