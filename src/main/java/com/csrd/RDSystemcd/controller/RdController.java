package com.csrd.RDSystemcd.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.service.AuditService;
import com.csrd.RDSystemcd.service.EmailService;
import com.csrd.RDSystemcd.service.RDService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/rdusers")
public class RdController {

    private final RDService rdService;
    private final Rdrepo rdrepo;
    private final EmailService emailService;
    private final AuditService auditService;

    public RdController(Rdrepo rdrepo,
                        RDService rdService,
                        EmailService emailService,
                        AuditService auditService) {
        this.rdrepo = rdrepo;
        this.rdService = rdService;
        this.emailService = emailService;
        this.auditService = auditService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RdUser> getUserById(@PathVariable int id) {

        return rdrepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= GET ALL USERS =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/allUser")
    public ResponseEntity<Page<RdUser>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RdUser> users = rdrepo.findAll(pageable);

        return ResponseEntity.ok(users);
    }

    // ================= SAVE USER =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/saveUser")
    public ResponseEntity<RdUser> saveUser(@Valid @RequestBody RdUser rd,
                                           HttpServletRequest request) {

        RdUser savedUser = rdrepo.save(rd);

        if (savedUser.getEmail() != null &&
            savedUser.getRdDate() != null &&
            savedUser.getRdAmount() != null) {

            emailService.sendUserEmail(
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRdDate().toString(),
                savedUser.getRdAmount().toString()
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
            "CREATE_USER",
            username,
            role,
            "User ID: " + savedUser.getRid() +
            ", Name: " + savedUser.getName(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // ================= DELETE USER =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id,
                                             HttpServletRequest request) {

        if (!rdrepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with id : " + id);
        }

        rdrepo.deleteById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
            "DELETE_USER",
            username,
            role,
            "User ID: " + id,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );

        return ResponseEntity.ok("User deleted successfully");
    }

    // ================= UPDATE USER =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<RdUser> updateUser(
            @PathVariable int id,
            @RequestBody RdUser rd,
            HttpServletRequest request) {

        Optional<RdUser> optionalUser = rdrepo.findById(id);

        if (!optionalUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        RdUser existing = optionalUser.get();

        existing.setName(rd.getName());
        existing.setEmail(rd.getEmail());
        existing.setAddress(rd.getAddress());
        existing.setDob(rd.getDob());
        existing.setGender(rd.getGender());
        existing.setRdDate(rd.getRdDate());
        existing.setRdAmount(rd.getRdAmount());
        existing.setOccupation(rd.getOccupation());
        existing.setAccountNumber(rd.getAccountNumber());
        existing.setAadharNo(rd.getAadharNo());
        existing.setPanNo(rd.getPanNo());
        existing.setNomineeName(rd.getNomineeName());
        existing.setNomineeAddress(rd.getNomineeAddress());
        existing.setNomineeAadharNo(rd.getNomineeAadharNo());

        // 🔥 IMPORTANT FIX (THIS WAS MISSING)
        existing.setTotalMonths(rd.getTotalMonths());

        RdUser updatedUser = rdrepo.save(existing);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
            "UPDATE_USER",
            username,
            role,
            "User ID: " + id +
            ", Name: " + updatedUser.getName(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );

        return ResponseEntity.ok(updatedUser);
    }
    
    
 // ================= FILTER =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/filter")
    public Page<RdUser> filterUsers(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return rdService.filterUsers(startDate, endDate, minAmount, maxAmount, pageable);
    }
 // ================= SEARCH =================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/search")
    public Page<RdUser> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        keyword = keyword.trim().replaceAll("\\s+", "");

        // Agar number hai → Aadhar / Account search
        if (keyword.matches("\\d+")) {
            return rdrepo.findByAadharNoContainingOrAccountNumberContaining(
                    keyword, keyword, pageable);
        }

        // Otherwise → Name search
        return rdrepo.findByNameContainingIgnoreCase(keyword, pageable);
    }
}