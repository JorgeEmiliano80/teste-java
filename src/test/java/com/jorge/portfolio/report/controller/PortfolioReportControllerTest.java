package com.jorge.portfolio.report.controller;

import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.report.dto.PortfolioSummaryResponse;
import com.jorge.portfolio.report.dto.StatusBudgetSummaryResponse;
import com.jorge.portfolio.report.service.PortfolioReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortfolioReportControllerTest {

    private final PortfolioReportService portfolioReportService = mock(PortfolioReportService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PortfolioReportController(portfolioReportService))
                .build();
    }

    @Test
    void shouldGetPortfolioSummary() throws Exception {
        PortfolioSummaryResponse response = new PortfolioSummaryResponse(
                List.of(new StatusBudgetSummaryResponse(
                        ProjectStatus.EM_ANALISE,
                        2L,
                        new BigDecimal("300000.00")
                )),
                new BigDecimal("300000.00"),
                new BigDecimal("30.00"),
                4L
        );

        when(portfolioReportService.generatePortfolioSummary()).thenReturn(response);

        mockMvc.perform(get("/api/reports/portfolio-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusSummaries", hasSize(1)))
                .andExpect(jsonPath("$.statusSummaries[0].status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.statusSummaries[0].projectCount").value(2L))
                .andExpect(jsonPath("$.statusSummaries[0].totalBudget").value(300000.00))
                .andExpect(jsonPath("$.totalBudget").value(300000.00))
                .andExpect(jsonPath("$.averageClosedProjectDurationInDays").value(30.00))
                .andExpect(jsonPath("$.uniqueAllocatedMembers").value(4L));

        verify(portfolioReportService).generatePortfolioSummary();
    }
}
