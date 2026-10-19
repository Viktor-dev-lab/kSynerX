package org.example.ksynerx.presentation.controller;

import org.example.ksynerx.KSynerXApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = KSynerXApplication.class)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.productId").isString())
                .andExpect(jsonPath("$.data.productId").value("1"))
                .andExpect(jsonPath("$.data.name").value("Bàn phím cơ"))
                .andExpect(jsonPath("$.data.price").value(1290000.00));
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy sản phẩm với productId = 999"));
    }

    @Test
    void shouldUseProductIdFromPathWhenQueryContainsAnotherProductId() throws Exception {
        mockMvc.perform(get("/api/v1/products/1").param("productId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("1"))
                .andExpect(jsonPath("$.data.name").value("Bàn phím cơ"));
    }

    @Test
    void shouldReturnNotFoundForUnknownStringProductId() throws Exception {
        mockMvc.perform(get("/api/v1/products/abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy sản phẩm với productId = abc"));
    }

    @Test
    @Sql(statements = "INSERT INTO products (product_id, name, price) VALUES ('PRD001', 'Sản phẩm mã chuỗi', 100.00)")
    @Sql(statements = "DELETE FROM products WHERE product_id = 'PRD001'", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldLoadAlphanumericProductIdFromDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/products/PRD001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("PRD001"))
                .andExpect(jsonPath("$.data.name").value("Sản phẩm mã chuỗi"));
    }

    @Test
    @Sql(statements = "INSERT INTO products (product_id, name, price) VALUES ('001', 'Sản phẩm mã 001', 200.00)")
    @Sql(statements = "DELETE FROM products WHERE product_id = '001'", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldPreserveLeadingZerosInProductId() throws Exception {
        mockMvc.perform(get("/api/v1/products/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("001"))
                .andExpect(jsonPath("$.data.name").value("Sản phẩm mã 001"));
    }

    @Test
    void shouldReturnRequestMetadataInResponse() throws Exception {
        mockMvc.perform(get("/api/v1/products/1")
                        .param("requestId", "request-001")
                        .param("requestDateTime", "2026-10-19T08:00:00+07:00")
                        .param("channel", "Web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("request-001"))
                .andExpect(jsonPath("$.requestDateTime").value("2026-10-19T08:00+07:00"))
                .andExpect(jsonPath("$.channel").value("Web"));
    }

    @Test
    void shouldReturnBadRequestForBlankProductId() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("productId không được để trống"));
    }

    @Test
    void shouldReturnBadRequestForProductIdLongerThanDatabaseLimit() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", "P".repeat(65)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("productId không được vượt quá 64 ký tự"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequestDateTime() throws Exception {
        mockMvc.perform(get("/api/v1/products/1").param("requestDateTime", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Tham số 'requestDateTime' không hợp lệ"));
    }
}
