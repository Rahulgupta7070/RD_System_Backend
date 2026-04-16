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

        // ✅ TOTAL DEPOSIT (REAL DATA)
        double totalDeposit = passbooks.stream()
                .filter(p -> p.getRdAmount() != null)
                .mapToDouble(p -> p.getRdAmount().doubleValue())
                .sum();

        // ✅ TOTAL MATURITY (VERY IMPORTANT)
        double totalMaturity = users.stream()
                .mapToDouble(u -> schedulerService.getMaturity(u.getRid()))
                .sum();

        // ✅ TOTAL INTEREST (CORRECT)
        double totalInterest = totalMaturity - totalDeposit;

        // ✅ TOTAL FINE
        double totalFine = passbooks.stream()
                .mapToDouble(p -> p.getFineAmount() == null ? 0 : p.getFineAmount())
                .sum();

        // ✅ ACTIVE ACCOUNTS
        long activeAccounts = users.stream()
                .filter(u -> u.getRdAmount() != null && u.getRdAmount().doubleValue() > 0)
                .count();

        // ✅ COMPLETED ACCOUNTS
        long completedAccounts = totalUsers - activeAccounts;

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
        monthlyData.put("JAN", 0.0);
        monthlyData.put("FEB", 0.0);
        monthlyData.put("MAR", 0.0);
        monthlyData.put("APR", 0.0);
        monthlyData.put("MAY", 0.0);
        monthlyData.put("JUN", 0.0);
        monthlyData.put("JUL", 0.0);
        monthlyData.put("AUG", 0.0);
        monthlyData.put("SEP", 0.0);
        monthlyData.put("OCT", 0.0);
        monthlyData.put("NOV", 0.0);
        monthlyData.put("DEC", 0.0);

        for (RdPassbook p : passbooks) {

            if (p.getRdDate() != null && p.getRdAmount() != null) {

                String month = p.getRdDate()
                        .getMonth()
                        .toString()
                        .substring(0, 3);

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
            map.put("amount", entry.getValue());

            result.add(map);
        }

        return result;
    }
}