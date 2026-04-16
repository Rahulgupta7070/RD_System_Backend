package com.csrd.RDSystemcd.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.repo.Rdrepo;

@Service
public class SchedulerService {

    private final Rdrepo rdrepo;
    private final Cdpassbrepo passrepo;

    private final EmailService emailService;

    public SchedulerService(Rdrepo rdrepo, Cdpassbrepo passrepo, EmailService emailService) {
        this.rdrepo = rdrepo;
        this.passrepo = passrepo;
        this.emailService = emailService;
    }

    // Monthly deposit
    public void runMonthlyDeposit() {

        List<RdUser> users = rdrepo.findAll();

        for (RdUser user : users) {

            if (user.getRdAmount() == null) continue;

            RdPassbook entry = new RdPassbook();
            entry.setRid(user.getRid());
            entry.setRdAmount(user.getRdAmount());
            entry.setRdDate(LocalDate.now());

            //  flag → status
            entry.setStatus("PAID");

            passrepo.save(entry);
        }
    }

    //  Formula
    public double calculateMaturity(double P, int n, double r) {
        return P * n +
               (P * n * (n + 1) / 2.0) * (r / (12 * 100));
    }

    //  FIXED MATURITY
public double getMaturity(int rid) {

    List<RdPassbook> list = passrepo.findByRid(rid);

    if (list.isEmpty()) return 0;

    int n = list.size(); // months count

    double total = list.stream()
            .mapToDouble(p -> p.getRdAmount().doubleValue())
            .sum();

    // assume monthly amount (same deposit every month)
    double P = total / n;

    double rate = 7; // yearly %

    // ✅ RD FORMULA
    double maturity = P * n +
            (P * n * (n + 1) / 2.0) * (rate / (12 * 100));

    return Math.round(maturity * 100.0) / 100.0;
}
}