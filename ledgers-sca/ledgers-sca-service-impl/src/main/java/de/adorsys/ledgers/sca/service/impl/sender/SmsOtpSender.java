/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.SMS_OTP;

@Service
@RequiredArgsConstructor
public class SmsOtpSender implements SCASender<MailScaMessage> {
    private final EmailSender emailSender;

    @Override
    public boolean send(MailScaMessage message) {
        return emailSender.send(message);
    }

    @Override
    public ScaMethodTypeBO getType() {
        return SMS_OTP;
    }
}
