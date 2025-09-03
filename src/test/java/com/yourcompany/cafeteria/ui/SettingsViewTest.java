package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.MainApp;
import com.yourcompany.cafeteria.util.DataSourceProvider;
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
        // Boot the real app so navigation works
        new MainApp().start(stage);
    }

    @Test
    void settings_should_list_printers_and_save(FxRobot robot) {
        // Navigate to Settings
        robot.clickOn("Settings");

        // Wait for the Settings view to load and the combo to appear
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        org.testfx.util.WaitForAsyncUtils.sleep(500, java.util.concurrent.TimeUnit.MILLISECONDS);

        ComboBox<?> printers = robot.lookup("#printerCombo").queryAs(ComboBox.class);
        Assertions.assertNotNull(printers, "Printer combo should exist on Settings view");
        Assertions.assertTrue(printers.getItems().size() > 0, "Expected at least one system printer");

        // Select first printer programmatically to avoid key timing issues
        robot.interact(() -> printers.getSelectionModel().select(0));

        // Save
        robot.clickOn("Save Settings");

        Label status = robot.lookup("#statusLabel").queryAs(Label.class);
        Assertions.assertTrue(status.getText().toLowerCase().contains("saved"));

        // Test print button should be clickable
        Button testBtn = robot.lookup("Test Print").queryButton();
        robot.clickOn(testBtn);
    }
}


