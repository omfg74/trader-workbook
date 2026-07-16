package com.omfgdevelop.trader_workbook;

import com.omfgdevelop.trader_workbook.dto.IssuerResponse;
import com.omfgdevelop.trader_workbook.dto.ReferenceRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityRequest;
import com.omfgdevelop.trader_workbook.dto.SecurityResponse;
import com.omfgdevelop.trader_workbook.dto.SecurityTypeResponse;
import com.omfgdevelop.trader_workbook.dto.TokenResponse;
import com.omfgdevelop.trader_workbook.dto.TradeCreateRequest;
import com.omfgdevelop.trader_workbook.dto.TradeUpdateRequest;
import com.omfgdevelop.trader_workbook.dto.UpdateUserRoleRequest;
import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.entity.Side;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import com.omfgdevelop.trader_workbook.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TradeIntegrationTest extends IntegrationTestSupport {

    private String adminToken;
    private String userToken;
    private String otherUserToken;
    private Long securityId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin", "admin").accessToken();
        userToken = registerAndLogin(uniqueUsername("trader"), "password123").accessToken();
        otherUserToken = registerAndLogin(uniqueUsername("other"), "password123").accessToken();
        securityId = createTestSecurity();
    }

    @Test
    void createGetUpdateAndDeleteTrade() throws Exception {
        String createResponse = mockMvc.perform(authorized(userToken, post("/api/v1/trades"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TradeCreateRequest(
                                securityId,
                                Side.BUY,
                                new BigDecimal("100.00"),
                                new BigDecimal("1.50"),
                                new BigDecimal("0.50"),
                                "test buy",
                                Instant.parse("2026-01-15T10:00:00Z")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.pnl").isEmpty())
                .andExpect(jsonPath("$.securityTicker").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tradeId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(authorized(userToken, get("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("test buy"));

        mockMvc.perform(authorized(userToken, patch("/api/v1/trades/{id}", tradeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TradeUpdateRequest(
                                null, null, null, null, null,
                                "updated comment", null,
                                new BigDecimal("110.00"),
                                Instant.parse("2026-02-01T12:00:00Z"),
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.pnl").value(8.0))
                .andExpect(jsonPath("$.comment").value("updated comment"));

        mockMvc.perform(authorized(userToken, patch("/api/v1/trades/{id}", tradeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TradeUpdateRequest(
                                null, null, null, null, null,
                                null, null, null, null, true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.pnl").isEmpty());

        mockMvc.perform(authorized(userToken, delete("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(userToken, get("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersTrade() throws Exception {
        Long tradeId = createTrade(userToken);

        mockMvc.perform(authorized(otherUserToken, get("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(otherUserToken, patch("/api/v1/trades/{id}", tradeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewAllTradesButNotModifyOthers() throws Exception {
        Long tradeId = createTrade(userToken);

        mockMvc.perform(authorized(adminToken, get("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isOk());

        mockMvc.perform(authorized(adminToken, get("/api/v1/trades"))
                        .param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(authorized(adminToken, patch("/api/v1/trades/{id}", tradeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(adminToken, delete("/api/v1/trades/{id}", tradeId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void listTradesWithStatusFilter() throws Exception {
        Long openTradeId = createTrade(userToken);

        mockMvc.perform(authorized(userToken, patch("/api/v1/trades/{id}", openTradeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TradeUpdateRequest(
                                null, null, null, null, null,
                                null, null,
                                new BigDecimal("105.00"),
                                Instant.parse("2026-03-01T10:00:00Z"),
                                null
                        ))))
                .andExpect(status().isOk());

        createTrade(userToken);

        mockMvc.perform(authorized(userToken, get("/api/v1/trades"))
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.status == 'OPEN')]").exists())
                .andExpect(jsonPath("$.content[?(@.status == 'CLOSED')]").doesNotExist());
    }

    private Long createTrade(String token) throws Exception {
        String response = mockMvc.perform(authorized(token, post("/api/v1/trades"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TradeCreateRequest(
                                securityId,
                                Side.BUY,
                                new BigDecimal("50.00"),
                                new BigDecimal("1.00"),
                                new BigDecimal("1.00"),
                                null,
                                Instant.parse("2026-01-01T00:00:00Z")
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createTestSecurity() throws Exception {
        SecurityTypeResponse type = objectMapper.readValue(
                mockMvc.perform(authorized(adminToken, post("/api/v1/admin/security-types"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new ReferenceRequest("Stock_" + System.nanoTime(), "Equity"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SecurityTypeResponse.class
        );

        IssuerResponse issuer = objectMapper.readValue(
                mockMvc.perform(authorized(adminToken, post("/api/v1/admin/issuers"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new ReferenceRequest("Issuer_" + System.nanoTime(), null))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                IssuerResponse.class
        );

        SecurityResponse security = objectMapper.readValue(
                mockMvc.perform(authorized(adminToken, post("/api/v1/admin/securities"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new SecurityRequest(
                                        "GAZP_" + System.nanoTime(),
                                        "Gazprom",
                                        type.id(),
                                        issuer.id(),
                                        null
                                ))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SecurityResponse.class
        );

        return security.id();
    }
}
