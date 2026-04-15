package com.csrd.RDSystemcd.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.csrd.RDSystemcd.config.JwtUtil;
import com.csrd.RDSystemcd.entity.Admin;
import com.csrd.RDSystemcd.repo.AdminRepo;
import com.csrd.RDSystemcd.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AdminRepo repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public AuthController(AdminRepo repo, JwtUtil jwtUtil, PasswordEncoder encoder, EmailService emailService) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    // 🔐 LOGIN
    @PostMapping("/login")
    public String login(@RequestBody Admin request, HttpServletRequest httpRequest) {

        Admin admin = repo.findByEmail(request.getEmail());

        if (admin != null && encoder.matches(request.getPassword(), admin.getPassword())) {

            String ip = httpRequest.getRemoteAddr(); // 🔥 IP

            String userAgent = httpRequest.getHeader("User-Agent"); // 🔥 Device info

            if ("ROLE_ADMIN".equals(admin.getRole())) {
                emailService.sendLoginAlert(admin.getEmail());
            }

            return jwtUtil.generateToken(admin.getEmail(), admin.getRole());
        }

        return "Invalid credentials";
    }

    // 🔴 LOGOUT (SECURED)
    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public String logout(@RequestHeader("Authorization") String header) {

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            String email = jwtUtil.extractUsername(token);

            // 🔥 logout alert
            emailService.sendLogoutAlert(email);
        }

        return "Logged out successfully";
    }

    // 👑 CREATE ADMIN
    @PostMapping("/create-admin")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public Admin createAdmin(@RequestBody Admin newAdmin) {

        newAdmin.setPassword(encoder.encode(newAdmin.getPassword()));
        newAdmin.setRole("ROLE_ADMIN");

        return repo.save(newAdmin);
    }
}