/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepositAccountPaymentServiceImpl implements DepositAccountPaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    public DepositAccountPaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResultBO<TransactionStatusBO> getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment
                                                      .map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return new PaymentResultBO<>(TransactionStatusBO.valueOf(transactionStatus.name()));
    }

    @Override
    public PaymentBO getPaymentById(PaymentTypeBO paymentType, PaymentProductBO paymentProduct, String paymentId) throws PaymentNotFoundException {
        PaymentBO payment = paymentRepository.findById(paymentId)
                                    .map(paymentMapper::toPaymentBO)
                                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return filterPaymentByTypeAndProduct(payment, paymentType, paymentProduct);
    }

    private PaymentBO filterPaymentByTypeAndProduct(PaymentBO payment, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException {
        boolean isPresentPayment = PaymentTypeBO.valueOf(payment.getPaymentType().name()) == paymentType;
        if (payment.getPaymentType() != PaymentTypeBO.BULK) {
            isPresentPayment = payment.getTargets().stream()
                                   .map(t -> PaymentProductBO.valueOf(t.getPaymentProduct().name()))
                                   .allMatch(t -> t == paymentProduct);
        }
        if (!isPresentPayment) {
            throw new PaymentNotFoundException(payment.getPaymentId());
        }
        return payment;
    }
}
