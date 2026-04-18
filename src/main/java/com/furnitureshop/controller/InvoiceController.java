package com.furnitureshop.controller;

import com.furnitureshop.dto.InvoiceFormDto;
import com.furnitureshop.dto.InvoiceItemDto;
import com.furnitureshop.model.InvoiceStatus;
import com.furnitureshop.service.CustomerService;
import com.furnitureshop.service.InvoiceService;
import com.furnitureshop.service.PdfService;
import com.furnitureshop.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/billing")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PdfService pdfService;

    public InvoiceController(InvoiceService invoiceService,
                             CustomerService customerService,
                             ProductService productService,
                             PdfService pdfService) {
        this.invoiceService = invoiceService;
        this.customerService = customerService;
        this.productService = productService;
        this.pdfService = pdfService;
    }

    // ─── New Bill / Quotation Form ───────────────────────────────────────────

    @GetMapping("/new")
    public String newBillForm(Model model) {
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("activePage", "newBill");
        return "billing/new-bill";
    }

    // ─── Create Invoice (handles both QUOTATION and FINAL_BILL) ─────────────

    @PostMapping("/create")
    public String createInvoice(@RequestParam Long customerId,
                                @RequestParam String status,
                                @RequestParam(required = false) String notes,
                                @RequestParam List<Long> productIds,
                                @RequestParam List<Integer> quantities,
                                @RequestParam List<java.math.BigDecimal> prices,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            List<InvoiceItemDto> items = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                if (productIds.get(i) != null && quantities.get(i) != null && quantities.get(i) > 0) {
                    items.add(new InvoiceItemDto(productIds.get(i), quantities.get(i),
                            (prices != null && i < prices.size()) ? prices.get(i) : null));
                }
            }

            InvoiceFormDto form = InvoiceFormDto.builder()
                    .customerId(customerId)
                    .notes(notes)
                    .status(status)
                    .items(items)
                    .build();

            if ("FINAL_BILL".equals(status)) {
                var invoice = invoiceService.createFinalBill(form, auth.getName());
                ra.addFlashAttribute("success", "Final Bill " + invoice.getInvoiceNumber() + " created successfully!");
                return "redirect:/billing/view/" + invoice.getId();
            } else {
                var invoice = invoiceService.createQuotation(form, auth.getName());
                ra.addFlashAttribute("success", "Quotation " + invoice.getInvoiceNumber() + " created successfully!");
                return "redirect:/billing/view/" + invoice.getId();
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error creating invoice: " + e.getMessage());
            return "redirect:/billing/new";
        }
    }

    // ─── View Invoice ────────────────────────────────────────────────────────

    @GetMapping("/view/{id}")
    public String viewInvoice(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return invoiceService.findById(id).map(invoice -> {
            model.addAttribute("invoice", invoice);
            model.addAttribute("activePage", invoice.isQuotation() ? "quotations" : "salesHistory");
            return "billing/invoice-view";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Invoice not found.");
            return "redirect:/billing/history";
        });
    }

    // ─── Convert Quotation to Final Bill ────────────────────────────────────

    @PostMapping("/convert/{id}")
    public String convertToFinalBill(@PathVariable Long id, RedirectAttributes ra) {
        try {
            var invoice = invoiceService.convertToFinalBill(id);
            ra.addFlashAttribute("success",
                    "Quotation successfully converted to Final Bill: " + invoice.getInvoiceNumber());
            return "redirect:/billing/view/" + invoice.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Conversion failed: " + e.getMessage());
            return "redirect:/billing/view/" + id;
        }
    }

    // ─── Cancel Quotation ────────────────────────────────────────────────────

    @PostMapping("/cancel/{id}")
    public String cancelQuotation(@PathVariable Long id, RedirectAttributes ra) {
        try {
            invoiceService.cancelQuotation(id);
            ra.addFlashAttribute("success", "Quotation cancelled successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billing/quotations";
    }

    // ─── Quotations List ─────────────────────────────────────────────────────

    @GetMapping("/quotations")
    public String quotations(Model model, @RequestParam(required = false) String q) {
        model.addAttribute("invoices", q != null && !q.isBlank()
                ? invoiceService.searchQuotations(q)
                : invoiceService.getQuotations());
        model.addAttribute("query", q);
        model.addAttribute("activePage", "quotations");
        model.addAttribute("pageTitle", "Quotations");
        model.addAttribute("statusFilter", "QUOTATION");
        return "billing/invoice-list";
    }

    // ─── Sales History (Final Bills) ─────────────────────────────────────────

    @GetMapping("/history")
    public String salesHistory(Model model, @RequestParam(required = false) String q) {
        model.addAttribute("invoices", q != null && !q.isBlank()
                ? invoiceService.searchFinalBills(q)
                : invoiceService.getFinalBills());
        model.addAttribute("query", q);
        model.addAttribute("activePage", "salesHistory");
        model.addAttribute("pageTitle", "Sales History");
        model.addAttribute("statusFilter", "FINAL_BILL");
        return "billing/invoice-list";
    }

    // ─── PDF Download ─────────────────────────────────────────────────────────

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        return invoiceService.findById(id).map(invoice -> {
            byte[] pdf = pdfService.generateInvoicePdf(invoice);
            String filename = (invoice.isQuotation() ? "Quotation" : "Invoice")
                    + "-" + invoice.getInvoiceNumber().replace("/", "-") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── AJAX: Get product price ──────────────────────────────────────────────

    @GetMapping("/api/product/{id}/price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable Long id) {
    	return productService.findById(id)
    		    .map(p -> {
    		        Map<String, Object> map = new HashMap<>();
    		        map.put("price", p.getCurrentPrice());
    		        map.put("name", p.getName());
    		        map.put("stock", p.getStockQuantity());
    		        map.put("hsn", p.getHsnCode());
    		        return ResponseEntity.ok(map);
    		    })
    		    .orElse(ResponseEntity.notFound().build());
    }

    // ─── AJAX: Search customers ───────────────────────────────────────────────

    @GetMapping("/api/customers/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchCustomers(@RequestParam String q) {
        var customers = customerService.search(q);
        var result = customers.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "name", c.getName(),
                "phone", c.getPhone(),
                "address", c.getAddress(),
                "gstNumber", c.getGstNumber() != null ? c.getGstNumber() : ""
        )).toList();
        return ResponseEntity.ok(result);
    }
}
