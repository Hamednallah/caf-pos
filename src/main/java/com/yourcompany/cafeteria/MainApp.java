package com.yourcompany.cafeteria;

import com.yourcompany.cafeteria.service.SettingsService;
import com.yourcompany.cafeteria.ui.MainController;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.geometry.NodeOrientation;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    private Stage primaryStage;
    private static final String BASE_BUNDLE_NAME = "com.yourcompany.cafeteria.messages";

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        // It's good practice to run migrations at the start.
        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();

        // Load the application with the default locale
        loadApplication(Locale.getDefault());
    }

    public void switchLanguage(Locale newLocale) {
        Locale.setDefault(newLocale);
        try {
            // Reload the entire application UI
            primaryStage.close();
            start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadApplication(Locale locale) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_BUNDLE_NAME, locale);

        URL mainViewUrl = getClass().getResource("/com/yourcompany/cafeteria/fxml/MainView.fxml");
        if (mainViewUrl == null) {
            System.err.println("Cannot find MainView.fxml. Please check the path.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(mainViewUrl, bundle);
        Parent root = loader.load();

        // Give the controller a reference back to this MainApp instance
        MainController controller = loader.getController();
        controller.setMainApp(this);
        controller.setResources(bundle);

        Scene scene = new Scene(root);

        // Load and apply theme
        try (var c = DataSourceProvider.getConnection()) {
            SettingsService settingsService = new SettingsService(c);
            String theme = settingsService.get("ui.theme");
            if ("Dark".equalsIgnoreCase(theme)) {
                scene.getStylesheets().add(getClass().getResource("/com/yourcompany/cafeteria/dark-theme.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/com/yourcompany/cafeteria/application.css").toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default stylesheet
            scene.getStylesheets().add(getClass().getResource("/com/yourcompany/cafeteria/application.css").toExternalForm());
        }


        // Handle Right-to-Left (RTL) layout
        if (isRTL(locale)) {
            scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            scene.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private boolean isRTL(Locale locale) {
        return locale.getLanguage().equals("ar");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
