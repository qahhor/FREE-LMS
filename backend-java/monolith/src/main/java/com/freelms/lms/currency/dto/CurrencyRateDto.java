package com.freelms.lms.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDto {
    private String code;
    private String name;
    private String nameUz;
    private String nameEn;
    private String nameRu;
    private BigDecimal rate;
    private BigDecimal diff;
    private LocalDate date;
}
