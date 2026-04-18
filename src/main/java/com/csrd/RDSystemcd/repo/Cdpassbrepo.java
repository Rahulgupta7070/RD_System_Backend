package com.csrd.RDSystemcd.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.csrd.RDSystemcd.entity.RdPassbook;

@Repository
public interface Cdpassbrepo extends JpaRepository<RdPassbook, Integer> {
    List<RdPassbook> findByRid(int rid);
    boolean existsByRidAndRdDate(int rid, LocalDate rdDate);
    @Query("SELECT COUNT(p) > 0 FROM RdPassbook p WHERE p.rid = :rid AND MONTH(p.rdDate) = :month AND YEAR(p.rdDate) = :year")
    boolean existsByRidAndMonth(@Param("rid") int rid,
                                @Param("month") int month,
                                @Param("year") int year);
}