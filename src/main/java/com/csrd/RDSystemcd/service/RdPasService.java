package com.csrd.RDSystemcd.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;


@Service
public class RdPasService {

    public void calculateLateFine(RdPassbook passbook) {

        LocalDate paymentDate = passbook.getRdDate();

        LocalDate dueDate = LocalDate.of(
            paymentDate.getYear(),
            paymentDate.getMonth(),
            5
        );

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