/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.db.EnableDepositAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = DepositAccountServiceBasePackage.class,
        basePackages = "de.adorsys.ledgers.cleanup")
@EnableDepositAccountRepository
public class DepositAccountServiceConfiguration {

    private final ASPSPConfigSource configSource;

    @Autowired
    public DepositAccountServiceConfiguration(ASPSPConfigSource configSource) {
        this.configSource = configSource;
    }

    @Bean
    public ASPSPConfigData configData() {
        return configSource.aspspConfigData();
    }
}
