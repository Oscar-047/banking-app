package com.banking.banking_app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountResponse {
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;
}