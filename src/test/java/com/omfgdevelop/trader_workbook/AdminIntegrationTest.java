package com.omfgdevelop.trader_workbook;

import com.omfgdevelop.trader_workbook.dto.ReferenceRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityRequest;
import com.omfgdevelop.trader_workbook.dto.UpdateUserRoleRequest;
import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminIntegrationTest extends IntegrationTestSupport {

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin", "admin").accessToken();
        userToken = registerAndLogin(uniqueUsername("regular"), "password123").accessToken();
    }

    @Test
    void adminCrudForReferences() throws Exception {
        String typeResponse = mockMvc.perform(authorized(adminToken, post("/api/v1/admin/security-types"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReferenceRequest("Bond", "Debt instrument"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bond"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long typeId = objectMapper.readTree(typeResponse).get("id").asLong();

        mockMvc.perform(authorized(adminToken, put("/api/v1/admin/security-types/{id}", typeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReferenceRequest("Bond Updated", "Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bond Updated"));

        String issuerResponse = mockMvc.perform(authorized(adminToken, post("/api/v1/admin/issuers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReferenceRequest("MOEX", "Exchange"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long issuerId = objectMapper.readTree(issuerResponse).get("id").asLong();

        String securityResponse = mockMvc.perform(authorized(adminToken, post("/api/v1/admin/securities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SecurityRequest("SBER", "Sberbank", typeId, issuerId, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.typeName").value("Bond Updated"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long securityId = objectMapper.readTree(securityResponse).get("id").asLong();

        mockMvc.perform(authorized(adminToken, get("/api/v1/admin/securities")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.ticker == 'SBER')]").exists());

        mockMvc.perform(authorized(adminToken, delete("/api/v1/admin/security-types/{id}", typeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete security type with linked securities"));

        mockMvc.perform(authorized(adminToken, delete("/api/v1/admin/securities/{id}", securityId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(adminToken, delete("/api/v1/admin/issuers/{id}", issuerId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(adminToken, delete("/api/v1/admin/security-types/{id}", typeId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCanListUsersAndChangeRole() throws Exception {
        String username = uniqueUsername("promote");
        register(username, "password123");

        String usersResponse = mockMvc.perform(authorized(adminToken, get("/api/v1/admin/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == '" + username + "')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var users = objectMapper.readTree(usersResponse);
        Long userId = null;
        for (var user : users) {
            if (username.equals(user.get("username").asText())) {
                userId = user.get("id").asLong();
                break;
            }
        }
        if (userId == null) {
            throw new IllegalStateException("User not found: " + username);
        }

        mockMvc.perform(authorized(adminToken, patch("/api/v1/admin/users/{id}", userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UpdateUserRoleRequest(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void regularUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(authorized(userToken, get("/api/v1/admin/users")))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(userToken, post("/api/v1/admin/security-types"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReferenceRequest("ETF", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanReadPublicReferences() throws Exception {
        mockMvc.perform(authorized(adminToken, post("/api/v1/admin/security-types"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReferenceRequest("ETF_" + System.nanoTime(), "Fund"))))
                .andExpect(status().isCreated());

        mockMvc.perform(authorized(userToken, get("/api/v1/security-types")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(authorized(userToken, get("/api/v1/securities"))
                        .param("q", "ETF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
