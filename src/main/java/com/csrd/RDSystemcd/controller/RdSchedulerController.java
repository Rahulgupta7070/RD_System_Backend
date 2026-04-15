package com.csrd.RDSystemcd.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.service.SchedulerService;

@RestController
@RequestMapping("/scheduler")
@CrossOrigin(origins = "http://localhost:5173")
public class RdSchedulerController {

    private final SchedulerService service;

    public RdSchedulerController(SchedulerService service) {
        this.service = service;
    }

    // 💰 MATURITY
    @GetMapping("/maturity/{rid}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public double getMaturity(@PathVariable int rid) {
        return service.getMaturity(rid);
    }

    // 🧮 CALCULATOR
    @GetMapping("/calculate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public double calculate(
            @RequestParam double amount,
            @RequestParam int months,
            @RequestParam double rate) {

        return service.calculateMaturity(amount, months, rate);
    }
}