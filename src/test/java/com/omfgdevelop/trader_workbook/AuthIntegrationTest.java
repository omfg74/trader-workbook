package com.omfgdevelop.trader_workbook;

import com.omfgdevelop.trader_workbook.dto.RefreshRequest;
import com.omfgdevelop.trader_workbook.dto.TokenResponse;
import com.omfgdevelop.trader_workbook.dto.UserResponse;
import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    void registerLoginRefreshAndMe() throws Exception {
        String username = uniqueUsername("user");
        String password = "password123";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new com.omfgdevelop.trader_workbook.dto.RegisterRequest(username, password))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"));

        TokenResponse tokens = login(username, password);
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.tokenType()).isEqualTo("Bearer");

        mockMvc.perform(authorized(tokens.accessToken(), get("/auth/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RefreshRequest(tokens.refreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new com.omfgdevelop.trader_workbook.dto.LoginRequest("unknown", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerDuplicateUsernameReturns400() throws Exception {
        String username = uniqueUsername("dup");
        register(username, "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new com.omfgdevelop.trader_workbook.dto.RegisterRequest(username, "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void adminIsSeededOnStartup() throws Exception {
        TokenResponse adminTokens = login("admin", "admin");

        UserResponse me = objectMapper.readValue(
                mockMvc.perform(authorized(adminTokens.accessToken(), get("/auth/me")))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                UserResponse.class
        );

        assertThat(me.role()).isEqualTo(Role.ADMIN);
    }
}
