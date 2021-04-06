/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.repository;


import com.tecnalia.datausage.model.ContractStore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
/**
 *
 * @author root
 */
@Repository
public interface ContractRepository extends JpaRepository<ContractStore, Long> {
    Optional<ContractStore> findByContractId(String policyId);
    Optional<ContractStore> findByContractUuid(String contractUuid);
    Long  deleteByContractUuid(String contractUuid);
    Iterable<ContractStore> findAllByProviderIdAndConsumerId(String providerId, String consumerId);
    
    
 
   
}