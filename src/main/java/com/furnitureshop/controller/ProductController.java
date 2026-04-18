package com.furnitureshop.controller;

import com.furnitureshop.model.Product;
import com.furnitureshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String inventory(Model model, @RequestParam(required = false) String q) {
        model.addAttribute("products", q != null && !q.isBlank()
                ? productService.searchProducts(q)
                : productService.getAllActiveProducts());
        model.addAttribute("query", q);
        model.addAttribute("activePage", "inventory");
        model.addAttribute("newProduct", new Product());
        model.addAttribute("outOfStockCount", productService.countOutOfStock());
        model.addAttribute("lowStockCount", productService.countLowStock());
        return "inventory/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("activePage", "inventory");
        model.addAttribute("pageTitle", "Add New Product");
        model.addAttribute("formAction", "/inventory/save");
        return "inventory/form";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(product -> {
            model.addAttribute("product", product);
            model.addAttribute("activePage", "inventory");
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("formAction", "/inventory/save");
            return "inventory/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Product not found.");
            return "redirect:/inventory";
        });
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              Model model,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "inventory");
            model.addAttribute("pageTitle", product.getId() == null ? "Add New Product" : "Edit Product");
            model.addAttribute("formAction", "/inventory/save");
            return "inventory/form";
        }
        productService.save(product);
        ra.addFlashAttribute("success", product.getId() == null
                ? "Product added successfully!"
                : "Product updated successfully!");
        return "redirect:/inventory";
    }

    @PostMapping("/add-stock/{id}")
    public String addStock(@PathVariable Long id,
                           @RequestParam int quantity,
                           RedirectAttributes ra) {
        try {
            productService.addStock(id, quantity);
            ra.addFlashAttribute("success", "Stock updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deactivate(id);
            ra.addFlashAttribute("success", "Product removed from catalogue.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory";
    }

    @GetMapping("/{id}/view")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("activePage", "inventory");
            return "inventory/view";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Product not found.");
            return "redirect:/inventory";
        });
    }
}
