/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.usagecontrol;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tecnalia.datausage.utils.HttpUtils;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author usuario
 */
@Component
public class PersonalDataEnforcement {
    @Value("${cape.enforce.usage.url}")
    private String enforceConsentsUrl = "";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalDataEnforcement.class);
    
    private final PolicyReader policyReader;
    private final HttpUtils httpUtils;

    public PersonalDataEnforcement(PolicyReader policyReader, HttpUtils httpUtils)
        throws IllegalArgumentException {
        if (policyReader == null)
            throw new IllegalArgumentException("The PolicyReader cannot be null.");

        if (httpUtils == null)
            throw new IllegalArgumentException("The HttpUtils cannot be null.");

        this.policyReader = policyReader;
        this.httpUtils = httpUtils;
    }

    public String enforce (ArrayList<Permission> permissionList, String providerURI,
            String consumerURI, String targetDataUri, String dataObject) {
        
        JSONArray filteredDataObject = new JSONArray();
        
        String consumerPurpose = getConsumerPurpose(permissionList, consumerURI);

        JSONArray dataObjectsList = new JSONArray(dataObject);
        for(Object onePersonDataObject: dataObjectsList) {
            JSONObject onePersonDataObjectJson = (JSONObject)onePersonDataObject;
            String userId = getUserId(permissionList, onePersonDataObjectJson);
            JSONObject filteredOnePersonDataObjectJson = enforceConsents(consumerURI, providerURI,
                            userId, targetDataUri, consumerPurpose, onePersonDataObjectJson);
            filteredDataObject.put(filteredOnePersonDataObjectJson);           
        }
        
        return filteredDataObject.toString();
    }
    
    String getConsumerPurpose(ArrayList<Permission> permissionList, String consumerURI) {
        String consumerPurposeAsString = "";  
        URI pip = policyReader.getPipEndpoint(permissionList.get(0).getPreDuty().get(0));
        
        try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI, StandardCharsets.UTF_8.toString());
            consumerPurposeAsString = httpUtils.sendHttpGetRequest(
                    pip + "?consumerUri="+ encodedConsumerUri);
       } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
            return consumerPurposeAsString;
        }

        return consumerPurposeAsString;
    }
    
    String getUserId(ArrayList<Permission> permissionList, JSONObject onePersonDataObjectJson) {
        String userId = "";  
        //Get the value of the jsonPath attribute
        Map <String, Object> propsMap = permissionList.get(0).getPreDuty().get(0).getProperties();
        TypedLiteral userIdPathLit = (TypedLiteral)propsMap.get("https://w3id.org/idsa/code/JsonPath");
        String userIdPath = userIdPathLit.getValue();

        //Get the value from the DataObject indicated by the jsonPath attribute
        String onePersonDataObjectStr = onePersonDataObjectJson.toString();
        DocumentContext jsonContext = JsonPath.parse(onePersonDataObjectStr);
        userId = jsonContext.read(userIdPath);        
        return userId;
    }


    public JSONObject enforceConsents(String consumerURI, String providerURI,
                            String userId, String targetDataUri, String consumerPurpose, 
                            JSONObject onePersonDataObjectJson) {
        JSONObject filteredOnePersonDataObjectJson = new JSONObject();
        //TODO- Invoke CaPe API to filter one person's data
        Map<String,String>params = new HashMap<String, String>();
        params.put("sinkServiceId:", consumerURI);
        params.put("sourceServiceId::", providerURI);
        params.put("userId::", userId);
        params.put("datasetId::", targetDataUri);
        params.put("purposeCategory::", consumerPurpose);
        try {
            String filteredOnePersonDataObjectAsString = httpUtils.sendHttpPostRequest(enforceConsentsUrl, params, onePersonDataObjectJson.toString());
        } catch (URISyntaxException | RuntimeException e) {
        }
        
        //TODO- Quitar esta línea y dejar lo que devuelve CaPe
        filteredOnePersonDataObjectJson = onePersonDataObjectJson;

        return filteredOnePersonDataObjectJson;
    }
}
