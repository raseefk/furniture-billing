package com.furnitureshop.controller;

import com.furnitureshop.model.Customer;
import com.furnitureshop.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(Model model, @RequestParam(required = false) String q) {
        model.addAttribute("customers", q != null && !q.isBlank()
                ? customerService.search(q)
                : customerService.getAllActiveCustomers());
        model.addAttribute("query", q);
        model.addAttribute("activePage", "customers");
        return "billing/customers";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("activePage", "customers");
        model.addAttribute("pageTitle", "Add Customer");
        return "billing/customer-form";
    }

    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return customerService.findById(id).map(c -> {
            model.addAttribute("customer", c);
            model.addAttribute("activePage", "customers");
            model.addAttribute("pageTitle", "Edit Customer");
            return "billing/customer-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Customer not found.");
            return "redirect:/customers";
        });
    }

    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute("customer") Customer customer,
                               BindingResult result,
                               Model model,
                               RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "customers");
            model.addAttribute("pageTitle", customer.getId() == null ? "Add Customer" : "Edit Customer");
            return "billing/customer-form";
        }
        customerService.save(customer);
        ra.addFlashAttribute("success", "Customer saved successfully!");
        return "redirect:/customers";
    }
}
