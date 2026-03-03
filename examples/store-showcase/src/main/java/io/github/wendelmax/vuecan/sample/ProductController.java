package io.github.wendelmax.vuecan.sample;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import io.github.wendelmax.vuecan.web.VuecanResponse;
import java.util.Map;
import java.util.function.Supplier;
import java.time.LocalDateTime;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

        @GetMapping
        public VuecanResponse<?> list(@RequestParam(required = false) String category) {
                List<Product> products = List.of(
                                new Product(1L, "Premium Laptop", "electronics"),
                                new Product(2L, "Smart Phone", "electronics"),
                                new Product(3L, "Ergonomic Desk", "furniture"),
                                new Product(4L, "Mechanical Keyboard", "electronics"));
                List<Product> filtered = category == null || category.isBlank()
                                ? products
                                : products.stream().filter(p -> category.equals(p.category())).toList();

                // Showcase LazyProp: Only evaluated if requested
                Supplier<Map<String, Object>> marketTrends = () -> {
                        System.out.println("--- Evaluating Market Trends LazyProp ---");
                        return Map.of(
                                        "trending", "Mechanical Keyboards",
                                        "lastUpdate", LocalDateTime.now().toString(),
                                        "demand", "High");
                };

                return VuecanResponse.view("products/catalog")
                                .with("products", filtered)
                                .with("currentCategory", category != null ? category : "")
                                .with("marketTrends", marketTrends);
        }

        @GetMapping("/api/status")
        @ResponseBody
        public VuecanResponse<Map<String, Object>> getStatus() {
                return VuecanResponse.ok(Map.of(
                                "status", "Operational",
                                "serverTime", LocalDateTime.now().toString(),
                                "activeUsers", (int) (Math.random() * 100)));
        }
}
