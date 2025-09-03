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
     * Formats and prints an order receipt to the default printer using ESC/POS commands.
     * @param order The order to print.
     */
    public static void print(Order order) {
        try {
            byte[] receiptBytes = formatEscPosReceipt(order);
            print(receiptBytes);
        } catch (Exception e) {
            e.printStackTrace();
            // In a real UI, show an alert to the user that printing failed.
        }
    }

    /**
     * Prints a raw string to the default printer.
     * @param text The text to print.
     */
    public static void print(String text) throws Exception {
        print(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Prints a byte array to the default printer. Falls back to console if no printer is configured.
     * @param data The byte array to print.
     */
    public static void print(byte[] data) throws Exception {
        String printerName;
        try (Connection c = DataSourceProvider.getConnection()) {
            printerName = new SettingsService(c).get("printer.default");
        }

        if (printerName == null || printerName.trim().isEmpty()) {
            System.err.println("Receipt printing skipped: Default printer not configured in Settings.");
            System.out.println("\n--- CONSOLE FALLBACK (RAW BYTES) ---");
            System.out.println(new String(data, StandardCharsets.UTF_8)); // For demonstration
            System.out.println("------------------------------------\n");
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
        job.print(new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
    }

    public static String[] listPrinters() {
        PrintService[] arr = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            names[i] = arr[i].getName();
        }
        return names;
    }

    private static byte[] formatEscPosReceipt(Order order) {
        EscPosBuilder builder = new EscPosBuilder();

        builder.alignCenter()
               .bold(true)
               .append("Cafeteria POS")
               .feedLine()
               .bold(false)
               .append("------------------------------------------")
               .feedLine()
               .alignLeft()
               .append("Order ID: " + order.id)
               .feedLine()
               .append("Cashier: " + order.cashierId)
               .feedLine();

        if (order.createdAt != null) {
            builder.append("Date: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(order.createdAt)).feedLine();
        }
        builder.append("------------------------------------------").feedLine();

        for (OrderItem item : order.items) {
            String itemName = item.getItemName() != null ? item.getItemName() : "Item #" + item.getItemId();
            String itemLine = String.format("%-24s %2d x %5.2f = %6.2f",
                    itemName.length() > 24 ? itemName.substring(0, 24) : itemName,
                    item.getQuantity(),
                    item.getPriceAtPurchase(),
                    item.getLineTotal());
            builder.append(itemLine).feedLine();
        }

        builder.append("------------------------------------------").feedLine();

        BigDecimal subtotal = order.totalAmount != null ? order.totalAmount : BigDecimal.ZERO;
        BigDecimal discount = order.discountAmount != null ? order.discountAmount : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        builder.alignRight()
               .append(String.format("Subtotal: %8.2f", subtotal))
               .feedLine();

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(String.format("Discount: %8.2f", discount.negate())).feedLine();
        }

        builder.bold(true)
               .append(String.format("Total: %8.2f", total))
               .feedLine()
               .bold(false);

        builder.feedLine()
               .alignCenter()
               .append("Thank you for your visit!")
               .feedLines(3)
               .cut();

        return builder.getBytes();
    }
}
