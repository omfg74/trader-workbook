package com.omfgdevelop.trader_workbook.service;

import com.omfgdevelop.trader_workbook.dto.IssuerResponse;
import com.omfgdevelop.trader_workbook.dto.ReferenceRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityResponse;
import com.omfgdevelop.trader_workbook.dto.SecurityTypeResponse;
import com.omfgdevelop.trader_workbook.entity.Issuer;
import com.omfgdevelop.trader_workbook.entity.Security;
import com.omfgdevelop.trader_workbook.entity.SecurityType;
import com.omfgdevelop.trader_workbook.exception.BadRequestException;
import com.omfgdevelop.trader_workbook.exception.NotFoundException;
import com.omfgdevelop.trader_workbook.repository.IssuerRepository;
import com.omfgdevelop.trader_workbook.repository.SecurityRepository;
import com.omfgdevelop.trader_workbook.repository.SecurityTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final SecurityTypeRepository securityTypeRepository;
    private final IssuerRepository issuerRepository;
    private final SecurityRepository securityRepository;

    public List<SecurityTypeResponse> listSecurityTypes() {
        return securityTypeRepository.findAll().stream().map(this::toSecurityTypeResponse).toList();
    }

    @Transactional
    public SecurityTypeResponse createSecurityType(ReferenceRequest request) {
        if (securityTypeRepository.existsByName(request.name())) {
            throw new BadRequestException("Security type already exists");
        }
        SecurityType entity = new SecurityType();
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toSecurityTypeResponse(securityTypeRepository.save(entity));
    }

    @Transactional
    public SecurityTypeResponse updateSecurityType(Long id, ReferenceRequest request) {
        SecurityType entity = securityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Security type not found"));
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toSecurityTypeResponse(securityTypeRepository.save(entity));
    }

    @Transactional
    public void deleteSecurityType(Long id) {
        if (!securityTypeRepository.existsById(id)) {
            throw new NotFoundException("Security type not found");
        }
        if (securityRepository.existsBySecurityTypeId(id)) {
            throw new BadRequestException("Cannot delete security type with linked securities");
        }
        securityTypeRepository.deleteById(id);
    }

    public List<IssuerResponse> listIssuers() {
        return issuerRepository.findAll().stream().map(this::toIssuerResponse).toList();
    }

    @Transactional
    public IssuerResponse createIssuer(ReferenceRequest request) {
        if (issuerRepository.existsByName(request.name())) {
            throw new BadRequestException("Issuer already exists");
        }
        Issuer entity = new Issuer();
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toIssuerResponse(issuerRepository.save(entity));
    }

    @Transactional
    public IssuerResponse updateIssuer(Long id, ReferenceRequest request) {
        Issuer entity = issuerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Issuer not found"));
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toIssuerResponse(issuerRepository.save(entity));
    }

    @Transactional
    public void deleteIssuer(Long id) {
        if (!issuerRepository.existsById(id)) {
            throw new NotFoundException("Issuer not found");
        }
        if (securityRepository.existsByIssuerId(id)) {
            throw new BadRequestException("Cannot delete issuer with linked securities");
        }
        issuerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SecurityResponse> listSecurities() {
        return listSecurities(null);
    }

    @Transactional(readOnly = true)
    public List<SecurityResponse> listSecurities(String q) {
        List<Security> securities = (q == null || q.isBlank())
                ? securityRepository.findAllWithRelations()
                : securityRepository.searchByTickerOrName(q.trim());
        return securities.stream().map(this::toSecurityResponse).toList();
    }

    @Transactional
    public SecurityResponse createSecurity(SecurityRequest request) {
        Security entity = new Security();
        applySecurityFields(entity, request);
        return toSecurityResponse(securityRepository.save(entity));
    }

    @Transactional
    public SecurityResponse updateSecurity(Long id, SecurityRequest request) {
        Security entity = securityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Security not found"));
        applySecurityFields(entity, request);
        return toSecurityResponse(securityRepository.save(entity));
    }

    @Transactional
    public void deleteSecurity(Long id) {
        if (!securityRepository.existsById(id)) {
            throw new NotFoundException("Security not found");
        }
        securityRepository.deleteById(id);
    }

    public Security getSecurityOrThrow(Long id) {
        return securityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Security not found"));
    }

    private void applySecurityFields(Security entity, SecurityRequest request) {
        SecurityType type = securityTypeRepository.findById(request.typeId())
                .orElseThrow(() -> new NotFoundException("Security type not found"));
        entity.setTicker(request.ticker());
        entity.setName(request.name());
        entity.setSecurityType(type);
        entity.setDescription(request.description());

        if (request.issuerId() != null) {
            Issuer issuer = issuerRepository.findById(request.issuerId())
                    .orElseThrow(() -> new NotFoundException("Issuer not found"));
            entity.setIssuer(issuer);
        } else {
            entity.setIssuer(null);
        }
    }

    private SecurityTypeResponse toSecurityTypeResponse(SecurityType entity) {
        return new SecurityTypeResponse(entity.getId(), entity.getName(), entity.getDescription());
    }

    private IssuerResponse toIssuerResponse(Issuer entity) {
        return new IssuerResponse(entity.getId(), entity.getName(), entity.getDescription());
    }

    private SecurityResponse toSecurityResponse(Security entity) {
        return new SecurityResponse(
                entity.getId(),
                entity.getTicker(),
                entity.getName(),
                entity.getSecurityType().getId(),
                entity.getSecurityType().getName(),
                entity.getIssuer() != null ? entity.getIssuer().getId() : null,
                entity.getIssuer() != null ? entity.getIssuer().getName() : null,
                entity.getDescription()
        );
    }
}
