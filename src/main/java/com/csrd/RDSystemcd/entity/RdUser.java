package com.csrd.RDSystemcd.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "rd_user")
public class RdUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rid;

    private String name;
    private String address;
    private LocalDate dob;
    private String gender;
    
    @jakarta.validation.constraints.Email(message = "Invalid email")
    @NotBlank(message = "Email required")
    private String email;

    private LocalDate rdDate;
    private BigDecimal rdAmount;

    private String occupation;
    private String accountNumber;
    private String aadharNo;
    private String panNo;

    private String nomineeName;
    private String nomineeAddress;
    private String nomineeAadharNo;


    // getters setters

    public int getRid() { return rid; }
    public void setRid(int rid) { this.rid = rid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public LocalDate getRdDate() { return rdDate; }
    public void setRdDate(LocalDate rdDate) { this.rdDate = rdDate; }

    public BigDecimal getRdAmount() { return rdAmount; }
    public void setRdAmount(BigDecimal rdAmount) { this.rdAmount = rdAmount; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAadharNo() { return aadharNo; }
    public void setAadharNo(String aadharNo) { this.aadharNo = aadharNo; }

    public String getPanNo() { return panNo; }
    public void setPanNo(String panNo) { this.panNo = panNo; }

    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }

    public String getNomineeAddress() { return nomineeAddress; }
    public void setNomineeAddress(String nomineeAddress) { this.nomineeAddress = nomineeAddress; }

    public String getNomineeAadharNo() { return nomineeAadharNo; }
    public void setNomineeAadharNo(String nomineeAadharNo) { this.nomineeAadharNo = nomineeAadharNo; }
}