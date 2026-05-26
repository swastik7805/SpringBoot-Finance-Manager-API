package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.GoalRequest;
import com.example.syfe.dtos.requests.GoalUpdateRequest;
import com.example.syfe.dtos.responses.GoalResponse;
import com.example.syfe.services.GoalService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoalService goalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createGoal_ValidRequest_ReturnsCreated() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Vacation");
        request.setTargetAmount(new BigDecimal("2000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .goalName("Vacation")
                .targetAmount(new BigDecimal("2000.00"))
                .build();

        when(goalService.createGoal(any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Vacation"));
    }

    @Test
    @WithMockUser
    void getAllGoals_ReturnsOk() throws Exception {
        GoalResponse goal1 = GoalResponse.builder().id(1L).goalName("Vacation").build();
        GoalResponse goal2 = GoalResponse.builder().id(2L).goalName("Emergency Fund").build();

        when(goalService.getAllGoals()).thenReturn(List.of(goal1, goal2));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].goalName").value("Vacation"))
                .andExpect(jsonPath("$.goals[1].goalName").value("Emergency Fund"));
    }

    @Test
    @WithMockUser
    void getGoalById_ReturnsOk() throws Exception {
        GoalResponse goal = GoalResponse.builder().id(1L).goalName("Vacation").build();

        when(goalService.getGoalById(1L)).thenReturn(goal);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Vacation"));
    }

    @Test
    @WithMockUser
    void updateGoal_ReturnsOk() throws Exception {
        GoalUpdateRequest request = new GoalUpdateRequest();
        request.setTargetAmount(new BigDecimal("2500.00"));

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .goalName("Vacation")
                .targetAmount(new BigDecimal("2500.00"))
                .build();

        when(goalService.updateGoal(anyLong(), any(GoalUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(2500.00));
    }

    @Test
    @WithMockUser
    void deleteGoal_ReturnsOk() throws Exception {
        doNothing().when(goalService).deleteGoal(1L);

        mockMvc.perform(delete("/api/goals/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
