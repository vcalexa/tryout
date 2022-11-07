package com.example.repository;

import com.example.data.CommissionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Repository
public interface CommisionsRepository extends CrudRepository<CommissionType, Long> {

    @Query(value = "SELECT SUM(amount) FROM commission_type  WHERE client_id = ?1 AND to_char(date, 'YYYY-MM') = ?2", nativeQuery = true)
    Optional<BigDecimal> getClientSumOfTurnoverPerMonth(int client_id, String yearMonth);

    Optional<CommissionType> findByIdAndDate(int id, Date date);
}
