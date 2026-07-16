package com.omfgdevelop.trader_workbook.repository;

import com.omfgdevelop.trader_workbook.entity.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssuerRepository extends JpaRepository<Issuer, Long> {

    Optional<Issuer> findByName(String name);

    boolean existsByName(String name);
}
