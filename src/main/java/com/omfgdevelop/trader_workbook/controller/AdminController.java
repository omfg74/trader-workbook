package com.omfgdevelop.trader_workbook.controller;

import com.omfgdevelop.trader_workbook.dto.IssuerResponse;
import com.omfgdevelop.trader_workbook.dto.ReferenceRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityResponse;
import com.omfgdevelop.trader_workbook.dto.SecurityTypeResponse;
import com.omfgdevelop.trader_workbook.dto.UpdateUserRoleRequest;
import com.omfgdevelop.trader_workbook.dto.UserResponse;
import com.omfgdevelop.trader_workbook.service.ReferenceService;
import com.omfgdevelop.trader_workbook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ReferenceService referenceService;
    private final UserService userService;

    @GetMapping("/security-types")
    public List<SecurityTypeResponse> listSecurityTypes() {
        return referenceService.listSecurityTypes();
    }

    @PostMapping("/security-types")
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityTypeResponse createSecurityType(@Valid @RequestBody ReferenceRequest request) {
        return referenceService.createSecurityType(request);
    }

    @PutMapping("/security-types/{id}")
    public SecurityTypeResponse updateSecurityType(@PathVariable Long id, @Valid @RequestBody ReferenceRequest request) {
        return referenceService.updateSecurityType(id, request);
    }

    @DeleteMapping("/security-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecurityType(@PathVariable Long id) {
        referenceService.deleteSecurityType(id);
    }

    @GetMapping("/issuers")
    public List<IssuerResponse> listIssuers() {
        return referenceService.listIssuers();
    }

    @PostMapping("/issuers")
    @ResponseStatus(HttpStatus.CREATED)
    public IssuerResponse createIssuer(@Valid @RequestBody ReferenceRequest request) {
        return referenceService.createIssuer(request);
    }

    @PutMapping("/issuers/{id}")
    public IssuerResponse updateIssuer(@PathVariable Long id, @Valid @RequestBody ReferenceRequest request) {
        return referenceService.updateIssuer(id, request);
    }

    @DeleteMapping("/issuers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIssuer(@PathVariable Long id) {
        referenceService.deleteIssuer(id);
    }

    @GetMapping("/securities")
    public List<SecurityResponse> listSecurities() {
        return referenceService.listSecurities();
    }

    @PostMapping("/securities")
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityResponse createSecurity(@Valid @RequestBody SecurityRequest request) {
        return referenceService.createSecurity(request);
    }

    @PutMapping("/securities/{id}")
    public SecurityResponse updateSecurity(@PathVariable Long id, @Valid @RequestBody SecurityRequest request) {
        return referenceService.updateSecurity(id, request);
    }

    @DeleteMapping("/securities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecurity(@PathVariable Long id) {
        referenceService.deleteSecurity(id);
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @PatchMapping("/users/{id}")
    public UserResponse updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return userService.updateRole(id, request);
    }
}
