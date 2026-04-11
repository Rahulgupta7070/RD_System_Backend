package com.csrd.RDSystemcd.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.entity.Admin;
import com.csrd.RDSystemcd.repo.AdminRepo;
import com.csrd.RDSystemcd.config.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AdminRepo repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;

    public AuthController(AdminRepo repo, JwtUtil jwtUtil, PasswordEncoder encoder) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
    }

    // 🔐 LOGIN
    @PostMapping("/login")
    public String login(@RequestBody Admin request) {

        Admin admin = repo.findByEmail(request.getEmail());

        if (admin != null && encoder.matches(request.getPassword(), admin.getPassword())) {
            return jwtUtil.generateToken(admin.getEmail(), admin.getRole());
        }

        return "Invalid credentials";
    }

    // 👑 CREATE ADMIN (ONLY SUPER ADMIN)
    @PostMapping("/create-admin")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public Admin createAdmin(@RequestBody Admin newAdmin) {

        newAdmin.setPassword(encoder.encode(newAdmin.getPassword()));
        newAdmin.setRole("ADMIN");

        return repo.save(newAdmin);
    }
}