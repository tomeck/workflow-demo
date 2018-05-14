package com.teck.repositories;

import com.teck.entities.WorkflowInstance;

import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.ViewIndexed;
import org.springframework.data.couchbase.repository.CouchbasePagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@N1qlPrimaryIndexed
@ViewIndexed(designDoc = "workflow")
public interface WorkflowRepository extends CouchbasePagingAndSortingRepository<WorkflowInstance, String> {

    List<WorkflowInstance> findByCorrelationId(String correlationId);

    WorkflowInstance findOne(String id);

    /*
    Page<Building> findByCompanyIdAndNameLikeOrderByName(String companyId, String name, Pageable pageable);

    @Query("#{#n1ql.selectEntity} where #{#n1ql.filter} and companyId = $1 and $2 within #{#n1ql.bucket}")
    Building findByCompanyAndAreaId(String companyId, String areaId);

    @Query("#{#n1ql.selectEntity} where #{#n1ql.filter} AND ANY phone IN phoneNumbers SATISFIES phone = $1 END")
    List<Building> findByPhoneNumber(String telephoneNumber);

    @Query("SELECT COUNT(*) AS count FROM #{#n1ql.bucket} WHERE #{#n1ql.filter} and companyId = $1")
    Long countBuildings(String companyId);
    */
}
