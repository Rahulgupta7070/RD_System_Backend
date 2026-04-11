package com.csrd.RDSystemcd.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Cdpassbrepo;
import com.csrd.RDSystemcd.repo.Rdrepo;

@Service
public class SchedulerService {

    private final Rdrepo rdrepo;
    private final Cdpassbrepo passrepo;

    public SchedulerService(Rdrepo rdrepo, Cdpassbrepo passrepo) {
        this.rdrepo = rdrepo;
        this.passrepo = passrepo;
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

        double total = 0;
        double interest = 0;

        double rate = 7; // yearly %

        int n = list.size();

        for (int i = 0; i < n; i++) {

            double amount = list.get(i).getRdAmount().doubleValue();

            total += amount;

            // interest for remaining months
            interest += amount * (rate / 100) * ((n - i) / 12.0);
        }

        double maturity = total + interest;

        return Math.round(maturity * 100.0) / 100.0;
    }
}