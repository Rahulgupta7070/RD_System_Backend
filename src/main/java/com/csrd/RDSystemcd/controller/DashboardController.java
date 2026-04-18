package com.csrd.RDSystemcd.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.csrd.RDSystemcd.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    //  SUMMARY
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public Map<String, Object> getSummary() {
        return dashboardService.getSummary();
    }

    //  MONTHLY COLLECTION
    @GetMapping("/monthly-collection")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public List<Map<String, Object>> getMonthlyCollection() {
        return dashboardService.getMonthlyCollection();
    }
}