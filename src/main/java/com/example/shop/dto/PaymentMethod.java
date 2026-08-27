package com.example.shop.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CardPaymentMethod.class, name = "CARD"),
        @JsonSubTypes.Type(value = WalletPaymentMethod.class, name = "WALLET")
})
public sealed interface PaymentMethod permits CardPaymentMethod, WalletPaymentMethod {

    String kind();
}
