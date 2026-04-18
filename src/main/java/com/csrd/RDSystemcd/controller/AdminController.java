package com.csrd.RDSystemcd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.entity.Admin;
import com.csrd.RDSystemcd.repo.AdminRepo;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminRepo repo;

    public AdminController(AdminRepo repo) {
        this.repo = repo;
    }

    //  GET ALL ADMINS (ONLY SUPER ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<Admin> getAllAdmins() {
        return repo.findAll();
    }

    //  DELETE ADMIN
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteAdmin(@PathVariable int id) {

        Admin admin = repo.findById(id).orElse(null);

        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        //  SUPER ADMIN DELETE BLOCK
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            return ResponseEntity.badRequest()
                    .body("Cannot delete Super Admin ❌");
        }

        repo.deleteById(id);

        return ResponseEntity.ok("Admin deleted successfully");
    }
}