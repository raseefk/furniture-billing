package com.furnitureshop.controller;

import com.furnitureshop.dto.DashboardStatsDto;
import com.furnitureshop.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final CustomerService customerService;
    private final InvoiceService invoiceService;
    private final UserService userService;

    public DashboardController(ProductService productService,
                               CustomerService customerService,
                               InvoiceService invoiceService,
                               UserService userService) {
        this.productService = productService;
        this.customerService = customerService;
        this.invoiceService = invoiceService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalProducts(productService.countActive())
                .outOfStockCount(productService.countOutOfStock())
                .lowStockCount(productService.countLowStock())
                .totalCustomers(customerService.count())
                .totalQuotations(invoiceService.countQuotations())
                .totalFinalBills(invoiceService.countFinalBills())
                .totalRevenue(invoiceService.getTotalRevenue())
                .monthlyRevenue(invoiceService.getMonthlyRevenue())
                .build();

        model.addAttribute("stats", stats);
        model.addAttribute("recentBills", invoiceService.getRecentBills());
        model.addAttribute("recentQuotations", invoiceService.getRecentQuotations());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("username", auth.getName());

        userService.findByUsername(auth.getName())
                .ifPresent(u -> model.addAttribute("fullName", u.getFullName()));

        return "dashboard";
    }
}
