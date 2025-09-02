package com.yourcompany.cafeteria.util;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.service.SettingsService;

import javax.print.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;

public class ReceiptPrinter {

    /**
     * Formats and prints an order receipt to the default printer.
     * @param order The order to print.
     */
    public static void print(Order order) {
        try {
            String receiptText = formatReceipt(order);
            print(receiptText);
        } catch (Exception e) {
            e.printStackTrace();
            // In a real UI, show an alert to the user that printing failed.
        }
    }

    /**
     * Prints a raw string to the default printer. Falls back to console if no printer is configured.
     * @param text The text to print.
     */
    public static void print(String text) throws Exception {
        String printerName;
        try (Connection c = DataSourceProvider.getConnection()) {
            printerName = new SettingsService(c).get("printer.default");
        }

        if (printerName == null || printerName.trim().isEmpty()) {
            System.err.println("Receipt printing skipped: Default printer not configured in Settings.");
            System.out.println("\n--- CONSOLE FALLBACK RECEIPT ---");
            System.out.println(text);
            System.out.println("--------------------------------\n");
            return;
        }

        PrintService svc = null;
        for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
            if (s.getName().equalsIgnoreCase(printerName)) {
                svc = s;
                break;
            }
        }

        if (svc == null) {
            throw new IllegalStateException("Configured printer not found: " + printerName);
        }

        DocPrintJob job = svc.createPrintJob();
        job.print(new SimpleDoc(text.getBytes(StandardCharsets.UTF_8), DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
    }

    /**
     * Lists all available printer names.
     * @return An array of printer names.
     */
    public static String[] listPrinters() {
        PrintService[] arr = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            names[i] = arr[i].getName();
        }
        return names;
    }

    private static String formatReceipt(Order order) {
        StringBuilder sb = new StringBuilder();

        sb.append("Order ID: ").append(order.id).append("\n");
        sb.append("Cashier ID: ").append(order.cashierId).append("\n");
        if (order.createdAt != null) {
            sb.append("Date: ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(order.createdAt)).append("\n");
        }
        sb.append("----------------------------------------\n");

        for (OrderItem item : order.items) {
            String itemName = item.getItemName() != null ? item.getItemName() : "Item #" + item.getItemId();
            sb.append(String.format("%-20s %2d x %5.2f = %6.2f\n",
                    itemName,
                    item.getQuantity(),
                    item.getPriceAtPurchase(),
                    item.getLineTotal()));
        }

        sb.append("----------------------------------------\n");

        BigDecimal subtotal = order.totalAmount != null ? order.totalAmount : BigDecimal.ZERO;
        BigDecimal discount = order.discountAmount != null ? order.discountAmount : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        sb.append(String.format("%28s: %6.2f\n", "Subtotal", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%28s: %6.2f\n", "Discount", discount.negate()));
        }
        sb.append(String.format("%28s: %6.2f\n", "Total", total));

        sb.append("\n       Thank you for your visit!        \n");

        return sb.toString();
    }
}
