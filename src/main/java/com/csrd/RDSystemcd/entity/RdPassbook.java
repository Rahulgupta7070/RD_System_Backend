package com.csrd.RDSystemcd.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "rd_passbook")
public class RdPassbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pid;

    @Column(name = "rid")
    private int rid;

    // ❌ @NotNull हटाया (backend set करेगा)
    @Column(name = "rd_date")
    private LocalDate rdDate;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "1", message = "Amount must be > 0")
    @Column(name = "rd_amount")
    private BigDecimal rdAmount;

    @Min(value = 0, message = "Late days cannot be negative")
    @Column(name = "late_day")
    private Integer lateDay;

   

    @Min(value = 0, message = "Fine cannot be negative")
    @Column(name = "fine_amount")
    private Integer fineAmount;

    // ❌ @NotBlank हटाया (backend set करेगा)
    private String status;

    // ===== GETTERS & SETTERS =====

    public int getPid() { return pid; }
    public void setPid(int pid) { this.pid = pid; }

    public int getRid() { return rid; }
    public void setRid(int rid) { this.rid = rid; }

    public LocalDate getRdDate() { return rdDate; }
    public void setRdDate(LocalDate rdDate) { this.rdDate = rdDate; }

    public BigDecimal getRdAmount() { return rdAmount; }
    public void setRdAmount(BigDecimal rdAmount) { this.rdAmount = rdAmount; }

    public Integer getLateDay() { return lateDay; }
    public void setLateDay(Integer lateDay) { this.lateDay = lateDay; }

    

    public Integer getFineAmount() { return fineAmount; }
    public void setFineAmount(Integer fineAmount) { this.fineAmount = fineAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}