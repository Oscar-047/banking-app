package com.banking.banking_app.service;

import com.banking.banking_app.dto.AccountResponse;
import com.banking.banking_app.dto.DepositRequest;
import com.banking.banking_app.dto.WithdrawRequest;
import com.banking.banking_app.model.Account;
import com.banking.banking_app.model.Transaction;
import com.banking.banking_app.repository.AccountRepository;
import com.banking.banking_app.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountResponse createAccount(String name) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountHolderName(name);
        account.setBalance(BigDecimal.ZERO);
        accountRepository.save(account);

        return toResponse(account);
    }

    public AccountResponse getAccount(String accountNumber) {
        Account account = findAccount(accountNumber);
        return toResponse(account);
    }

    @Transactional
    public AccountResponse deposit(DepositRequest request) {
        Account account = findAccount(request.getAccountNumber());

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        saveTransaction(account, "DEPOSIT", request.getAmount());

        return toResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(WithdrawRequest request) {
        Account account = findAccount(request.getAccountNumber());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        saveTransaction(account, "WITHDRAW", request.getAmount());

        return toResponse(account);
    }

    public List<Transaction> getTransactions(String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private void saveTransaction(Account account, String type, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAccountNumber(account.getAccountNumber());
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance());
        transactionRepository.save(tx);
    }

    private String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setBalance(account.getBalance());
        return response;
    }
}