package com.tecnalia.datausage.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnalia.datausage.model.IdsUseObject;
import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.model.RuleStore;
import com.tecnalia.datausage.repository.ContractRepository;
import com.tecnalia.datausage.repository.RuleRepository;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Prohibition;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.UnsupportedPatternException;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-11T13:39:14.709Z[GMT]")
@RestController
public class EnforceApiController implements EnforceApi {

    private static final Logger log = LoggerFactory.getLogger(EnforceApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    private  ContractRepository contractRepository;
    
    @Autowired    
    private RuleRepository ruleRepository;

    @Autowired 
    PolicyHandler policyHandler;

    @org.springframework.beans.factory.annotation.Autowired
    public EnforceApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<Object> usageControlUseUsingPOST(@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody IdsUseObject body) {
        String accept = request.getHeader("Accept");

        //Get contracts from ContractAgreement table applied to this providerURI, consumerUri
        Iterable<ContractStore> contractList = this.contractRepository.findAllByProviderIdAndConsumerId(body.getProviderUri(), body.getConsumerUri());
        //Get contracts which start-end dates are valid according to current date, and get the most recent Contract
        Contract validContract = null;
        Date latestContractDate = null;
        Date validContractStart = null;
        String validContractUuid = null;
        for (ContractStore contractStore: contractList) {
            String contractTxt = contractStore.getContractAsString();
            Serializer serializer = new Serializer();
            try {
                Contract contract = serializer.deserialize(contractTxt, Contract.class);
                Date contractStart = contract.getContractStart().toGregorianCalendar().getTime();
                Date contractEnd = contract.getContractEnd().toGregorianCalendar().getTime();
                Date contractDate = contract.getContractDate().toGregorianCalendar().getTime();
                Date date = new Date();
                if (date.after(contractStart) && date.before(contractEnd)) {
                    if((latestContractDate == null) || 
                            ((latestContractDate != null) && (contractDate.after(latestContractDate)))){
                        latestContractDate = contractDate;
                        validContractStart = contractStart;
                        validContract = contract;
                        validContractUuid = contractStore.getContractUuid();
                    }
                } 
            } catch (Exception e) {
                return new ResponseEntity<>("Incorrect contract format", HttpStatus.BAD_REQUEST);
            }
        }
        if(validContract == null)
            return new ResponseEntity<>("No valid contracts found", HttpStatus.BAD_REQUEST);
        
        //Get rules from rule table applied to the contract, targetDataUri
        Iterable<RuleStore> ruleList = this.ruleRepository.findAllByContractUuidAndTargetId(validContractUuid, body.getTargetDataUri());
        //Classify Rules into Permissions and Prohibitions
        ArrayList<Permission> permissionList = new ArrayList<>();
        ArrayList<Prohibition> prohibitionList = new ArrayList<>();
        for (RuleStore ruleStore: ruleList) {
            String ruleTxt = ruleStore.getRuleContent();
            Serializer serializer = new Serializer();
            try {
                Rule rule = serializer.deserialize(ruleTxt, Rule.class);
                if(rule.getClass().getSimpleName().compareToIgnoreCase("Permission") == 0)
                    permissionList.add((Permission)rule);
                else if(rule.getClass().getSimpleName().compareToIgnoreCase("Prohibition") == 0)
                    prohibitionList.add((Prohibition)rule);
             } catch (Exception e) {
                return new ResponseEntity<>("Invalid rule format", HttpStatus.BAD_REQUEST);
            }
        }
        
        try {
            boolean allowAccess = false;
            URI consumerURI = new URI(body.getConsumerUri());
            if(body.isConsuming()) {
               //For each rule, apply enforcement
               allowAccess = policyHandler.onDataAccess(permissionList, prohibitionList, validContractStart, body.getTargetDataUri());               
            } else {
               //For each rule, apply enforcement            
               allowAccess = policyHandler.onDataProvision(permissionList, prohibitionList, consumerURI);
            }
            
            if(allowAccess) {
                return new ResponseEntity<>(body.getDataObject(), HttpStatus.OK); 
            } else {
                return new ResponseEntity<>("PDP decided to inhibit the usage: Event is not allowed according to policy", HttpStatus.FORBIDDEN); 
            }
        } catch (URISyntaxException e) {
            return new ResponseEntity<>("Incorrect conumer URI", HttpStatus.BAD_REQUEST);                      
        } catch (UnsupportedPatternException e) {
            return new ResponseEntity<>("Unsupported Policy Pattern", HttpStatus.BAD_REQUEST);                      
        }
    }

}
