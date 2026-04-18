
package com.csrd.RDSystemcd.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csrd.RDSystemcd.entity.AuditLog;
import com.csrd.RDSystemcd.repo.AuditLogRepo;

@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditController {

    private final AuditLogRepo repo;

    public AuditController(AuditLogRepo repo) {
        this.repo = repo;
    }

    // ================= ALL LOGS =================
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<AuditLog> getAllLogs() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    // ================= LATEST 10 =================
    @GetMapping("/latest")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<AuditLog> getLatestLogs() {
        return repo.findTop10ByOrderByTimestampDesc();
    }

    // ================= FILTER BY ACTION =================
    @GetMapping("/action")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<AuditLog> getByAction(@RequestParam String action) {
        return repo.findByAction(action);
    }

    // ================= FILTER BY USER =================
    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<AuditLog> getByUser(@RequestParam String username) {
        return repo.findByUsername(username);
    }

    // ================= SEARCH (USER + ACTION) =================
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<AuditLog> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action) {

        if (username != null && action != null) {
            return repo.findByUsernameAndAction(username, action);
        } else if (username != null) {
            return repo.findByUsername(username);
        } else if (action != null) {
            return repo.findByAction(action);
        } else {
            return repo.findAll();
        }
    }
    
   
@DeleteMapping("/delete-range/{range}")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public ResponseEntity<?> deleteByRange(@PathVariable String range) {

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime fromDate;

    switch (range) {
        case "7":
            fromDate = now.minusDays(7);
            break;
        case "15":
            fromDate = now.minusDays(15);
            break;
        case "30":
            fromDate = now.minusDays(30);
            break;
        case "today":
            fromDate = now.toLocalDate().atStartOfDay();
            break;
        case "all":
            repo.deleteAll();
            return ResponseEntity.ok("All logs deleted");
        default:
            return ResponseEntity.badRequest().body("Invalid range");
    }

    // ✅ Correct deletion
    repo.deleteByTimestampAfter(fromDate);

    return ResponseEntity.ok("Selected range logs deleted");
}
}

