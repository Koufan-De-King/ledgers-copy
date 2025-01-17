/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.AccountMgmStaffResourceAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersAccountStaff", url = LedgersURL.LEDGERS_URL, path = AccountMgmStaffResourceAPI.BASE_PATH)
public interface AccountMgmtStaffRestClient extends AccountMgmStaffResourceAPI {
}
