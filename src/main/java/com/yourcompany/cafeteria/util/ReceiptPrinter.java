package com.yourcompany.cafeteria.util;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.service.SettingsService;

import javax.print.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ReceiptPrinter {

    /**
     * Formats and prints an order receipt to the default printer using ESC/POS commands.
     * @param order The order to print.
     */
    public static void print(Order order, java.util.ResourceBundle resources, java.util.Locale locale) {
        try {
            byte[] receiptBytes = formatEscPosReceipt(order, resources, locale);
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
        print(null, data);
    }

    public static void print(String printerName, byte[] data) throws Exception {
        if (printerName == null || printerName.trim().isEmpty()) {
            try (Connection c = DataSourceProvider.getConnection()) {
                printerName = new SettingsService(c).getSetting("printer.default");
            }
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

    private static byte[] formatEscPosReceipt(Order order, java.util.ResourceBundle resources, java.util.Locale locale) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);

        EscPosBuilder builder = new EscPosBuilder();

        builder.alignCenter()
               .bold(true)
               .append(resources.getString("receipt.title"))
               .feedLine()
               .bold(false)
               .append("------------------------------------------")
               .feedLine()
               .alignLeft()
               .append(resources.getString("receipt.orderId") + " " + order.id)
               .feedLine()
               .append(resources.getString("receipt.cashier") + " " + order.cashierId)
               .feedLine();

        if (order.createdAt != null) {
            builder.append(resources.getString("receipt.date") + " " + dateTimeFormat.format(order.createdAt)).feedLine();
        }
        builder.append("------------------------------------------").feedLine();

        boolean isRtl = locale.getLanguage().equals("ar");

        for (OrderItem item : order.items) {
            String itemName = item.getItemName() != null ? item.getItemName() : resources.getString("receipt.item") + " #" + item.getItemId();
            String itemLine;
            if (isRtl) {
                itemLine = String.format("%s = %s x %2d %-20s",
                        currencyFormat.format(item.getLineTotal()),
                        currencyFormat.format(item.getPriceAtPurchase()),
                        item.getQuantity(),
                        itemName.length() > 20 ? itemName.substring(0, 20) : itemName);
                builder.alignRight().append(itemLine).feedLine();
            } else {
                itemLine = String.format("%-20s %2d x %s = %s",
                        itemName.length() > 20 ? itemName.substring(0, 20) : itemName,
                        item.getQuantity(),
                        currencyFormat.format(item.getPriceAtPurchase()),
                        currencyFormat.format(item.getLineTotal()));
                builder.alignLeft().append(itemLine).feedLine();
            }
        }

        builder.append("------------------------------------------").feedLine();

        BigDecimal subtotal = order.totalAmount != null ? order.totalAmount : BigDecimal.ZERO;
        BigDecimal discount = order.discountAmount != null ? order.discountAmount : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        if (isRtl) {
            builder.alignLeft();
            builder.append(currencyFormat.format(subtotal) + " :" + resources.getString("receipt.subtotal")).feedLine();
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                builder.append(currencyFormat.format(discount.negate()) + " :" + resources.getString("receipt.discount")).feedLine();
            }
            builder.bold(true).append(currencyFormat.format(total) + " :" + resources.getString("receipt.total")).feedLine().bold(false);
        } else {
            builder.alignRight();
            builder.append(resources.getString("receipt.subtotal") + " " + currencyFormat.format(subtotal)).feedLine();
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                builder.append(resources.getString("receipt.discount") + " " + currencyFormat.format(discount.negate())).feedLine();
            }
            builder.bold(true).append(resources.getString("receipt.total") + " " + currencyFormat.format(total)).feedLine().bold(false);
        }

        builder.feedLine()
               .alignCenter()
               .append(resources.getString("receipt.thankYou"))
               .feedLines(3)
               .cut();

        return builder.getBytes();
    }
}
