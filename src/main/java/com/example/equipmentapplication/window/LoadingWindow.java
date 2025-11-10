package com.example.equipmentapplication.window;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class LoadingWindow {
    private Stage stage;
    private ProgressBar progressBar;
    private Label messageLabel;

    public LoadingWindow() {
        stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(-1);

        messageLabel = new Label("Подключение к базе данных");

        VBox root = new VBox(20, messageLabel, progressBar);
        root.setAlignment(Pos.CENTER);

        Scene loadingScene = new Scene(root, 350, 120);
        URL stylesheetUrl = getClass().getResource("/styles.css");
        loadingScene.getStylesheets().add(stylesheetUrl != null ? stylesheetUrl.toExternalForm() : "");
        stage.setScene(loadingScene);
        stage.setTitle("Загрузка");
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void updateMessage(String message) {
        messageLabel.setText(message);
    }
}