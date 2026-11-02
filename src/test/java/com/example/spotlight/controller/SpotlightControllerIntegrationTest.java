package com.example.spotlight.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpotlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldApplySpotlight() throws Exception {
        var request = """
                {
                  "resourceCode": "RES-001",
                  "highlights": ["Premium", "Featured"],
                  "position": 1
                }
                """;

        mockMvc.perform(post("/api/spotlights/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceCode").value("RES-001"))
                .andExpect(jsonPath("$.ownerId").value(100))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        var request = """
                {
                  "resourceCode": null,
                  "highlights": [],
                  "position": 0
                }
                """;

        mockMvc.perform(post("/api/spotlights/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
