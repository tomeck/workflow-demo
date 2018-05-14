package com.teck.service;

import com.teck.entities.WorkflowInstance;
import com.teck.repositories.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

@Service
public class WorkflowPersistenceServiceImpl implements WorkflowPersistenceServiceIntfc {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Override
    public WorkflowInstance findById(String workflowId) {
        return workflowRepository.findOne(workflowId);
    }

    @Override
    public WorkflowInstance save(@Valid WorkflowInstance workflowInst) {
        return workflowRepository.save(workflowInst);
    }

    /*
    @Override
    public List<Building> findByCompanyId(String companyId) {
        return buildingRepository.findByCompanyId(companyId);
    }

    public List<Building> findByCompanyIdAndNameLike(String companyId, String name, int page) {
        return buildingRepository.findByCompanyIdAndNameLikeOrderByName(companyId, name, new PageRequest(page, 20))
                .getContent();
    }

    @Override
    public Building findByCompanyAndAreaId(String companyId, String areaId) {
        return buildingRepository.findByCompanyAndAreaId(companyId, areaId);
    }

    @Override
    public List<Building> findByPhoneNumber(String telephoneNumber) {
        return buildingRepository.findByPhoneNumber(telephoneNumber);
    }

    @Override
    public Long countBuildings(String companyId) {
        return buildingRepository.countBuildings(companyId);
    }
    */
}
