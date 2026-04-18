package com.csrd.RDSystemcd.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csrd.RDSystemcd.config.JwtUtil;
import com.csrd.RDSystemcd.entity.Admin;
import com.csrd.RDSystemcd.repo.AdminRepo;
import com.csrd.RDSystemcd.service.AuditService;
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
    private final AuditService auditService;

    public AuthController(AdminRepo repo, JwtUtil jwtUtil,
            PasswordEncoder encoder,
            EmailService emailService,
            AuditService auditService) {   // 👈 add

this.repo = repo;
this.jwtUtil = jwtUtil;
this.encoder = encoder;
this.emailService = emailService;
this.auditService = auditService; // 👈 add
}

    //  LOGIN
 @PostMapping("/login")
public String login(@RequestBody Admin request, HttpServletRequest httpRequest) {

    Admin admin = repo.findByEmail(request.getEmail());

    if (admin != null && encoder.matches(request.getPassword(), admin.getPassword())) {

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        // 🔥 LOGIN ALERT EMAIL
        if ("ROLE_ADMIN".equals(admin.getRole())) {
            emailService.sendLoginAlert(admin.getEmail());
        }

        // 🔥 AUDIT LOG
        auditService.log(
            "LOGIN",
            admin.getEmail(),
            admin.getRole(),
            "Login successful",
            ip,
            userAgent
        );

        return jwtUtil.generateToken(admin.getEmail(), admin.getRole());
    }

    // ❌ FAILED LOGIN bhi log karo (important)
    auditService.log(
        "FAILED_LOGIN",
        request.getEmail(),
        "UNKNOWN",
        "Invalid credentials",
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent")
    );

    return "Invalid credentials";
}
    //  LOGOUT (SECURED)
 @PostMapping("/logout")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
public String logout(@RequestHeader("Authorization") String header,
                     HttpServletRequest request) {

    if (header != null && header.startsWith("Bearer ")) {

        String token = header.substring(7);
        String email = jwtUtil.extractUsername(token);

        // 🔥 EMAIL
        emailService.sendLogoutAlert(email);

        // 🔥 AUDIT LOG
        auditService.log(
            "LOGOUT",
            email,
            "ADMIN",
            "User logged out",
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
    }

    return "Logged out successfully";
}

    //  CREATE ADMIN
  @PostMapping("/create-admin")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public Admin createAdmin(@RequestBody Admin newAdmin,
                         HttpServletRequest request) {

    newAdmin.setPassword(encoder.encode(newAdmin.getPassword()));
    newAdmin.setRole("ROLE_ADMIN");

    Admin saved = repo.save(newAdmin);

    // 🔥 AUDIT
    auditService.log(
        "CREATE_ADMIN",
        saved.getEmail(),
        "SUPER_ADMIN",
        "New admin created",
        request.getRemoteAddr(),
        request.getHeader("User-Agent")
    );

    return saved;
}
}