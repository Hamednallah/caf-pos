package com.yourcompany.cafeteria;

import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;

import java.net.URL;

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

        Parent root = FXMLLoader.load(mainViewUrl);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Cafeteria POS");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
