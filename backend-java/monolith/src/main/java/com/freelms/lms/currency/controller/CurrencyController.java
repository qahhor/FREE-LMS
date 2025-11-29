package com.freelms.lms.currency.controller;

import com.freelms.lms.common.dto.ApiResponse;
import com.freelms.lms.currency.dto.CurrencyRateDto;
import com.freelms.lms.currency.service.CbuCurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency exchange rates from Central Bank of Uzbekistan")
public class CurrencyController {

    private final CbuCurrencyService currencyService;

    @GetMapping("/rates")
    @Operation(summary = "Get all currency rates", description = "Returns all currency rates for today from CBU")
    public ResponseEntity<ApiResponse<List<CurrencyRateDto>>> getAllRates() {
        List<CurrencyRateDto> rates = currencyService.getAllRates();
        return ResponseEntity.ok(ApiResponse.success(rates));
    }

    @GetMapping("/rates/{code}")
    @Operation(summary = "Get currency rate by code", description = "Returns currency rate for specific currency (USD, EUR, RUB, etc.)")
    public ResponseEntity<ApiResponse<CurrencyRateDto>> getRateByCode(
            @Parameter(description = "Currency code (e.g., USD, EUR, RUB)")
            @PathVariable String code) {
        return currencyService.getRateByCode(code.toUpperCase())
                .map(rate -> ResponseEntity.ok(ApiResponse.success(rate)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rates/date/{date}")
    @Operation(summary = "Get rates by date", description = "Returns all currency rates for a specific date")
    public ResponseEntity<ApiResponse<List<CurrencyRateDto>>> getRatesByDate(
            @Parameter(description = "Date in format yyyy-MM-dd")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CurrencyRateDto> rates = currencyService.getRatesByDate(date);
        return ResponseEntity.ok(ApiResponse.success(rates));
    }

    @GetMapping("/rates/{code}/date/{date}")
    @Operation(summary = "Get rate by code and date", description = "Returns currency rate for specific currency and date")
    public ResponseEntity<ApiResponse<CurrencyRateDto>> getRateByCodeAndDate(
            @Parameter(description = "Currency code (e.g., USD, EUR, RUB)")
            @PathVariable String code,
            @Parameter(description = "Date in format yyyy-MM-dd")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return currencyService.getRateByCodeAndDate(code.toUpperCase(), date)
                .map(rate -> ResponseEntity.ok(ApiResponse.success(rate)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/convert/to-uzs")
    @Operation(summary = "Convert to UZS", description = "Convert amount from foreign currency to UZS")
    public ResponseEntity<ApiResponse<BigDecimal>> convertToUzs(
            @Parameter(description = "Currency code (e.g., USD, EUR)")
            @RequestParam String code,
            @Parameter(description = "Amount to convert")
            @RequestParam BigDecimal amount) {
        BigDecimal result = currencyService.convertToUzs(code.toUpperCase(), amount);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/convert/from-uzs")
    @Operation(summary = "Convert from UZS", description = "Convert amount from UZS to foreign currency")
    public ResponseEntity<ApiResponse<BigDecimal>> convertFromUzs(
            @Parameter(description = "Currency code (e.g., USD, EUR)")
            @RequestParam String code,
            @Parameter(description = "Amount in UZS to convert")
            @RequestParam BigDecimal amount) {
        BigDecimal result = currencyService.convertFromUzs(code.toUpperCase(), amount);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
