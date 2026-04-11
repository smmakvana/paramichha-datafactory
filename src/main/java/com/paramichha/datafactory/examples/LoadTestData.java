package com.paramichha.datafactory.examples;

import com.paramichha.datafactory.DataFactory;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;

/**
 * Load Test Data — generating thousands of realistic objects in milliseconds.
 */
public class LoadTestData {

    @Value @Builder
    static class OrderRequest {
        @NotBlank @Email           String customerEmail;
        @NotBlank                  String productSku;
        @NotNull @Min(1) @Max(999) Integer quantity;
        @NotNull @Positive         BigDecimal unitPrice;
        @NotNull @Past             LocalDate orderDate;
    }

    public static void main(String[] args) {
        bulkObjects();
        bulkStrings();
        bulkNumbers();
        distribution();
    }

    static void bulkObjects() {
        section("1000 valid orders in one line");
        long start = System.currentTimeMillis();
        List<OrderRequest> orders = DataFactory.of(OrderRequest.class).stream(1000);
        long ms = System.currentTimeMillis() - start;
        long unique = orders.stream().map(OrderRequest::getCustomerEmail).distinct().count();
        System.out.println("  generated:     " + orders.size() + " orders in " + ms + "ms");
        System.out.println("  unique emails: " + unique);
        System.out.println("  sample:        " + orders.get(0).getCustomerEmail()
                + "  qty=" + orders.get(0).getQuantity());
    }

    static void bulkStrings() {
        section("Bulk string generation");
        System.out.println("  10 emails:   " + DataFactory.emails(10));
        System.out.println("  5 names:     " + DataFactory.names(5));
        System.out.println("  5 companies: " + DataFactory.companies(5));
    }

    static void bulkNumbers() {
        section("Bulk number generation");
        System.out.println("  10 ages: " + DataFactory.integer().range(18, 65).stream(10));
        System.out.println("  5 ids:   " + DataFactory.longVal().positive().stream(5));
    }

    static void distribution() {
        section("Data distribution — 500 orders");
        List<OrderRequest> orders = DataFactory.of(OrderRequest.class).stream(500);
        IntSummaryStatistics qty = orders.stream()
                .mapToInt(OrderRequest::getQuantity).summaryStatistics();
        DoubleSummaryStatistics price = orders.stream()
                .mapToDouble(o -> o.getUnitPrice().doubleValue()).summaryStatistics();
        System.out.println("  qty   min=" + qty.getMin() + " max=" + qty.getMax()
                + String.format(" avg=%.1f", qty.getAverage()));
        System.out.printf("  price min=%.2f max=%.2f avg=%.2f%n",
                price.getMin(), price.getMax(), price.getAverage());
    }

    static void section(String t) {
        System.out.println();
        System.out.println(t);
        System.out.println("  " + "-".repeat(t.length()));
    }
}
