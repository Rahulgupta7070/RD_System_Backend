package com.csrd.RDSystemcd.service;

import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Rdrepo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final Rdrepo rdrepo;

    public DashboardService(Rdrepo rdrepo) {
        this.rdrepo = rdrepo;
    }

    public Map<String, Object> getSummary() {
        List<RdUser> users = rdrepo.findAll();

        int totalUsers = users.size();

        double totalDeposit = users.stream()
                .filter(u -> u.getRdAmount() != null)
                .mapToDouble(u -> u.getRdAmount().doubleValue())
                .sum();

        double totalInterest = totalDeposit * 0.05;
        double totalFine = totalUsers * 50;

        long activeAccounts = users.stream()
                .filter(u -> u.getRdAmount() != null && u.getRdAmount().doubleValue() > 0)
                .count();

        long completedAccounts = totalUsers - activeAccounts;

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalDeposit", totalDeposit);
        data.put("totalInterest", totalInterest);
        data.put("totalFine", totalFine);
        data.put("activeAccounts", activeAccounts);
        data.put("completedAccounts", completedAccounts);

        return data;
    }

    public List<Map<String, Object>> getMonthlyCollection() {
        List<RdUser> users = rdrepo.findAll();

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

        for (RdUser u : users) {
            if (u.getRdDate() != null && u.getRdAmount() != null) {
                String month = u.getRdDate().getMonth().toString().substring(0, 3);

                monthlyData.put(
                        month,
                        monthlyData.get(month) + u.getRdAmount().doubleValue()
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