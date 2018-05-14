package com.teck.service;

import com.teck.entities.WorkflowInstance;

import javax.validation.Valid;
import java.util.List;

public interface WorkflowPersistenceServiceIntfc {

    WorkflowInstance save(@Valid WorkflowInstance workflowInst);

    WorkflowInstance findById(String workflowId);

    /*
    List<Building> findByCompanyId(String companyId);

    Building findByCompanyAndAreaId(String companyId, String areaId);

    List<Building> findByCompanyIdAndNameLike(String companyId, String name, int page);

    List<Building> findByPhoneNumber(String telephoneNumber);

    Long countBuildings(String companyId);
    */
}
