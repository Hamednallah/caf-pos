package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class SettingsViewTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Use in-memory DB for tests to avoid unsupported file flags
        DataSourceProvider.setURL("jdbc:h2:mem:cafeteria_test;DB_CLOSE_DELAY=-1");
        // Apply DB migrations since we're not booting MainApp
        Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load().migrate();
        // Load Settings view directly to avoid nav timing issues
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/SettingsView.fxml"));
        stage.setScene(new Scene(root, 1024, 768));
        stage.show();
    }

    @Test
    void settings_should_list_printers_and_save(FxRobot robot) {
        // Wait for the Settings view to load and the combo to appear
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        org.testfx.util.WaitForAsyncUtils.sleep(500, java.util.concurrent.TimeUnit.MILLISECONDS);
        org.testfx.util.WaitForAsyncUtils.sleep(1000, java.util.concurrent.TimeUnit.MILLISECONDS);

        ComboBox<?> printers = robot.lookup("#printerCombo").queryAs(ComboBox.class);
        Assertions.assertNotNull(printers, "Printer combo should exist on Settings view");
        System.out.println("Printer combo items: " + printers.getItems());
        Assertions.assertTrue(printers.getItems().size() > 0, "Expected at least one system printer");

        // Select first printer programmatically to avoid key timing issues
        robot.interact(() -> printers.getSelectionModel().select(0));
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        
        // Debug: Check if selection worked
        System.out.println("Selected printer: " + printers.getValue());
        Assertions.assertNotNull(printers.getValue(), "Printer should be selected");

        // Test if ANY button clicks work - try Test Print first
        System.out.println("Testing Test Print button click...");
        robot.clickOn("Test Print");
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        org.testfx.util.WaitForAsyncUtils.sleep(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        Label statusAfterTestPrint = robot.lookup("#statusLabel").queryAs(Label.class);
        System.out.println("Status after Test Print: '" + statusAfterTestPrint.getText() + "'");

        // Test database connection directly
        try (var c = DataSourceProvider.getConnection()) {
            var stmt = c.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM settings");
            rs.next();
            System.out.println("Settings table has " + rs.getInt(1) + " rows");
            
            // Test direct save to see if it works
            var settingsService = new com.yourcompany.cafeteria.service.SettingsService(c);
            settingsService.set("printer.default", "Test Printer");
            System.out.println("Direct save test: printer.default = " + settingsService.get("printer.default"));
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Save
        robot.clickOn("Save Settings");
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        org.testfx.util.WaitForAsyncUtils.sleep(500, java.util.concurrent.TimeUnit.MILLISECONDS);

        Label status = robot.lookup("#statusLabel").queryAs(Label.class);
        System.out.println("Status label text: '" + status.getText() + "'");
        System.out.println("Status label visible: " + status.isVisible());
        System.out.println("Status label managed: " + status.isManaged());
        Assertions.assertTrue(status.getText().toLowerCase().contains("saved"));

        // Test print button should be clickable
        Button testBtn = robot.lookup("Test Print").queryButton();
        robot.clickOn(testBtn);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }
}


