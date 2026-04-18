package com.csrd.RDSystemcd.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.AuditLog;
import com.csrd.RDSystemcd.repo.AuditLogRepo;

@Service
public class AuditService {

    private final AuditLogRepo repo;

    public AuditService(AuditLogRepo repo) {
        this.repo = repo;
    }

    public void log(String action, String username, String role,
                    String details, String ip, String agent) {

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setRole(role);
        log.setDetails(details);
        log.setIpAddress(ip);
        log.setUserAgent(agent);
        log.setTimestamp(LocalDateTime.now());

        repo.save(log);
    }
}
