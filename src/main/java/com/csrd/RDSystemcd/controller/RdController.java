package com.csrd.RDSystemcd.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.service.RDService;
import com.csrd.RDSystemcd.service.EmailService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/rdusers")
public class RdController {

    private final RDService rdService;
    private final Rdrepo rdrepo;
    private final EmailService emailService;

    public RdController(Rdrepo rdrepo, RDService rdService, EmailService emailService) {
        this.rdrepo = rdrepo;
        this.rdService = rdService;
        this.emailService = emailService;
    }

    // ✅ TEST
    @GetMapping("/test")
    public String test() {
        return "API Working 🚀";
    }

    // ✅ GET ALL USERS
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/allUser")
    public ResponseEntity<Page<RdUser>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RdUser> users = rdrepo.findAll(pageable);

        return ResponseEntity.ok(users);
    }

    // ✅ GET USER BY ID
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RdUser> getUserById(@PathVariable int id) {
        return rdrepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ SAVE USER + EMAIL
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PostMapping("/saveUser")
    public ResponseEntity<RdUser> saveUser(@Valid @RequestBody RdUser rd) {

        RdUser savedUser = rdrepo.save(rd);

        // 🔥 EMAIL SEND (SAFE)
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

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // ✅ DELETE USER
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {

        if (!rdrepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with id : " + id);
        }

        rdrepo.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // ✅ UPDATE USER
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<RdUser> updateUser(
            @PathVariable int id,
            @RequestBody RdUser rd) {

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

        RdUser updatedUser = rdrepo.save(existing);

        return ResponseEntity.ok(updatedUser);
    }

    // ✅ SEARCH
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @GetMapping("/search")
    public Page<RdUser> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        keyword = keyword.trim().replaceAll("\\s+", "");

        if (keyword.matches("\\d+")) {
            return rdrepo.findByAadharNoContainingOrAccountNumberContaining(
                    keyword, keyword, pageable);
        }

        return rdrepo.findByNameContainingIgnoreCase(keyword, pageable);
    }

    // ✅ FILTER
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
}