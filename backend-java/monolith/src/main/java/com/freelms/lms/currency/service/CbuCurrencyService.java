package com.freelms.lms.currency.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freelms.lms.currency.dto.CurrencyRateDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for fetching currency rates from Central Bank of Uzbekistan (CBU)
 * API: https://cbu.uz/ru/arkhiv-kursov-valyut/json/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CbuCurrencyService {

    private static final String CBU_API_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final RestTemplate restTemplate;

    /**
     * Get all currency rates for today
     */
    @Cacheable(value = "currencyRates", key = "'all'")
    public List<CurrencyRateDto> getAllRates() {
        return fetchRates(null, null);
    }

    /**
     * Get currency rate by code (e.g., USD, EUR, RUB)
     */
    @Cacheable(value = "currencyRates", key = "#code")
    public Optional<CurrencyRateDto> getRateByCode(String code) {
        List<CurrencyRateDto> rates = fetchRates(code, null);
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }

    /**
     * Get currency rates for a specific date
     */
    @Cacheable(value = "currencyRates", key = "#date.toString()")
    public List<CurrencyRateDto> getRatesByDate(LocalDate date) {
        return fetchRates(null, date);
    }

    /**
     * Get specific currency rate for a specific date
     */
    @Cacheable(value = "currencyRates", key = "#code + '_' + #date.toString()")
    public Optional<CurrencyRateDto> getRateByCodeAndDate(String code, LocalDate date) {
        List<CurrencyRateDto> rates = fetchRates(code, date);
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }

    /**
     * Convert amount from one currency to UZS
     */
    public BigDecimal convertToUzs(String currencyCode, BigDecimal amount) {
        return getRateByCode(currencyCode)
                .map(rate -> amount.multiply(rate.getRate()))
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + currencyCode));
    }

    /**
     * Convert amount from UZS to another currency
     */
    public BigDecimal convertFromUzs(String currencyCode, BigDecimal amountUzs) {
        return getRateByCode(currencyCode)
                .map(rate -> amountUzs.divide(rate.getRate(), 2, BigDecimal.ROUND_HALF_UP))
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + currencyCode));
    }

    private List<CurrencyRateDto> fetchRates(String code, LocalDate date) {
        try {
            String url = buildUrl(code, date);
            log.debug("Fetching currency rates from: {}", url);

            CbuRate[] response = restTemplate.getForObject(url, CbuRate[].class);

            if (response == null || response.length == 0) {
                log.warn("No currency rates returned from CBU API");
                return Collections.emptyList();
            }

            return Arrays.stream(response)
                    .map(this::mapToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching currency rates from CBU", e);
            return Collections.emptyList();
        }
    }

    private String buildUrl(String code, LocalDate date) {
        StringBuilder url = new StringBuilder(CBU_API_URL);

        if (code != null && !code.isEmpty()) {
            url.append(code).append("/");
        }

        if (date != null) {
            url.append("?date=").append(date.format(DATE_FORMATTER));
        }

        return url.toString();
    }

    private CurrencyRateDto mapToDto(CbuRate rate) {
        return CurrencyRateDto.builder()
                .code(rate.getCcy())
                .name(rate.getCcyNmUz())
                .nameUz(rate.getCcyNmUz())
                .nameEn(rate.getCcyNmEn())
                .nameRu(rate.getCcyNmRu())
                .rate(new BigDecimal(rate.getRate()))
                .diff(new BigDecimal(rate.getDiff()))
                .date(LocalDate.parse(rate.getDate(), DATE_FORMATTER))
                .build();
    }

    /**
     * CBU API response model
     */
    @Data
    private static class CbuRate {
        @JsonProperty("id")
        private String id;

        @JsonProperty("Code")
        private String code;

        @JsonProperty("Ccy")
        private String ccy;

        @JsonProperty("CcyNm_RU")
        private String ccyNmRu;

        @JsonProperty("CcyNm_UZ")
        private String ccyNmUz;

        @JsonProperty("CcyNm_UZC")
        private String ccyNmUzc;

        @JsonProperty("CcyNm_EN")
        private String ccyNmEn;

        @JsonProperty("Nominal")
        private String nominal;

        @JsonProperty("Rate")
        private String rate;

        @JsonProperty("Diff")
        private String diff;

        @JsonProperty("Date")
        private String date;
    }
}
