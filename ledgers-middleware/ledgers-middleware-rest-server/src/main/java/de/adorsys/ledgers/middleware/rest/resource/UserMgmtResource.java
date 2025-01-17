/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAuthConfirmationService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserMgmtResource implements UserMgmtRestAPI {
    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareAuthConfirmationService authConfirmationService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("hasAccessToAccountByLogin(#login, #iban)")
    public ResponseEntity<Boolean> multilevel(String login, String iban) {
        return ResponseEntity.ok(middlewareUserService.checkMultilevelScaRequired(login, iban));
    }

    @Override
    @PreAuthorize("hasAccessToAccountsByLogin(#login, #references)")
    public ResponseEntity<Boolean> multilevelAccounts(String login, List<AccountReferenceTO> references) {
        return ResponseEntity.ok(middlewareUserService.checkMultilevelScaRequired(login, references));
    }

    @Override
    public ResponseEntity<UserTO> register(String login, String email, String pin, UserRoleTO role) {
        UserTO created = middlewareUserService.create(new UserTO(login, email, pin, role));
        created.setPin(null);
        return ResponseEntity.ok(created);
    }

    @Override
    @PreAuthorize("hasManagerAccessToUser(#userId)")
    public ResponseEntity<UserTO> getUserById(String userId) {
        return ResponseEntity.ok(middlewareUserService.findById(userId));
    }

    @Override
    public ResponseEntity<UserTO> getUser() {
        return ResponseEntity.ok(middlewareUserService.findById(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("isSameUser(#user.id)")
    public ResponseEntity<Void> editSelf(UserTO user) {
        middlewareUserService.editBasicSelf(scaInfoHolder.getUserId(), user);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> updateUserScaData(List<ScaUserDataTO> data) {
        UserTO initiator = middlewareUserService.findById(scaInfoHolder.getUserId());
        UserTO user = middlewareUserService.updateScaData(initiator.getLogin(), data);

        URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                          .build().toUri();

        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<AuthConfirmationTO> verifyAuthConfirmationCode(String authorisationId, String authConfirmCode) {
        return ResponseEntity.ok(authConfirmationService.verifyAuthConfirmationCode(authorisationId, authConfirmCode, scaInfoHolder.getScaInfo().getUserLogin()));
    }

    @Override
    public ResponseEntity<AuthConfirmationTO> completeAuthConfirmation(String authorisationId, boolean authCodeConfirmed) {
        return ResponseEntity.ok(authConfirmationService.completeAuthConfirmation(authorisationId, authCodeConfirmed, scaInfoHolder.getScaInfo().getUserLogin()));
    }

    @Override
    public ResponseEntity<Void> resetPasswordViaEmail(String login) {
        middlewareUserService.resetPasswordViaEmail(login);
        return ResponseEntity.noContent().build();
    }
}
