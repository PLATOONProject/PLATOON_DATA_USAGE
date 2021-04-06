package com.tecnalia.datausage.api;

import com.tecnalia.datausage.model.IdsUseObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-11T13:39:14.709Z[GMT]")
@RestController
public class EnforceApiController implements EnforceApi {

    private static final Logger log = LoggerFactory.getLogger(EnforceApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public EnforceApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<Void> usageControlUseUsingPOST(@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody IdsUseObject body) {
        String accept = request.getHeader("Accept");
        //TODO
        //Get policies from OdrlPolicy table applied to this targetDataUri, providerURI, consumerUri
        //Get rules from rule table applied to each policyId, targetDataUri
        if(body.isConsuming()) {
           //TODO
           //For each rule, apply enforcement
        } else {
           //For each rule, apply enforcement            
        }

        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}