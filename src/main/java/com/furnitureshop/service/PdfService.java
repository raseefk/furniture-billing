package com.furnitureshop.service;

import com.furnitureshop.config.ShopConfig;
import com.furnitureshop.model.Invoice;
import com.furnitureshop.model.InvoiceItem;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private final ShopConfig shopConfig;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public PdfService(ShopConfig shopConfig) {
        this.shopConfig = shopConfig;
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        String html = buildHtml(invoice);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private String buildHtml(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
        sb.append("<meta charset=\"UTF-8\"/>");
        sb.append("<title>").append(invoice.getDocumentTitle()).append("</title>");
        sb.append("<style>").append(getPdfStyles()).append("</style>");
        sb.append("</head><body>");

        // Watermark for quotation
        if (invoice.isQuotation()) {
            sb.append("<div class=\"watermark\">QUOTATION</div>");
        }

        // Header / Branding
        sb.append("<div class=\"header\">");
        sb.append("<div class=\"shop-info\">");
        sb.append("<h1 class=\"shop-name\">").append(escape(shopConfig.getName())).append("</h1>");
        sb.append("<p>").append(escape(shopConfig.getAddress())).append("</p>");
        sb.append("<p>Phone: ").append(escape(shopConfig.getPhone()))
          .append(" | Email: ").append(escape(shopConfig.getEmail())).append("</p>");
        sb.append("<p>GSTIN: <strong>").append(escape(shopConfig.getGstin())).append("</strong></p>");
        sb.append("</div>");
        sb.append("<div class=\"doc-type\">");
        sb.append("<h2>").append(invoice.getDocumentTitle().toUpperCase()).append("</h2>");
        sb.append("</div>");
        sb.append("</div>");

        sb.append("<hr class=\"divider\"/>");

        // Invoice Meta + Customer Info
        sb.append("<div class=\"meta-section\">");

        sb.append("<div class=\"bill-to\">");
        sb.append("<p class=\"section-label\">BILL TO</p>");
        sb.append("<p class=\"customer-name\">").append(escape(invoice.getCustomer().getName())).append("</p>");
        sb.append("<p>").append(escape(invoice.getCustomer().getAddress())).append("</p>");
        sb.append("<p>Phone: ").append(escape(invoice.getCustomer().getPhone())).append("</p>");
        if (invoice.getCustomer().hasGst()) {
            sb.append("<p>GSTIN: <strong>").append(escape(invoice.getCustomer().getGstNumber())).append("</strong></p>");
        }
        sb.append("</div>");

        sb.append("<div class=\"invoice-details\">");
        sb.append("<table class=\"details-table\">");
        sb.append("<tr><td class=\"label\">").append(invoice.isQuotation() ? "Quotation No." : "Invoice No.").append("</td>")
          .append("<td class=\"value\">").append(escape(invoice.getInvoiceNumber())).append("</td></tr>");
        sb.append("<tr><td class=\"label\">Date</td>")
          .append("<td class=\"value\">").append(invoice.getCreatedAt().format(DATE_FMT)).append("</td></tr>");
        if (invoice.isFinalBill() && invoice.getConvertedAt() != null) {
            sb.append("<tr><td class=\"label\">Billed On</td>")
              .append("<td class=\"value\">").append(invoice.getConvertedAt().format(DT_FMT)).append("</td></tr>");
        }
        sb.append("<tr><td class=\"label\">Status</td>")
          .append("<td class=\"value status-").append(invoice.getStatus().name()).append("\">")
          .append(escape(invoice.getStatus().getDisplayName())).append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        sb.append("</div>"); // meta-section

        // Items Table
        sb.append("<table class=\"items-table\">");
        sb.append("<thead><tr>");
        sb.append("<th class=\"col-no\">#</th>");
        sb.append("<th class=\"col-desc\">Description</th>");
        sb.append("<th class=\"col-hsn\">HSN</th>");
        sb.append("<th class=\"col-qty\">Qty</th>");
        sb.append("<th class=\"col-price\">Unit Price (&#8377;)</th>");
        sb.append("<th class=\"col-total\">Amount (&#8377;)</th>");
        sb.append("</tr></thead><tbody>");

        int i = 1;
        for (InvoiceItem item : invoice.getItems()) {
            sb.append("<tr class=\"").append(i % 2 == 0 ? "even-row" : "odd-row").append("\">");
            sb.append("<td class=\"center\">").append(i++).append("</td>");
            sb.append("<td>").append(escape(item.getProductName() != null ? item.getProductName() : item.getProduct().getName())).append("</td>");
            sb.append("<td class=\"center\">").append(escape(item.getHsnCode())).append("</td>");
            sb.append("<td class=\"center\">").append(item.getQuantity()).append("</td>");
            sb.append("<td class=\"right\">").append(fmt(item.getSoldAtPrice())).append("</td>");
            sb.append("<td class=\"right\">").append(fmt(item.getTotal())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        // Totals
        sb.append("<div class=\"totals-section\">");
        sb.append("<table class=\"totals-table\">");
        sb.append("<tr><td>Subtotal</td><td class=\"right\">&#8377; ").append(fmt(invoice.getSubtotal())).append("</td></tr>");
        sb.append("<tr><td>CGST @ 9%</td><td class=\"right\">&#8377; ").append(fmt(invoice.getCgstAmount())).append("</td></tr>");
        sb.append("<tr><td>SGST @ 9%</td><td class=\"right\">&#8377; ").append(fmt(invoice.getSgstAmount())).append("</td></tr>");
        sb.append("<tr class=\"grand-total-row\"><td>GRAND TOTAL</td><td class=\"right\">&#8377; ").append(fmt(invoice.getGrandTotal())).append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
            sb.append("<div class=\"notes\">");
            sb.append("<p class=\"section-label\">NOTES</p>");
            sb.append("<p>").append(escape(invoice.getNotes())).append("</p>");
            sb.append("</div>");
        }

        // Footer
        sb.append("<div class=\"footer\">");
        if (invoice.isQuotation()) {
            sb.append("<p>This is a quotation only. Stock is not reserved. Prices valid for 30 days.</p>");
        } else {
            sb.append("<p>This is a computer generated invoice and does not require a physical signature.</p>");
        }
        sb.append("<p>Thank you for choosing <strong>").append(escape(shopConfig.getName())).append("</strong>. ")
          .append(escape(shopConfig.getTagline())).append("</p>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String getPdfStyles() {
        return """
            @page { size: A4; margin: 15mm 12mm 20mm 12mm; }
            * { margin: 0; padding: 0; box-sizing: border-box; font-family: Arial, sans-serif; }
            body { font-size: 10pt; color: #222; background: #fff; }
            .watermark {
                position: fixed; top: 40%; left: 15%; width: 70%; text-align: center;
                font-size: 72pt; color: rgba(200,180,0,0.12); font-weight: bold;
                transform: rotate(-30deg); z-index: -1;
            }
            .header { display: table; width: 100%; margin-bottom: 6px; }
            .shop-info { display: table-cell; vertical-align: top; }
            .doc-type { display: table-cell; vertical-align: top; text-align: right; }
            .shop-name { font-size: 18pt; font-weight: bold; color: #1a1a2e; margin-bottom: 3px; }
            .shop-info p { font-size: 8.5pt; color: #444; margin: 2px 0; }
            .doc-type h2 { font-size: 16pt; color: #b8860b; font-weight: bold; margin-top: 10px; }
            .divider { border: none; border-top: 2px solid #b8860b; margin: 8px 0; }
            .meta-section { display: table; width: 100%; margin: 10px 0; }
            .bill-to { display: table-cell; vertical-align: top; width: 55%; }
            .invoice-details { display: table-cell; vertical-align: top; width: 45%; text-align: right; }
            .section-label { font-size: 8pt; font-weight: bold; color: #888; letter-spacing: 1px; margin-bottom: 4px; }
            .customer-name { font-size: 12pt; font-weight: bold; color: #1a1a2e; margin: 3px 0; }
            .bill-to p { font-size: 9pt; color: #444; margin: 2px 0; }
            .details-table { font-size: 9pt; width: 100%; }
            .details-table td.label { color: #666; padding: 2px 8px 2px 0; font-weight: bold; }
            .details-table td.value { color: #222; padding: 2px 0; }
            .status-QUOTATION { color: #b8860b; font-weight: bold; }
            .status-FINAL_BILL { color: #16a34a; font-weight: bold; }
            .items-table { width: 100%; border-collapse: collapse; margin: 14px 0 0 0; font-size: 9pt; }
            .items-table thead tr { background-color: #1a1a2e; color: #fff; }
            .items-table thead th { padding: 7px 5px; text-align: left; font-size: 8.5pt; }
            .items-table .col-no { width: 4%; }
            .items-table .col-desc { width: 35%; }
            .items-table .col-hsn { width: 10%; text-align: center; }
            .items-table .col-qty { width: 7%; text-align: center; }
            .items-table .col-price { width: 15%; text-align: right; }
            .items-table .col-total { width: 15%; text-align: right; }
            .items-table tbody td { padding: 6px 5px; border-bottom: 1px solid #eee; }
            .even-row { background-color: #f8f4e8; }
            .odd-row { background-color: #fff; }
            .center { text-align: center; }
            .right { text-align: right; }
            .totals-section { display: table; width: 100%; margin-top: 6px; }
            .totals-table { float: right; width: 42%; border-collapse: collapse; font-size: 9.5pt; margin-top: 4px; }
            .totals-table td { padding: 4px 8px; border-top: 1px solid #eee; }
            .totals-table td:first-child { color: #555; }
            .totals-table td.right { text-align: right; font-weight: bold; }
            .grand-total-row { background: #1a1a2e; color: #fff; font-size: 11pt; font-weight: bold; }
            .grand-total-row td { padding: 7px 8px; }
            .grand-total-row td.right { text-align: right; color: #f0c040; }
            .notes { margin-top: 20px; padding: 8px; border-left: 3px solid #b8860b; background: #fffbe6; font-size: 9pt; }
            .notes p { margin: 2px 0; color: #555; }
            .footer { margin-top: 25px; padding-top: 8px; border-top: 1px dashed #bbb; text-align: center; font-size: 8pt; color: #888; }
            .footer p { margin: 3px 0; }
            """;
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return String.format("%,.2f", val);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#x27;");
    }
}
