package com.yourcompany.cafeteria;

import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
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

        // -- I18N SETUP --
        // In a real app, this would come from a settings file or a login screen selection.
        // Locale locale = new Locale("ar"); // For Arabic
        Locale locale = Locale.ENGLISH;   // For English

        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);

        URL mainViewUrl = getClass().getResource("/fxml/MainView.fxml");
        if (mainViewUrl == null) {
            System.err.println("Cannot find MainView.fxml. Please check the path.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(mainViewUrl, bundle);
        Parent root = loader.load();

        com.yourcompany.cafeteria.ui.MainController controller = loader.getController();
        controller.setResources(bundle);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());

        // Set RTL layout if Arabic
        if (locale.getLanguage().equals("ar")) {
            scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            scene.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }

        primaryStage.setTitle(bundle.getString("main.title"));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
