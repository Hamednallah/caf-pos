package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.DashboardStats;
import com.yourcompany.cafeteria.service.DashboardService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import com.yourcompany.cafeteria.model.DashboardStats;
import com.yourcompany.cafeteria.service.DashboardService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private GridPane statsGridPane;

    @FXML
    private BarChart<String, Number> salesChart;

    private DashboardService dashboardService;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        try {
            dashboardService = new DashboardService(DataSourceProvider.getConnection());
            loadStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStats() throws Exception {
        DashboardStats stats = dashboardService.getDashboardStats();
        populateGrid(stats);
        populateSalesChart(stats.getSalesByMonth());
    }

    private void populateGrid(DashboardStats stats) {
        statsGridPane.getChildren().clear();

        int row = 0;
        int col = 0;

        statsGridPane.add(createStatCard(resources.getString("dashboard.totalSales"), formatCurrency(stats.getTotalSales())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.salesCurrentMonth"), formatCurrency(stats.getSalesForCurrentMonth())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.avgDailySales"), formatCurrency(stats.getAverageDailySales())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.avgMonthlySales"), formatCurrency(stats.getAverageMonthlySales())), col++, row);

        row++;
        col = 0;
        statsGridPane.add(createStatCard(resources.getString("dashboard.totalExpenses"), formatCurrency(stats.getTotalExpenses())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.expensesCurrentMonth"), formatCurrency(stats.getExpensesForCurrentMonth())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.avgDailyExpenses"), formatCurrency(stats.getAverageDailyExpenses())), col++, row);
        statsGridPane.add(createStatCard(resources.getString("dashboard.avgMonthlyExpenses"), formatCurrency(stats.getAverageMonthlyExpenses())), col++, row);

        row++;
        col = 0;
        VBox userCountsCard = new VBox(10);
        userCountsCard.setStyle("-fx-background-color: -fx-surface; -fx-padding: 20; -fx-background-radius: 8;");
        Label userCountsTitle = new Label(resources.getString("dashboard.userCounts"));
        userCountsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        userCountsCard.getChildren().add(userCountsTitle);
        for (java.util.Map.Entry<String, Integer> entry : stats.getUserCountsByRole().entrySet()) {
            userCountsCard.getChildren().add(new Label(entry.getKey() + "s: " + entry.getValue()));
        }
        statsGridPane.add(userCountsCard, col, row, 2, 1);
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: -fx-surface; -fx-padding: 20; -fx-background-radius: 8;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value);
    }

    private void populateSalesChart(Map<String, BigDecimal> salesByMonth) {
        if (salesChart == null || salesByMonth == null) {
            return;
        }

        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(resources.getString("dashboard.chart.monthlySales.series"));

        // The DAO returns a LinkedHashMap which preserves insertion order (ordered by month)
        for (Map.Entry<String, BigDecimal> entry : salesByMonth.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        salesChart.setData(FXCollections.observableArrayList(series));
    }
}
