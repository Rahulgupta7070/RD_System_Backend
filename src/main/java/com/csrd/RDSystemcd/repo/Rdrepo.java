package com.csrd.RDSystemcd.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.csrd.RDSystemcd.entity.RdUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface Rdrepo extends JpaRepository<RdUser, Integer>, JpaSpecificationExecutor<RdUser> {

    Page<RdUser> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<RdUser> findByAadharNoContainingOrAccountNumberContaining(
            String aadhar, String account, Pageable pageable);

    Page<RdUser> findAll(Pageable pageable);
}