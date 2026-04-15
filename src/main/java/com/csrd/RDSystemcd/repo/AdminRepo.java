package com.csrd.RDSystemcd.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.csrd.RDSystemcd.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, Integer> {
    @Query("SELECT a FROM Admin a WHERE a.email = :email")
    Admin findByEmail(String email);
    Admin findByRole(String role);
}