package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WalletPaymentMethod(
        @NotBlank @Size(max = 40) String provider,
        @NotBlank @Size(max = 64) String walletId) implements PaymentMethod {

    @Override
    public String kind() {
        return "WALLET";
    }
}
