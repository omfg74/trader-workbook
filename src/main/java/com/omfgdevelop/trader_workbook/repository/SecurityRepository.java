package com.omfgdevelop.trader_workbook.repository;

import com.omfgdevelop.trader_workbook.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    boolean existsBySecurityTypeId(Long securityTypeId);

    boolean existsByIssuerId(Long issuerId);

    @Query("""
            SELECT DISTINCT s FROM Security s
            JOIN FETCH s.securityType
            LEFT JOIN FETCH s.issuer
            """)
    List<Security> findAllWithRelations();

    @Query("""
            SELECT DISTINCT s FROM Security s
            JOIN FETCH s.securityType
            LEFT JOIN FETCH s.issuer
            WHERE LOWER(s.ticker) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Security> searchByTickerOrName(@Param("q") String q);
}
