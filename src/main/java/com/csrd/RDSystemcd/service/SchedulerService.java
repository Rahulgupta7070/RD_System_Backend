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
    private final EmailService emailService;
    private final PdfService pdfService; // ✅ ADD

    public SchedulerService(Rdrepo rdrepo,
                            Cdpassbrepo passrepo,
                            EmailService emailService,
                            PdfService pdfService) {

        this.rdrepo = rdrepo;
        this.passrepo = passrepo;
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    // ================= MONTHLY DEPOSIT =================
    public void runMonthlyDeposit() {

        List<RdUser> users = rdrepo.findAll();

        for (RdUser user : users) {

            if (user.getRdAmount() == null) continue;

            RdPassbook entry = new RdPassbook();
            entry.setRid(user.getRid());
            entry.setRdAmount(user.getRdAmount());
            entry.setRdDate(LocalDate.now());
            entry.setStatus("PAID");

            passrepo.save(entry);
        }
    }

    // ================= CHECK + SEND PDF =================
    public void checkAndSendPdf(int rid) throws Exception {

        RdUser user = rdrepo.findById(rid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RdPassbook> list = passrepo.findByRid(rid);

        // ✅ only when completed
        if (list.size() == user.getTotalMonths()) {

            double total = list.stream()
                    .mapToDouble(p -> p.getRdAmount().doubleValue())
                    .sum();

            double maturity = getMaturity(rid); // ✅ FIXED

            byte[] pdf = pdfService.generatePdf(user, list, total, maturity);

            emailService.sendPdf(user.getEmail(), pdf);
        }
    }

    // ================= RD FORMULA =================
    public double calculateMaturity(double P, int n, double r) {
        return P * n +
               (P * n * (n + 1) / 2.0) * (r / (12 * 100));
    }

    // ================= FINAL MATURITY =================
    public double getMaturity(int rid) {

        List<RdPassbook> list = passrepo.findByRid(rid);
        if (list.isEmpty()) return 0;

        int n = list.size();

        double total = list.stream()
                .mapToDouble(p -> p.getRdAmount().doubleValue())
                .sum();

        double P = total / n;

        double rate = 7;

        double maturity = P * n +
                (P * n * (n + 1) / 2.0) * (rate / (12 * 100));

        return Math.round(maturity * 100.0) / 100.0;
    }
}