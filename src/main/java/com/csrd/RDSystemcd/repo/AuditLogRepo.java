package com.csrd.RDSystemcd.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.csrd.RDSystemcd.entity.AuditLog;

import jakarta.transaction.Transactional;

public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findTop10ByOrderByTimestampDesc();
	List<AuditLog> findByAction(String action);
	List<AuditLog> findByUsername(String username);
	List<AuditLog> findByUsernameAndAction(String username, String action);
	
	@Modifying
	@Transactional
	void deleteByTimestampAfter(LocalDateTime date);
}