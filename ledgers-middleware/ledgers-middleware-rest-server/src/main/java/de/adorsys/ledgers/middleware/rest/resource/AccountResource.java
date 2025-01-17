/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(AccountRestAPI.BASE_PATH)
public class AccountResource implements AccountRestAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareAccountManagementService middlewareAccountService;
    private final MiddlewareUserManagementService userManagementService;

    /**
     * Return the list of accounts linked with the current customer.
     *
     * @return : the list of accounts linked with the current customer.
     */
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER','SYSTEM')")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccounts() {
        return ResponseEntity.ok(middlewareAccountService.listDepositAccounts(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("hasAccessToAccount(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(String accountId) {
        return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true));
    }

    @Override
    @PreAuthorize("hasAccessToAccount(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances(String accountId) {
        AccountDetailsTO accountDetails = middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true);
        return ResponseEntity.ok(accountDetails.getBalances());
    }

    @Override
    @PreAuthorize("hasAccessToAccount(#accountId)")
    public ResponseEntity<List<TransactionTO>> getTransactionByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        dateChecker(dateFrom, dateTo);
        List<TransactionTO> transactions = middlewareAccountService.getTransactionsByDates(accountId, validDate(dateFrom), validDate(dateTo));
        return ResponseEntity.ok(transactions);
    }

    @Override
    @PreAuthorize("hasAccessToAccount(#accountId)")
    public ResponseEntity<CustomPageImpl<TransactionTO>> getTransactionByDatesPaged(String accountId, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        dateChecker(dateFrom, dateTo);
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        CustomPageImpl<TransactionTO> customPage = middlewareAccountService.getTransactionsByDatesPaged(accountId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(customPage);
    }

    @Override
    @PreAuthorize("hasAccessToAccount(#accountId)")
    public ResponseEntity<TransactionTO> getTransactionById(String accountId, String transactionId) {
        return ResponseEntity.ok(middlewareAccountService.getTransactionById(accountId, transactionId));
    }

    @Override
    @PreAuthorize("hasAccessToAccountWithIban(#request.psuAccount.iban)")
    public ResponseEntity<Boolean> fundsConfirmation(FundsConfirmationRequestTO request) {
        if (request.getInstructedAmount().getAmount().compareTo(BigDecimal.ZERO) <= 0) { //TODO move to validation filter
            throw MiddlewareModuleException.builder()
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .devMsg("Requested amount less or equal zero")
                          .build();
        }
        boolean fundsAvailable = middlewareAccountService.confirmFundsAvailability(request);
        return ResponseEntity.ok(fundsAvailable);
    }

    @Override
    @PreAuthorize("accountInfoByIdentifier(#accountIdentifierType, #accountIdentifier)")
    public ResponseEntity<List<AdditionalAccountInformationTO>> getAdditionalAccountInfo(AccountIdentifierTypeTO accountIdentifierType, String accountIdentifier) {
        return ResponseEntity.ok(userManagementService.getAdditionalInformation(scaInfoHolder.getScaInfo(), accountIdentifierType, accountIdentifier));
    }

    private void dateChecker(LocalDate dateFrom, LocalDate dateTo) { //TODO move to validationFilter
        if (!validDate(dateFrom).isEqual(validDate(dateTo))
                    && validDate(dateFrom).isAfter(validDate(dateTo))) {
            throw MiddlewareModuleException.builder()
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .devMsg("Illegal request dates sequence, possibly swapped 'date from' with 'date to'")
                          .build();
        }
    }

    private LocalDate validDate(LocalDate date) {
        return Optional.ofNullable(date)
                       .orElseGet(LocalDate::now);
    }
}
