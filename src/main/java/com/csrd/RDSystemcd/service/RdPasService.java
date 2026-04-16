package com.csrd.RDSystemcd.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;

@Service
public class RdPasService {

    public void calculateLateFine(RdPassbook passbook, RdUser user) {

        LocalDate paymentDate = passbook.getRdDate();

        // 🔥 RD start date (15)
        int rdDay = user.getRdDate().getDayOfMonth();

        // 🔥 current month ka valid due date
        int validDay = Math.min(rdDay, paymentDate.lengthOfMonth());

        LocalDate dueDate = LocalDate.of(
                paymentDate.getYear(),
                paymentDate.getMonth(),
                validDay
        );

        // 🔥 Late check
        if (paymentDate.isAfter(dueDate)) {

            long lateDays = ChronoUnit.DAYS.between(dueDate, paymentDate);

            int fine = (int) lateDays * 10;

            passbook.setLateDay((int) lateDays);
            passbook.setFineAmount(fine);
            passbook.setStatus("LATE");

        } else {
            passbook.setLateDay(0);
            passbook.setFineAmount(0);
            passbook.setStatus("PAID");
        }
    }
}