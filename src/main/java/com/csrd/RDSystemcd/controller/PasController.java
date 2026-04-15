package com.csrd.RDSystemcd.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.service.RdPasService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class PasController {

    private final Cdpassbrepo pasrepo;
    private final RdPasService rdService;

    public PasController(Cdpassbrepo pasrepo, RdPasService rdService) {
        this.pasrepo = pasrepo;
        this.rdService = rdService;
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

    // ✅ SAVE
    @PostMapping("/psave")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> save(@Valid @RequestBody RdPassbook ps) {

        rdService.calculateLateFine(ps);

        RdPassbook saved = pasrepo.save(ps);

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
    @PutMapping("/pupdate/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> update(
            @PathVariable int id,
            @Valid @RequestBody RdPassbook ps) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        rdService.calculateLateFine(ps);

        ps.setPid(id);

        return ResponseEntity.ok(pasrepo.save(ps));
    }
}