package com.omfgdevelop.trader_workbook.repository;

import com.omfgdevelop.trader_workbook.entity.Trade;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    // SpEL null-checks avoid PostgreSQL "could not determine data type of parameter" for Instant
    @Query("""
            SELECT t FROM Trade t
            WHERE (:#{#userId == null} = true OR t.user.id = :userId)
              AND (:#{#status == null} = true OR t.status = :status)
              AND (:#{#securityId == null} = true OR t.security.id = :securityId)
              AND (:#{#dateFrom == null} = true OR t.purchaseDate >= :dateFrom)
              AND (:#{#dateTo == null} = true OR t.purchaseDate <= :dateTo)
            ORDER BY t.purchaseDate DESC
            """)
    Page<Trade> findFiltered(
            @Param("userId") Long userId,
            @Param("status") TradeStatus status,
            @Param("securityId") Long securityId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable
    );
}
