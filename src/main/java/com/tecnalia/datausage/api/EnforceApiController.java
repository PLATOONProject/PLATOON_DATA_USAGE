package com.tecnalia.datausage.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tecnalia.datausage.service.EnforcementService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-05-06T08:37:54.369Z[GMT]")
@RestController
public class EnforceApiController implements EnforceApi {

    private static final Logger log = LoggerFactory.getLogger(EnforceApiController.class);

    @Autowired
    private  EnforcementService enforcementService;

    public ResponseEntity<Object> usageControlUseUsingPOST(@NotNull @Parameter(in = ParameterIn.QUERY, description = "" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "targetDataUri", required = true) String targetDataUri,@NotNull @Parameter(in = ParameterIn.QUERY, description = "" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "providerUri", required = true) String providerUri,@NotNull @Parameter(in = ParameterIn.QUERY, description = "" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "consumerUri", required = true) String consumerUri,@NotNull @Parameter(in = ParameterIn.QUERY, description = "" ,required=true,schema=@Schema()) @Valid @RequestParam(value = "consuming", required = true) Boolean consuming,@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody String body) {
          log.info("Enforcing policy for target {}", targetDataUri);
          return enforcementService.enforce(targetDataUri, providerUri, consumerUri, consuming, body);
    }

	@Override
	public ResponseEntity<Object> enforceContractAgreementPOST(@NotNull @Valid String contractAgreement,
			@NotNull @Valid Boolean consuming, @Valid String body) {
		log.info("Enforcing policy for agreement {}", contractAgreement);
        return enforcementService.enforceAgreement(contractAgreement, consuming, body);
	}

}
