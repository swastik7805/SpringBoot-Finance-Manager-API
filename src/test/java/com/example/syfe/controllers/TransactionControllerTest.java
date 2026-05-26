package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.TransactionRequest;
import com.example.syfe.dtos.requests.TransactionUpdateRequest;
import com.example.syfe.dtos.responses.TransactionResponse;
import com.example.syfe.services.TransactionService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createTransaction_ValidRequest_ReturnsCreated() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Lunch");
        request.setDate(LocalDate.now());
        request.setCategory("Food");

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Lunch")
                .type("EXPENSE")
                .category("Food")
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @WithMockUser
    void getAllTransactions_ReturnsOk() throws Exception {
        TransactionResponse t1 = TransactionResponse.builder().id(1L).amount(new BigDecimal("100.00")).build();
        TransactionResponse t2 = TransactionResponse.builder().id(2L).amount(new BigDecimal("200.00")).build();

        when(transactionService.getAllTransactions(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(1))
                .andExpect(jsonPath("$.transactions[1].id").value(2));
    }

    @Test
    @WithMockUser
    void updateTransaction_ReturnsOk() throws Exception {
        TransactionUpdateRequest request = new TransactionUpdateRequest();
        request.setAmount(new BigDecimal("150.00"));

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.00"))
                .build();

        when(transactionService.updateTransaction(anyLong(), any(TransactionUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/transactions/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    @WithMockUser
    void deleteTransaction_ReturnsOk() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);

        mockMvc.perform(delete("/api/transactions/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
