package com.csrd.RDSystemcd.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.csrd.RDSystemcd.entity.RdPassbook;

@Repository
public interface Cdpassbrepo extends JpaRepository<RdPassbook, Integer> {
    List<RdPassbook> findByRid(int rid);
    boolean existsByRidAndRdDate(int rid, LocalDate rdDate);
}