/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;
import java.util.Set;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG012 - Data management (STAFF access)")
public interface DataMgmtStaffAPI {
    String BASE_PATH = "/staff-access/data";

    @DeleteMapping(value = "/transactions/{accountId}")
    @Operation(summary = "Removes all transactions for account")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> account(@PathVariable(ACCOUNT_ID) String accountId);

    @DeleteMapping(value = "/account/{accountId}")
    @Operation(summary = "Removes account")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> depositAccount(@PathVariable(ACCOUNT_ID) String accountId);

    @DeleteMapping(value = "/user/{userId}")
    @Operation(summary = "Removes user")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> user(@PathVariable(USER_ID) String userId);

    @DeleteMapping(value = "/branch/{branchId}")
    @Operation(summary = "Removes all data related to TPP")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> branch(@PathVariable(BRANCH_ID) String branchId);

    @Operation(summary = "Upload data to Ledgers (users, accounts, transactions, balances)")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping(value = "/upload")
    ResponseEntity<Void> uploadData(@RequestBody UploadedDataTO data);

    @Operation(summary = "Retrieve the currencies list supported by ASPSP")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping(value = "/currencies")
    ResponseEntity<Set<Currency>> currencies();

    @Operation(summary = "Get next free branch id for country")
    @PostMapping(value = "/branch")
    ResponseEntity<String> branchId(@RequestBody BbanStructure bbanStructure);

    @Operation(summary = "Create Recovery point")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping(value = "/point")
    ResponseEntity<Void> createPoint(@RequestBody RecoveryPointTO recoveryPoint);

    @Operation(summary = "Get all Recovery points related to current branch")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping(value = "/point/all")
    ResponseEntity<List<RecoveryPointTO>> getAllPoints();

    @Operation(summary = "Get Recovery point by id related to current branch")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping(value = "/point/{id}")
    ResponseEntity<RecoveryPointTO> getPoint(@PathVariable(ID) Long id);

    @Operation(summary = "Deletes Recovery point by id related to current branch")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @DeleteMapping(value = "/point/{id}")
    ResponseEntity<Void> deletePoint(@PathVariable(ID) Long id);
}
