package com.omfgdevelop.trader_workbook.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.omfgdevelop.trader_workbook.dto.LoginRequest;
import com.omfgdevelop.trader_workbook.dto.RegisterRequest;
import com.omfgdevelop.trader_workbook.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    protected MockMvc mockMvc;

    protected String uniqueUsername(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    protected TokenResponse registerAndLogin(String username, String password) throws Exception {
        register(username, password);
        return login(username, password);
    }

    protected void register(String username, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(username, password))))
                .andExpect(status().isCreated());
    }

    protected TokenResponse login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
    }

    protected MockHttpServletRequestBuilder authorized(String accessToken, MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + accessToken);
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
