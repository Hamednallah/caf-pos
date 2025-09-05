package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.util.ReceiptPrinter;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrinterService {

    public List<String> getAvailablePrinters() {
        List<String> printers = new ArrayList<>();
        // Add virtual printers for testing if specified in system properties
        String virtualPrinters = System.getProperty("caf.test.virtualPrinters");
        if (virtualPrinters != null && !virtualPrinters.trim().isEmpty()) {
            printers.addAll(Arrays.asList(virtualPrinters.split(",")));
        }
        // Add real printers
        for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
            printers.add(s.getName());
        }
        return printers;
    }

    public void testPrint(String printerName, java.util.ResourceBundle resources) throws Exception {
        String testReceipt = "************************\n" +
                             resources.getString("settings.testPrint") + "\n" +
                             "************************\n";
        ReceiptPrinter.print(printerName, testReceipt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
