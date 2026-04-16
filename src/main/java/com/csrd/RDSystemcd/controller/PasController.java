package com.csrd.RDSystemcd.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.service.EmailService;
import com.csrd.RDSystemcd.service.PdfService;
import com.csrd.RDSystemcd.service.RdPasService;
import com.csrd.RDSystemcd.service.SchedulerService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class PasController {

    private final Cdpassbrepo pasrepo;
    private final RdPasService rdService;
    private final Rdrepo userRepo;
    private final EmailService emailService;
    private final SchedulerService schedulerService;
    private final  PdfService pdfService;
    
    
    public PasController(Cdpassbrepo pasrepo,
                         RdPasService rdService,
                         Rdrepo userRepo,
                         EmailService emailService,
                         SchedulerService schedulerService,
                         PdfService pdfService) {
        this.pasrepo = pasrepo;
        this.rdService = rdService;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.schedulerService =schedulerService;
        this.pdfService =pdfService;
    }

    // ================= GET ALL =================
    @GetMapping("/allPassUser")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getAll() {
        return ResponseEntity.ok(pasrepo.findAll());
    }

    // ================= GET BY ID =================
    @GetMapping("/puser/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<RdPassbook> getById(@PathVariable int id) {
        return pasrepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= GET BY RID =================
    @GetMapping("/passbook/{rid}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getByRid(@PathVariable int rid) {
        return ResponseEntity.ok(pasrepo.findByRid(rid));
    }

    // ================= SAVE (MAIN LOGIC) =================
    @PostMapping("/psave")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
   public ResponseEntity<?> save(@Valid @RequestBody RdPassbook ps) throws Exception {

    // 🔥 USER FETCH
    RdUser user = userRepo.findById(ps.getRid())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // 🔥 CHECK RD LIMIT
    List<RdPassbook> list = pasrepo.findByRid(ps.getRid());

    if (list.size() >= user.getTotalMonths()) {
        return ResponseEntity.badRequest()
                .body("RD completed! No more deposits allowed ❌");
    }

    // 🔥 STATUS SET
    if (list.size() + 1 == user.getTotalMonths()) {
        ps.setStatus("COMPLETED");
    } else {
        ps.setStatus("PAID");
    }

    // 🔥 LATE FINE
    rdService.calculateLateFine(ps, user);

    // 🔥 SAVE
    RdPassbook saved = pasrepo.save(ps);

    // 🔥 NORMAL EMAIL
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

    // 🔥🔥 MOST IMPORTANT (ADD THIS)
    schedulerService.checkAndSendPdf(ps.getRid());

    return new ResponseEntity<>(saved, HttpStatus.CREATED);
}

    // ================= DELETE =================
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

    // ================= UPDATE =================
    @PutMapping("/pupdate/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @Valid @RequestBody RdPassbook ps) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // 🔥 USER FETCH
        RdUser user = userRepo.findById(ps.getRid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 LATE FINE CALCULATE
        rdService.calculateLateFine(ps, user);

        ps.setPid(id);

        RdPassbook updated = pasrepo.save(ps);

        // 🔥 EMAIL SEND
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
    
    @GetMapping("/pdf/{rid}")
    public ResponseEntity<byte[]> getPdf(@PathVariable int rid) throws Exception {

        RdUser user = userRepo.findById(rid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RdPassbook> list = pasrepo.findByRid(rid);

        double total = list.stream()
                .mapToDouble(p -> p.getRdAmount().doubleValue())
                .sum();

        double maturity = schedulerService.getMaturity(rid);

        byte[] pdf = pdfService.generatePdf(user, list, total, maturity);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=rd.pdf")
                .body(pdf);
    }
}