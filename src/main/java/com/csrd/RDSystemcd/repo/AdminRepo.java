package com.csrd.RDSystemcd.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csrd.RDSystemcd.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, Integer> {
    Admin findByEmail(String email);
}