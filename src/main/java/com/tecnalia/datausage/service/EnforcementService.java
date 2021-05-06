/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.service;

import com.tecnalia.datausage.model.AccessStore;
import com.tecnalia.datausage.model.IdsUseObject;
import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.model.RuleStore;
import com.tecnalia.datausage.repository.AccessRepository;
import com.tecnalia.datausage.repository.ContractRepository;
import com.tecnalia.datausage.repository.RuleRepository;
import com.tecnalia.datausage.usagecontrol.PersonalDataEnforcement;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Prohibition;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.UnsupportedPatternException;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author usuario
 */
@Service
public class EnforcementService {
    private static final Logger log = LoggerFactory.getLogger(EnforcementService.class);

    @Autowired
    private  ContractRepository contractRepository;
    
    @Autowired    
    private RuleRepository ruleRepository;

    @Autowired   
    private AccessRepository accessRepository;   

    @Autowired
    PolicyHandler policyHandler;

    @Autowired
    PersonalDataEnforcement personalDataEnforcement;

    public ResponseEntity<Object> enforce(IdsUseObject body) {
        //Get contracts from ContractAgreement table applied to this providerURI & consumerUri
        Iterable<ContractStore> contractList = this.contractRepository.findAllByProviderIdAndConsumerId(body.getProviderUri(), body.getConsumerUri());
        //Get contracts that apply to targetUri and which start-end dates are valid according to current date, and get the most recent Contract
        ContractStore validContractStore = getValidContracts(contractList, body.getTargetDataUri());
        if(validContractStore == null)
            return new ResponseEntity<>("No valid contracts found", HttpStatus.BAD_REQUEST);
        String contractTxt = validContractStore.getContractAsString();
        Date validContractStart = null;
        try {
            Serializer serializer = new Serializer();
            Contract contract = serializer.deserialize(contractTxt, Contract.class);
            validContractStart = contract.getContractStart().toGregorianCalendar().getTime();
        } catch (Exception e) {
            return new ResponseEntity<>("Incorrect contract format", HttpStatus.BAD_REQUEST);
        }
        
        //Get rules from rule table applied to the contract, targetDataUri
        Iterable<RuleStore> ruleList = this.ruleRepository.findAllByContractUuidAndTargetId(validContractStore.getContractUuid(), body.getTargetDataUri());
        //Classify Rules into Permissions and Prohibitions
        ArrayList<Permission> permissionList = new ArrayList<>();
        ArrayList<Prohibition> prohibitionList = new ArrayList<>();
        for (RuleStore ruleStore: ruleList) {
            String ruleTxt = ruleStore.getRuleContent();
            Serializer serializer = new Serializer();
            try {
                //Rule rule = serializer.deserialize(ruleTxt, Rule.class);
                //String ruleType = getRuleType(rule);
                String ruleType = getRuleType(ruleTxt);
                if(ruleType.compareToIgnoreCase("Permission") == 0) {
                    Permission rule = serializer.deserialize(ruleTxt, Permission.class);
                    permissionList.add(rule);
                }
                else if(ruleType.compareToIgnoreCase("Prohibition") == 0) {
                    Prohibition rule = serializer.deserialize(ruleTxt, Prohibition.class);
                    prohibitionList.add((Prohibition)rule);
                }
             } catch (Exception e) {
                return new ResponseEntity<>("Invalid rule format", HttpStatus.BAD_REQUEST);
            }
        }
        
        try {
            boolean allowAccess = false;
            Object filteredDataObject = body.getDataObject();
            if(body.isConsuming()) {
               //For each rule, apply enforcement
               allowAccess = policyHandler.onDataAccess(permissionList, prohibitionList, validContractStart, body.getTargetDataUri(), body.getConsumerUri());
               if(policyHandler.getPattern(permissionList, prohibitionList)== Pattern.PERSONAL_DATA) {
                  filteredDataObject = personalDataEnforcement.enforce(
                          permissionList, 
                          body.getProviderUri(),
                          body.getConsumerUri(),
                          body.getTargetDataUri(),
                          body.getDataObject());
               }
               if(allowAccess)
                   incrementAccessFrequency(body.getTargetDataUri(), body.getConsumerUri());
            } else {
               //For each rule, apply enforcement            
               allowAccess = policyHandler.onDataProvision(permissionList, prohibitionList, body.getConsumerUri());
            }
            
            if(allowAccess) {
                return new ResponseEntity<>(filteredDataObject, HttpStatus.OK); 
            } else {
                return new ResponseEntity<>("PDP decided to inhibit the usage: Event is not allowed according to policy", HttpStatus.FORBIDDEN); 
            }
        } catch (UnsupportedPatternException e) {
            return new ResponseEntity<>("Unsupported Policy Pattern", HttpStatus.BAD_REQUEST);                      
        }
    }
    
    ContractStore getValidContracts(Iterable<ContractStore> contractList, String targetDataUri) {
        //Get contracts that apply to targetUri and which start-end dates are valid according to current date, and get the most recent Contract
        ContractStore validContractStore = null;
        Date latestContractDate = null;
        for (ContractStore contractStore: contractList) {
            //Firstly check if conract applies to targetDataUri
            Optional<RuleStore> bCheckExistsRule = this.ruleRepository.findByContractUuidAndTargetId(contractStore.getContractUuid(), targetDataUri);
            if(!bCheckExistsRule.isPresent())
                //If contract not applies to targetDataUri, do not consider it
                continue;
            String contractTxt = contractStore.getContractAsString();
            Serializer serializer = new Serializer();
            try {
                Contract contract = serializer.deserialize(contractTxt, Contract.class);
                Date contractStart = contract.getContractStart().toGregorianCalendar().getTime();
                Date contractEnd = null;
                XMLGregorianCalendar contractEndGregCal = contract.getContractEnd();
                if(contractEndGregCal != null) {
                    contractEnd = contractEndGregCal.toGregorianCalendar().getTime();
                }
                Date contractDate = contract.getContractDate().toGregorianCalendar().getTime();
                Date date = new Date();
                if (date.after(contractStart) && 
                        ((contractEnd==null) || (contractEnd != null && date.before(contractEnd)))) {
                    if((latestContractDate == null) || 
                            ((latestContractDate != null) && (contractDate.after(latestContractDate)))){
                        validContractStore = contractStore;
                        latestContractDate = contractDate;
                    }
                } 
            } catch (Exception e) {
            }
        }
        return validContractStore;
    }
    
    String getRuleTypeOLd(Rule rule) {
        String ruleType = "";        
        List<TypedLiteral> labelsList = rule.getLabel();
        for (TypedLiteral lit: labelsList) {
            String val = lit.getValue();
            if ((val.compareToIgnoreCase("Permission")== 0) || (val.compareToIgnoreCase("Prohibition")== 0)) {
                ruleType= val;
                break;
            }
        }
        return ruleType;
    }
    
    String getRuleType(String ruleTxt) {
        String ruleType = "";
        Serializer serializer = new Serializer();
        try  {
            Permission rule = serializer.deserialize(ruleTxt, Permission.class);
            ruleType = "Permission";
        }catch ( Exception ex) {
            
        }
            
        try  {
            Prohibition rule = serializer.deserialize(ruleTxt, Prohibition.class);
            ruleType = "Prohibition";
        }catch ( Exception ex) {
            
        }
        
        return ruleType;
    }
    @Transactional
    void incrementAccessFrequency(String targetDataUri, String consumerUri) {
        //Increment by 1 the access frequency
        Optional<AccessStore> bCheckExistsAccess = this.accessRepository.findByConsumerUriAndTargetUri(consumerUri, targetDataUri);
        //if exists update
        if (bCheckExistsAccess.isPresent()) {
            AccessStore accessStore = (AccessStore)bCheckExistsAccess.get();
            accessStore.setNumAccess(accessStore.getNumAccess() + 1);
            this.accessRepository.saveAndFlush(accessStore);                              
        } else {
        //insert
            AccessStore accessStore = new AccessStore();
            accessStore.setConsumerUri(consumerUri);
            accessStore.setTargetUri(targetDataUri);
            accessStore.setNumAccess(1);
            this.accessRepository.saveAndFlush(accessStore);
        }
        return;        
    }
}
