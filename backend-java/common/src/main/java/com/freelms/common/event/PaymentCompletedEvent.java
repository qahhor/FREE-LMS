package com.freelms.common.event;

import com.freelms.common.enums.PaymentGateway;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCompletedEvent extends DomainEvent {

    private final Long userId;
    private final Long paymentId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentGateway gateway;

    public PaymentCompletedEvent(Long userId, Long paymentId, BigDecimal amount, String currency, PaymentGateway gateway) {
        super();
        this.userId = userId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.gateway = gateway;
    }
}
