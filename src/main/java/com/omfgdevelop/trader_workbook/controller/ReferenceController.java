package com.omfgdevelop.trader_workbook.controller;

import com.omfgdevelop.trader_workbook.dto.IssuerResponse;
import com.omfgdevelop.trader_workbook.dto.SecurityResponse;
import com.omfgdevelop.trader_workbook.dto.SecurityTypeResponse;
import com.omfgdevelop.trader_workbook.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReferenceController {

    private final ReferenceService referenceService;

    @GetMapping("/security-types")
    public List<SecurityTypeResponse> listSecurityTypes() {
        return referenceService.listSecurityTypes();
    }

    @GetMapping("/issuers")
    public List<IssuerResponse> listIssuers() {
        return referenceService.listIssuers();
    }

    @GetMapping("/securities")
    public List<SecurityResponse> listSecurities(@RequestParam(required = false) String q) {
        return referenceService.listSecurities(q);
    }
}
