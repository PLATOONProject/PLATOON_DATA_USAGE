package com.tecnalia.datausage.api;

import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.service.PolicyService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-29T09:57:40.792Z[GMT]")
@RestController
public class PolicyApiController implements PolicyApi {
    
    
    @Autowired
    private final PolicyService policyService;

    private static final Logger log = LoggerFactory.getLogger(PolicyApiController.class);

 /*   private final ObjectMapper objectMapper;

    private final HttpServletRequest request;*/

    @org.springframework.beans.factory.annotation.Autowired
   /* public PolicyApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }*/

    
    public PolicyApiController(PolicyService policyService ) {
        this.policyService = policyService;
       
    }
    
    
    
    
    
    public ResponseEntity<String> addPolicyUsingPOST(@Parameter(in = ParameterIn.DEFAULT, description = "policy", required=true, schema=@Schema()) @Valid @RequestBody String body) {
        /*String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<String>(objectMapper.readValue("\"\"", String.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);*/
        
        log.info("PolicyApiController:::body :"+body);
        log.info("PolicyApiController:::: accept????");
        return policyService.addOrUpdate(body);
        
        
    }

    public ResponseEntity<String> deletePolicyUsingDELETE(@Parameter(in = ParameterIn.PATH, description = "", required=true, schema=@Schema()) @PathVariable("policyId") String policyId) 
       /* String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);*/
       
       
    {

     
   
       return policyService.deletePolicy(policyId);




   
      
   
      
      
    
    }
    
    

    
    
    

      public ResponseEntity<List<ContractStore>> getAllPoliciesUsingGET() {
        
        
        
        
        
      
        //String accept = request.getHeader("Accept");
       // if (accept != null && accept.contains("application/json")) {
        
      /*      try {
                
                Iterator var1 = this.odrlPolicyPersistenceService.getAll().iterator();

                while(var1.hasNext()) {
                    OdrlPolicyPersistence odrlPolicyPersistence = (OdrlPolicyPersistence)var1.next();
                    String mydataPolicyIdString = PolicyUtil.getMydataPolicyId(odrlPolicyPersistence.getPolicyId());
                    PolicyId mydataPolicyId = new PolicyId(mydataPolicyIdString);
                }
                
                
                
                return new ResponseEntity<List<OdrlPolicyPersistence>>(objectMapper.readValue("[ {\n  \"policyId\" : \"policyId\",\n  \"odrlPolicyAsString\" : \"odrlPolicyAsString\"\n}, {\n  \"policyId\" : \"policyId\",\n  \"odrlPolicyAsString\" : \"odrlPolicyAsString\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<OdrlPolicyPersistence>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }*/
       // }

        return policyService.getAllOdrlPolicyPersistence();
    }

}
