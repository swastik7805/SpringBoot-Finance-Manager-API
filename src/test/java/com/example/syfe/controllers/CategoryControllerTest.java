package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.CategoryRequest;
import com.example.syfe.dtos.responses.CategoryResponse;
import com.example.syfe.services.CategoryService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createCategory_ValidRequest_ReturnsCreated() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");
        request.setType("EXPENSE");

        CategoryResponse response = CategoryResponse.builder()
                .name("Groceries")
                .type("EXPENSE")
                .isCustom(true)
                .build();

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Groceries"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @WithMockUser
    void getAllCategories_ReturnsOk() throws Exception {
        CategoryResponse cat1 = CategoryResponse.builder().name("Food").type("EXPENSE").isCustom(false).build();
        CategoryResponse cat2 = CategoryResponse.builder().name("Salary").type("INCOME").isCustom(false).build();

        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Food"))
                .andExpect(jsonPath("$.categories[1].name").value("Salary"));
    }

    @Test
    @WithMockUser
    void deleteCategory_ReturnsOk() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyString());

        mockMvc.perform(delete("/api/categories/Groceries").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category Groceries deleted successfully"));
    }
}
