package com.csrd.RDSystemcd.service;

import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.repo.Rdrepo;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final Rdrepo rdrepo;
    private final Cdpassbrepo passbookRepo;
    private final SchedulerService schedulerService;

    public DashboardService(Rdrepo rdrepo,
                            Cdpassbrepo passbookRepo,
                            SchedulerService schedulerService) {
        this.rdrepo = rdrepo;
        this.passbookRepo = passbookRepo;
        this.schedulerService = schedulerService;
    }

    // ================= DASHBOARD SUMMARY =================
    public Map<String, Object> getSummary() {

        List<RdUser> users = rdrepo.findAll();
        List<RdPassbook> passbooks = passbookRepo.findAll();

        int totalUsers = users.size();

        // ✅ TOTAL DEPOSIT
        double totalDeposit = passbooks.stream()
                .filter(p -> p.getRdAmount() != null)
                .mapToDouble(p -> p.getRdAmount().doubleValue())
                .sum();

        // ✅ TOTAL MATURITY
        double totalMaturity = 0;
        for (RdUser u : users) {
            totalMaturity += schedulerService.getMaturity(u.getRid());
        }

        // ✅ INTEREST
        double totalInterest = totalMaturity - totalDeposit;

        // ✅ TOTAL FINE (NULL SAFE)
        double totalFine = passbooks.stream()
                .mapToDouble(p -> p.getFineAmount() == null ? 0 : p.getFineAmount())
                .sum();

        // ✅ ACTIVE & COMPLETED LOGIC FIX
        long completedAccounts = users.stream()
                .filter(u -> passbookRepo.findByRid(u.getRid()).size() == u.getTotalMonths())
                .count();

        long activeAccounts = totalUsers - completedAccounts;

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalDeposit", totalDeposit);
        data.put("totalInterest", Math.round(totalInterest * 100.0) / 100.0);
        data.put("totalFine", totalFine);
        data.put("activeAccounts", activeAccounts);
        data.put("completedAccounts", completedAccounts);

        return data;
    }

    // ================= MONTHLY COLLECTION =================
    public List<Map<String, Object>> getMonthlyCollection() {

        List<RdPassbook> passbooks = passbookRepo.findAll();

        Map<String, Double> monthlyData = new LinkedHashMap<>();

        String[] months = {
                "JAN","FEB","MAR","APR","MAY","JUN",
                "JUL","AUG","SEP","OCT","NOV","DEC"
        };

        for (String m : months) {
            monthlyData.put(m, 0.0);
        }

        for (RdPassbook p : passbooks) {

            if (p.getRdDate() != null && p.getRdAmount() != null) {

                String month = p.getRdDate()
                        .getMonth()
                        .toString()
                        .substring(0, 3)
                        .toUpperCase();

                monthlyData.put(
                        month,
                        monthlyData.get(month) + p.getRdAmount().doubleValue()
                );
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {

            Map<String, Object> map = new HashMap<>();
            map.put("month", entry.getKey());
            map.put("amount", Math.round(entry.getValue() * 100.0) / 100.0);

            result.add(map);
        }

        return result;
    }
}