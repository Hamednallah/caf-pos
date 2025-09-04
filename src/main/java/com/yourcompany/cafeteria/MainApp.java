package com.yourcompany.cafeteria;

import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // It's good practice to run migrations at the start.
        // This ensures the database schema is always up to date.
        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();

        URL mainViewUrl = getClass().getResource("/fxml/MainView.fxml");
        if (mainViewUrl == null) {
            System.err.println("Cannot find MainView.fxml. Please check the path.");
            return;
        }

        // Set the locale for i18n. This would typically be loaded from user settings.
        Locale locale = new Locale("ar");
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);

        FXMLLoader loader = new FXMLLoader(mainViewUrl, bundle);
        Parent root = loader.load();


        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());

        if (locale.getLanguage().equals("ar")) {
            scene.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }

        primaryStage.setTitle("Cafeteria POS");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
