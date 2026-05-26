package com.example.syfe.controllers;

import com.example.syfe.dtos.responses.MonthlyReportResponse;
import com.example.syfe.dtos.responses.YearlyReportResponse;
import com.example.syfe.services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    @WithMockUser
    void getMonthlyReport_ReturnsOk() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .year(2023)
                .month(10)
                .netSavings(new BigDecimal("1000.00"))
                .build();

        when(reportService.getMonthlyReport(2023, 10)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2023/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.netSavings").value(1000.00));
    }

    @Test
    @WithMockUser
    void getYearlyReport_ReturnsOk() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2023)
                .netSavings(new BigDecimal("12000.00"))
                .build();

        when(reportService.getYearlyReport(2023)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.netSavings").value(12000.00));
    }
}
