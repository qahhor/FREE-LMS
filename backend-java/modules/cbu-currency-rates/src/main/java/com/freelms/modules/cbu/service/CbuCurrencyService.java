package com.freelms.modules.cbu.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelms.modules.cbu.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Smartup LMS Module - CBU Currency Service
 *
 * Fetches and caches currency exchange rates from the Central Bank of Uzbekistan (cbu.uz).
 * API Documentation: https://cbu.uz/uz/arkhiv-kursov-valyut/json/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CbuCurrencyService {

    private static final String CBU_API_URL = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/";
    private static final String CACHE_KEY = "cbu:currency:rates";
    private static final List<String> MAIN_CURRENCIES = List.of("USD", "EUR", "RUB", "GBP", "CHF", "JPY", "CNY", "KRW");
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Get all current currency rates
     */
    @Cacheable(value = "currency-rates", key = "'all'")
    public CurrencyRatesResponse getAllRates() {
        log.info("Fetching all currency rates from CBU");
        return fetchRatesFromCbu();
    }

    /**
     * Get currency rate by code (USD, EUR, RUB, etc.)
     */
    public Optional<CurrencyRateDto> getRateByCode(String currencyCode) {
        CurrencyRatesResponse response = getAllRates();
        return response.getRates().stream()
                .filter(r -> r.getCode().equalsIgnoreCase(currencyCode) ||
                             r.getCcy().equalsIgnoreCase(currencyCode))
                .findFirst();
    }

    /**
     * Get main currencies for widget display
     */
    public CurrencyWidgetData getWidgetData() {
        CurrencyRatesResponse response = getAllRates();
        List<CurrencyRateDto> allRates = response.getRates();

        List<CurrencyRateDto> mainCurrencies = allRates.stream()
                .filter(r -> MAIN_CURRENCIES.contains(r.getCode().toUpperCase()))
                .sorted(Comparator.comparingInt(r -> MAIN_CURRENCIES.indexOf(r.getCode().toUpperCase())))
                .collect(Collectors.toList());

        List<CurrencyRateDto> additionalCurrencies = allRates.stream()
                .filter(r -> !MAIN_CURRENCIES.contains(r.getCode().toUpperCase()))
                .collect(Collectors.toList());

        CurrencyStatistics stats = buildStatistics(mainCurrencies);

        return CurrencyWidgetData.builder()
                .mainCurrencies(mainCurrencies)
                .additionalCurrencies(additionalCurrencies)
                .lastUpdated(response.getDate())
                .nextUpdate(calculateNextUpdate())
                .statistics(stats)
                .build();
    }

    /**
     * Convert amount between currencies
     */
    public CurrencyConversionResult convert(CurrencyConversionRequest request) {
        CurrencyRatesResponse rates = getAllRates();

        BigDecimal fromRate = BigDecimal.ONE; // UZS
        BigDecimal toRate = BigDecimal.ONE;   // UZS

        if (!request.getFromCurrency().equalsIgnoreCase("UZS")) {
            CurrencyRateDto from = rates.getRates().stream()
                    .filter(r -> r.getCode().equalsIgnoreCase(request.getFromCurrency()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + request.getFromCurrency()));
            fromRate = from.getRateAsBigDecimal().divide(
                    BigDecimal.valueOf(from.getNominalAsInt()), 6, RoundingMode.HALF_UP);
        }

        if (!request.getToCurrency().equalsIgnoreCase("UZS")) {
            CurrencyRateDto to = rates.getRates().stream()
                    .filter(r -> r.getCode().equalsIgnoreCase(request.getToCurrency()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + request.getToCurrency()));
            toRate = to.getRateAsBigDecimal().divide(
                    BigDecimal.valueOf(to.getNominalAsInt()), 6, RoundingMode.HALF_UP);
        }

        // Convert: amount * fromRate / toRate
        BigDecimal amountInUzs = request.getAmount().multiply(fromRate);
        BigDecimal convertedAmount = amountInUzs.divide(toRate, 2, RoundingMode.HALF_UP);
        BigDecimal exchangeRate = fromRate.divide(toRate, 6, RoundingMode.HALF_UP);

        return CurrencyConversionResult.builder()
                .fromCurrency(request.getFromCurrency().toUpperCase())
                .toCurrency(request.getToCurrency().toUpperCase())
                .originalAmount(request.getAmount())
                .convertedAmount(convertedAmount)
                .exchangeRate(exchangeRate)
                .date(rates.getDate())
                .build();
    }

    /**
     * Get rates for a specific date (historical)
     */
    public CurrencyRatesResponse getRatesForDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String url = CBU_API_URL + dateStr + "/";

        log.info("Fetching historical rates for date: {}", dateStr);
        return fetchRatesFromUrl(url, dateStr);
    }

    /**
     * Fetch rates from CBU API
     */
    private CurrencyRatesResponse fetchRatesFromCbu() {
        // Check cache first
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                CurrencyRatesResponse response = objectMapper.convertValue(cached, CurrencyRatesResponse.class);
                response.setCached(true);
                log.debug("Returning cached currency rates");
                return response;
            } catch (Exception e) {
                log.warn("Cache deserialization failed, fetching fresh data");
            }
        }

        return fetchRatesFromUrl(CBU_API_URL, LocalDate.now().toString());
    }

    private CurrencyRatesResponse fetchRatesFromUrl(String url, String date) {
        try {
            String jsonResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (jsonResponse == null || jsonResponse.isBlank()) {
                log.error("Empty response from CBU API");
                return createEmptyResponse(date);
            }

            List<CurrencyRateDto> rates = objectMapper.readValue(jsonResponse,
                    new TypeReference<List<CurrencyRateDto>>() {});

            CurrencyRatesResponse response = CurrencyRatesResponse.builder()
                    .rates(rates)
                    .date(date)
                    .source("cbu.uz")
                    .fetchedAt(Instant.now().toEpochMilli())
                    .cached(false)
                    .build();

            // Cache the response
            redisTemplate.opsForValue().set(CACHE_KEY, response, CACHE_TTL);
            log.info("Successfully fetched {} currency rates from CBU", rates.size());

            return response;

        } catch (Exception e) {
            log.error("Error fetching currency rates from CBU: {}", e.getMessage());
            return createEmptyResponse(date);
        }
    }

    private CurrencyRatesResponse createEmptyResponse(String date) {
        return CurrencyRatesResponse.builder()
                .rates(new ArrayList<>())
                .date(date)
                .source("cbu.uz")
                .fetchedAt(Instant.now().toEpochMilli())
                .cached(false)
                .build();
    }

    private CurrencyStatistics buildStatistics(List<CurrencyRateDto> mainCurrencies) {
        CurrencyStatistics.CurrencyStatisticsBuilder stats = CurrencyStatistics.builder()
                .totalCurrencies(mainCurrencies.size());

        mainCurrencies.forEach(rate -> {
            switch (rate.getCode().toUpperCase()) {
                case "USD":
                    stats.usdRate(rate.getRateAsBigDecimal());
                    stats.usdChange(rate.getDiffAsBigDecimal());
                    stats.usdTrend(rate.getTrendIcon());
                    break;
                case "EUR":
                    stats.eurRate(rate.getRateAsBigDecimal());
                    stats.eurChange(rate.getDiffAsBigDecimal());
                    stats.eurTrend(rate.getTrendIcon());
                    break;
                case "RUB":
                    stats.rubRate(rate.getRateAsBigDecimal());
                    stats.rubChange(rate.getDiffAsBigDecimal());
                    stats.rubTrend(rate.getTrendIcon());
                    break;
            }
        });

        return stats.build();
    }

    private String calculateNextUpdate() {
        // CBU updates rates at approximately 18:00 Uzbekistan time
        return "18:00 UZT";
    }

    /**
     * Scheduled task to refresh currency rates cache
     */
    @Scheduled(cron = "0 0 8,13,18 * * *") // At 08:00, 13:00, and 18:00 daily
    public void refreshRates() {
        log.info("Scheduled currency rates refresh started");
        try {
            redisTemplate.delete(CACHE_KEY);
            fetchRatesFromCbu();
            log.info("Currency rates cache refreshed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh currency rates: {}", e.getMessage());
        }
    }
}
