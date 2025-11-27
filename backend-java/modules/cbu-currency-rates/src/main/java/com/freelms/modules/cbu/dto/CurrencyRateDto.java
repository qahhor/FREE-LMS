package com.freelms.modules.cbu.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Smartup LMS Module - CBU Currency Rate DTOs
 *
 * Data models for currency exchange rates from Central Bank of Uzbekistan.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRateDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("Code")
    @JsonAlias("code")
    private String code;  // USD, EUR, RUB, etc.

    @JsonProperty("Ccy")
    @JsonAlias("ccy")
    private String ccy;   // Currency code (same as code)

    @JsonProperty("CcyNm_RU")
    @JsonAlias({"ccyNm_RU", "ccynm_ru"})
    private String nameRu; // Доллар США

    @JsonProperty("CcyNm_UZ")
    @JsonAlias({"ccyNm_UZ", "ccynm_uz"})
    private String nameUz; // AQSH dollari

    @JsonProperty("CcyNm_UZC")
    @JsonAlias({"ccyNm_UZC", "ccynm_uzc"})
    private String nameUzCyrillic; // АҚШ доллари

    @JsonProperty("CcyNm_EN")
    @JsonAlias({"ccyNm_EN", "ccynm_en"})
    private String nameEn; // US Dollar

    @JsonProperty("Nominal")
    @JsonAlias("nominal")
    private String nominal; // 1, 10, 100, 1000

    @JsonProperty("Rate")
    @JsonAlias("rate")
    private String rate;   // Exchange rate value

    @JsonProperty("Diff")
    @JsonAlias("diff")
    private String diff;   // Change from previous day

    @JsonProperty("Date")
    @JsonAlias("date")
    private String date;   // Rate date

    // Helper methods
    public BigDecimal getRateAsBigDecimal() {
        try {
            return new BigDecimal(rate.replace(",", ".").replace(" ", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getDiffAsBigDecimal() {
        try {
            return new BigDecimal(diff.replace(",", ".").replace(" ", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Integer getNominalAsInt() {
        try {
            return Integer.parseInt(nominal);
        } catch (Exception e) {
            return 1;
        }
    }

    public BigDecimal getRatePerUnit() {
        BigDecimal rateValue = getRateAsBigDecimal();
        int nominalValue = getNominalAsInt();
        if (nominalValue > 1) {
            return rateValue.divide(BigDecimal.valueOf(nominalValue), 4, java.math.RoundingMode.HALF_UP);
        }
        return rateValue;
    }

    public boolean isPositiveChange() {
        return getDiffAsBigDecimal().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegativeChange() {
        return getDiffAsBigDecimal().compareTo(BigDecimal.ZERO) < 0;
    }

    public String getTrendIcon() {
        if (isPositiveChange()) return "↑";
        if (isNegativeChange()) return "↓";
        return "→";
    }

    public String getTrendClass() {
        if (isPositiveChange()) return "trend-up";
        if (isNegativeChange()) return "trend-down";
        return "trend-neutral";
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyRatesResponse {
    private List<CurrencyRateDto> rates;
    private String date;
    private String source;
    private long fetchedAt;
    private boolean cached;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyConversionRequest {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyConversionResult {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private String date;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyWidgetData {
    private List<CurrencyRateDto> mainCurrencies;      // USD, EUR, RUB, GBP
    private List<CurrencyRateDto> additionalCurrencies;
    private String lastUpdated;
    private String nextUpdate;
    private CurrencyStatistics statistics;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyStatistics {
    private BigDecimal usdRate;
    private BigDecimal usdChange;
    private String usdTrend;
    private BigDecimal eurRate;
    private BigDecimal eurChange;
    private String eurTrend;
    private BigDecimal rubRate;
    private BigDecimal rubChange;
    private String rubTrend;
    private int totalCurrencies;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CurrencyRateHistory {
    private String currencyCode;
    private List<HistoricalRate> history;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class HistoricalRate {
    private LocalDate date;
    private BigDecimal rate;
    private BigDecimal change;
}
