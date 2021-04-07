/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.model;

/**
 *
 * @author root
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Id;

public class AccessId implements Serializable {
 

    private String targetUri;
    private String consumerUri;

    public AccessId() {
    }

    public AccessId(String targetUri, String consumerUri) {
        this.targetUri = targetUri;
        this.consumerUri = consumerUri;
    }

    
    
    
    
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessId accessId = (AccessId) o;
        return targetUri.equals(accessId.targetUri) &&
                consumerUri.equals(accessId.consumerUri);
    }
  
   

    @Override
    public int hashCode() {
        return Objects.hash(targetUri, consumerUri);
    }
}
