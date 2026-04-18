package com.csrd.RDSystemcd.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class PasController {

    private final Cdpassbrepo pasrepo;
    private final RdPasService rdService;
    private final Rdrepo userRepo;
    private final EmailService emailService;
    private final SchedulerService schedulerService;
    private final PdfService pdfService;
    private final AuditService auditService;

    public PasController(Cdpassbrepo pasrepo,
                         RdPasService rdService,
                         Rdrepo userRepo,
                         EmailService emailService,
                         SchedulerService schedulerService,
                         PdfService pdfService,
                         AuditService auditService) {

        this.pasrepo = pasrepo;
        this.rdService = rdService;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.schedulerService = schedulerService;
        this.pdfService = pdfService;
        this.auditService = auditService;
    }

    // ================= GET ALL =================
    @GetMapping("/allPassUser")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getAll() {
        return ResponseEntity.ok(pasrepo.findAll());
    }

    // ================= GET BY RID =================
    @GetMapping("/passbook/{rid}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RdPassbook>> getByRid(@PathVariable int rid) {
        return ResponseEntity.ok(pasrepo.findByRid(rid));
    }

    // ================= SAVE =================
    @PostMapping("/psave")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> save(@Valid @RequestBody RdPassbook ps,
                                 HttpServletRequest request) throws Exception {

        RdUser user = userRepo.findById(ps.getRid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RdPassbook> history = pasrepo.findByRid(ps.getRid());

        // ❌ RD COMPLETE BLOCK
        if (history.size() >= user.getTotalMonths()) {
            return ResponseEntity.badRequest()
                    .body("RD completed! No more deposits allowed ❌");
        }

        // ❌ SAME MONTH DUPLICATE BLOCK
        boolean alreadyPaid = pasrepo.existsByRidAndMonth(
                user.getRid(),
                ps.getRdDate().getMonthValue(),
                ps.getRdDate().getYear()
        );

        if (alreadyPaid) {
            return ResponseEntity.badRequest()
                    .body("Already deposited for this month ❌");
        }

        // 🔥 LATE FINE FIXED LOGIC
        rdService.calculateLateFine(ps, user, history);

        // ✅ STATUS
        if (history.size() + 1 == user.getTotalMonths()) {
            ps.setStatus("COMPLETED");
        }

        RdPassbook saved = pasrepo.save(ps);

        // ================= EMAIL =================
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
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

        // ================= PDF CHECK =================
        schedulerService.checkAndSendPdf(ps.getRid());

        // ================= AUDIT =================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
                "ADD_DEPOSIT",
                username,
                role,
                "User: " + user.getName() +
                " | Amount: " + saved.getRdAmount() +
                " | Date: " + saved.getRdDate() +
                " | Status: " + saved.getStatus() +
                " | Fine: " + saved.getFineAmount(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ================= DELETE =================
    @DeleteMapping("/pdelete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable int id,
                                         HttpServletRequest request) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Not found with id : " + id);
        }

        pasrepo.deleteById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
                "DELETE_DEPOSIT",
                username,
                role,
                "Deleted deposit ID: " + id,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.ok("Deleted successfully");
    }

    // ================= UPDATE =================
    @PutMapping("/pupdate/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable int id,
                                   @Valid @RequestBody RdPassbook ps,
                                   HttpServletRequest request) {

        if (!pasrepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        RdUser user = userRepo.findById(ps.getRid())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<RdPassbook> history = pasrepo.findByRid(ps.getRid())
                .stream()
                .sorted((a, b) -> a.getRdDate().compareTo(b.getRdDate()))
                .toList();

        rdService.calculateLateFine(ps, user, history);

        ps.setPid(id);

        RdPassbook updated = pasrepo.save(ps);

        // ================= AUDIT =================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        auditService.log(
                "UPDATE_DEPOSIT",
                username,
                role,
                "Updated ID: " + id +
                " | Amount: " + updated.getRdAmount() +
                " | Status: " + updated.getStatus(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.ok(updated);
    }

    // ================= PDF =================
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