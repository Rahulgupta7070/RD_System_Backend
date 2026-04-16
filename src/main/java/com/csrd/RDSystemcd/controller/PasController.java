package com.csrd.RDSystemcd.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.service.RdPasService;
import com.csrd.RDSystemcd.service.EmailService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class PasController {

    private final Cdpassbrepo pasrepo;
    private final RdPasService rdService;
    private final Rdrepo userRepo;
    private final EmailService emailService;

    // ✅ CONSTRUCTOR
    public PasController(Cdpassbrepo pasrepo,
                         RdPasService rdService,
                         Rdrepo userRepo,
                         EmailService emailService) {
        this.pasrepo = pasrepo;
        this.rdService = rdService;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // ✅ GET ALL PASSBOOK USERS
    @GetMapping("/allPassUser")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getAll() {
        return ResponseEntity.ok(pasrepo.findAll());
    }

    // ✅ GET BY ID
    @GetMapping("/puser/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> getById(@PathVariable int id) {
        return pasrepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ GET BY RID
    @GetMapping("/passbook/{rid}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getByRid(@PathVariable int rid) {
        return ResponseEntity.ok(pasrepo.findByRid(rid));
    }

    // ✅ SAVE + EMAIL SEND 🔥
//    @PostMapping("/psave")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
//   public ResponseEntity<RdPassbook> save(@Valid @RequestBody RdPassbook ps) {
//
//    // 🔥 USER FETCH (PEHLE KARO)
//    RdUser user = userRepo.findById(ps.getRid())
//            .orElseThrow(() -> new RuntimeException("User not found"));
//
//    // 🔥 LATE FINE CALCULATE (USER KE SAATH)
//    rdService.calculateLateFine(ps, user);
//
//    // 🔥 SAVE
//    RdPassbook saved = pasrepo.save(ps);
//
//    // 🔥 EMAIL SEND
//    if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
//        emailService.sendDepositEmail(
//                user.getEmail(),
//                user.getName(),
//                ps.getRdAmount().toString(),
//                ps.getRdDate().toString()
//        );
//    }
//
//    return new ResponseEntity<>(saved, HttpStatus.CREATED);
//}
    
    @PostMapping("/psave")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> save(@Valid @RequestBody RdPassbook ps) {

        // 🔥 USER FETCH
        RdUser user = userRepo.findById(ps.getRid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 LATE FINE CALCULATE
        rdService.calculateLateFine(ps, user);

        // 🔥 SAVE
        RdPassbook saved = pasrepo.save(ps);

        // 🔥 EMAIL SEND
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            emailService.sendDepositEmail(
                user.getEmail(),
                user.getName(),
                saved.getRdAmount().toString(),
                saved.getRdDate().toString(),
                saved.getLateDay(),
                saved.getFineAmount(),
                saved.getStatus()
            );
        }

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ✅ DELETE
    @DeleteMapping("/pdelete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable int id) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Not found with id : " + id);
        }

        pasrepo.deleteById(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    // ✅ UPDATE
//    @PutMapping("/pupdate/{id}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
//    public ResponseEntity<RdPassbook> update(
//            @PathVariable int id,
//            @Valid @RequestBody RdPassbook ps) {
//
//        if (!pasrepo.existsById(id)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        rdService.calculateLateFine(ps);
//
//        ps.setPid(id);
//
//        return ResponseEntity.ok(pasrepo.save(ps));
//    }
    @PutMapping("/pupdate/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> update(
            @PathVariable int id,
            @Valid @RequestBody RdPassbook ps) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // 🔥 USER FETCH (IMPORTANT)
        RdUser user = userRepo.findById(ps.getRid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 LATE FINE CALCULATE (CORRECT)
        rdService.calculateLateFine(ps, user);

        // 🔥 SET ID
        ps.setPid(id);

        // 🔥 SAVE
        RdPassbook updated = pasrepo.save(ps);

        // 🔥 EMAIL SEND (OPTIONAL BUT RECOMMENDED)
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            emailService.sendDepositEmail(
                user.getEmail(),
                user.getName(),
                updated.getRdAmount().toString(),
                updated.getRdDate().toString(),
                updated.getLateDay(),
                updated.getFineAmount(),
                updated.getStatus()
            );
        }

        return ResponseEntity.ok(updated);
    }
}