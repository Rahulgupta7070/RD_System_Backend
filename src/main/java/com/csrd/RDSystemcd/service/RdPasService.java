package com.csrd.RDSystemcd.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;

@Service
public class RdPasService {

public void calculateLateFine(RdPassbook passbook,
                              RdUser user,
                              List<RdPassbook> history) {

    LocalDate paymentDate = passbook.getRdDate();
    LocalDate startDate = user.getRdDate();

    int installmentIndex = history.size();

    LocalDate expectedMonth = startDate.plusMonths(installmentIndex);

    int dueDay = Math.min(startDate.getDayOfMonth(), expectedMonth.lengthOfMonth());

    LocalDate dueDate = LocalDate.of(
            expectedMonth.getYear(),
            expectedMonth.getMonth(),
            dueDay
    );

    // 👉 Grace Period (2 days)
    int GRACE_DAYS = 2;

    if (paymentDate.isAfter(dueDate)) {

        long totalLateDays = ChronoUnit.DAYS.between(dueDate, paymentDate);

        // 👉 grace apply
        long chargeableDays = totalLateDays - GRACE_DAYS;

        if (chargeableDays > 0) {
            int fine = (int) (chargeableDays * 10);

            passbook.setLateDay((int) totalLateDays);
            passbook.setFineAmount(fine);
            passbook.setStatus("LATE");
        } else {
            // 👉 within grace period
            passbook.setLateDay((int) totalLateDays);
            passbook.setFineAmount(0);
            passbook.setStatus("PAID");
        }

    } else {
        passbook.setLateDay(0);
        passbook.setFineAmount(0);
        passbook.setStatus("PAID");
    }
}
}