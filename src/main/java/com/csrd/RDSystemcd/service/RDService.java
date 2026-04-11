package com.csrd.RDSystemcd.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdUser;
import com.csrd.RDSystemcd.repo.Rdrepo;

@Service
public class RDService {

    private final Rdrepo rdrepo;

    //  Constructor injection
    public RDService(Rdrepo rdrepo) {
        this.rdrepo = rdrepo;
    }

  public Page<RdUser> filterUsers(
        String startDate,
        String endDate,
        Double minAmount,
        Double maxAmount,
        Pageable pageable) {

    return rdrepo.findAll((root, query, cb) -> {

        List<Predicate> predicates = new ArrayList<>();

        // Date filter
        if (startDate != null && endDate != null &&
            !startDate.isEmpty() && !endDate.isEmpty()) {

            predicates.add(cb.between(
                    root.get("rdDate"),
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            ));
        }

        //  Amount filter
        if (minAmount != null && maxAmount != null) {

            predicates.add(cb.between(
                    root.get("rdAmount"),
                    BigDecimal.valueOf(minAmount),
                    BigDecimal.valueOf(maxAmount)
            ));
        }

        return cb.and(predicates.toArray(new Predicate[0]));

    }, pageable);
}
}