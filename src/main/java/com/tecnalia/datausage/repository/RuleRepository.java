/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.repository;



import com.tecnalia.datausage.model.RuleStore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 *
 * @author root
 */
@Repository

public interface RuleRepository extends JpaRepository<RuleStore, String> {
    Iterable<RuleStore> findAllByContractId(String contractId);
    List<RuleStore> deleteByContractId(String contractId);
    List<RuleStore> deleteByContractUuid(String contracUuid);
    Iterable<RuleStore> findAllByContractUuidAndTargetId(String contractUuid, String targetId);
    
}