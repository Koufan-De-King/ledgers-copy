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

package de.adorsys.ledgers.middleware.rest.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ExpectationFailedRestException;
import de.adorsys.ledgers.middleware.rest.exception.GoneRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotAcceptableRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Payment" , description= "Provide endpoint for initiating and executing payment.")
public interface PaymentRestAPI {
	public static final String BASE_PATH = "/payments";

    @GetMapping("/{paymentId}/status")
    @ApiOperation(value="Read Payment Status", notes="Returns the status of a payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<TransactionStatusTO> getPaymentStatusById(@PathVariable("paymentId") String paymentId) throws NotFoundRestException; 

    @GetMapping(value = "/{paymentId}")
    @ApiOperation(value="Load Payment", notes="Returns the payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<?> getPaymentById(@PathVariable(name = "paymentId") String paymentId) throws NotFoundRestException;

    @PostMapping(params="paymentType")
    @ApiOperation(value="Initiates a Payment", notes="Initiates a payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> initiatePayment(
    		@RequestParam("paymentType") PaymentTypeTO paymentType, 
    		@RequestBody Object payment) throws NotFoundRestException;

    @GetMapping(value = "/{paymentId}/authorisations/{authorisationId}")
	@ApiOperation(value = "Get SCA", notes="Get the authorization response object eventually containing the list of selected sca methods.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> getSCA(@PathVariable("paymentId") String paymentId, 
    		@PathVariable("authorisationId") String authorisationId) throws ConflictRestException;
    
    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
	@ApiOperation(value = "Select SCA Method", notes="Select teh given sca method and request for authentication code generation.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> selectMethod(@PathVariable("paymentId") String paymentId, 
    		@PathVariable("authorisationId") String authorisationId,
    		@PathVariable("scaMethodId") String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException;
    
    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/authCode")
	@ApiOperation(value = "Send an authentication code for validation", notes="Validate an authetication code and returns the cosent token", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> authorizePayment(@PathVariable("paymentId") String paymentId,
    		@PathVariable("authorisationId") String authorisationId, 
    		@RequestParam(name="authCode") String authCode) throws GoneRestException,NotFoundRestException, ConflictRestException, ExpectationFailedRestException, NotAcceptableRestException;
    
// =======
    @PostMapping(value = "/{paymentId}/cancellation-authorisations")
    @ApiOperation(value="Initiates a Payment Cancelation", notes="Initiates a Payment Cancelation", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(@PathVariable("paymentId") String paymentId)
    	throws NotFoundRestException, NotAcceptableRestException;
    
    @GetMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}")
	@ApiOperation(value = "Get SCA", notes="Get the authorization response object eventually containing the list of selected sca methods.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> getCancelSCA(@PathVariable("paymentId") String paymentId, 
    		@PathVariable("cancellationId") String cancellationId) throws ConflictRestException;
    
    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/scaMethods/{scaMethodId}")
	@ApiOperation(value = "Select SCA Method", notes="Select teh given sca method and request for authentication code generation.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> selecCancelPaymentSCAtMethod(@PathVariable("paymentId") String paymentId, 
    		@PathVariable("cancellationId") String cancellationId,
    		@PathVariable("scaMethodId") String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException;
    
    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/authCode")
	@ApiOperation(value = "Send an authentication code for validation", notes="Validate an authetication code and returns the cosent token", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAPaymentResponseTO> authorizeCancelPayment(@PathVariable("paymentId") String paymentId,
    		@PathVariable("cancellationId") String cancellationId, 
    		@RequestParam(name="authCode") String authCode) throws GoneRestException,NotFoundRestException, ConflictRestException, ExpectationFailedRestException, NotAcceptableRestException;
}
