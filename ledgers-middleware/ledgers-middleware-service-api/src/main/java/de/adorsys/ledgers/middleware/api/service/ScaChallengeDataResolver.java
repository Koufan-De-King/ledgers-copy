/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;


import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;

public interface ScaChallengeDataResolver {
    ScaChallengeData resolveScaChallengeData(ScaMethodTypeTO scaMethodTypeTO);
}
