package com.example.printapplication.util;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AlertUtils {
    public static void showErrorAlert(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setOnShown(event -> {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            WindowUtils.centerStageOnParent(alertStage, owner);
        });
        alert.showAndWait();
    }

    public static void showInformationAlert(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.setOnShown(event -> {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            WindowUtils.centerStageOnParent(alertStage, owner);
        });
        alert.showAndWait();
    }
}