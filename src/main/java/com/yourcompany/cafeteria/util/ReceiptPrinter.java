package com.yourcompany.cafeteria.util;
import javax.print.*; import java.nio.charset.StandardCharsets; import java.sql.Connection; import com.yourcompany.cafeteria.service.SettingsService;
public class ReceiptPrinter {
  public static void print(String text) throws Exception {
    try (Connection c = com.yourcompany.cafeteria.util.DataSourceProvider.getConnection()) {
      String name = new SettingsService(c).get("printer.default");
      if (name == null) throw new IllegalStateException("Printer not configured in Settings.");
      PrintService svc = null;
      for (PrintService s: PrintServiceLookup.lookupPrintServices(null, null)) if (s.getName().equalsIgnoreCase(name)) { svc=s; break; }
      if (svc==null) throw new IllegalStateException("Configured printer not found: " + name);
      DocPrintJob job = svc.createPrintJob();
      job.print(new SimpleDoc(text.getBytes(StandardCharsets.UTF_8), DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
    }
  }
  public static String[] listPrinters(){ PrintService[] arr = PrintServiceLookup.lookupPrintServices(null, null); String[] names = new String[arr.length]; for(int i=0;i<arr.length;i++) names[i]=arr[i].getName(); return names; }
}
