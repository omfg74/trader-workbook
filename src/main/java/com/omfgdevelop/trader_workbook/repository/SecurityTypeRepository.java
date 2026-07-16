package com.omfgdevelop.trader_workbook.repository;

import com.omfgdevelop.trader_workbook.entity.SecurityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityTypeRepository extends JpaRepository<SecurityType, Long> {

    Optional<SecurityType> findByName(String name);

    boolean existsByName(String name);
}
