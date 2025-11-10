package com.example.equipmentapplication.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import javafx.util.Callback;

public class WindowUtils {
    public static void centerStageOnParent(Stage childStage, Stage parentStage) {
        if (parentStage != null && parentStage.isShowing()) {
            double centerX = parentStage.getX() + (parentStage.getWidth() / 2) - (childStage.getWidth() / 2);
            double centerY = parentStage.getY() + (parentStage.getHeight() / 2) - (childStage.getHeight() / 2);
            childStage.setX(centerX);
            childStage.setY(centerY);
        }
    }
    // Новый метод для создания фабрики ячеек статуса
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createStatusTableCellFactory() {
        return column -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                // Очищаем все стили
                getStyleClass().removeAll("status-active", "status-in-storage", "status-written-off", "status-cell");

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(getStatusDisplayName(status));

                    // Добавляем соответствующий CSS класс
                    switch (status) {
                        case "active" -> getStyleClass().add("status-active");
                        case "in_storage" -> getStyleClass().add("status-in-storage");
                        case "written_off" -> getStyleClass().add("status-written-off");
                    }
                }
            }
        };
    }
    public static String getStatusDisplayName(String status) {
        return switch (status) {
            case "active" -> "Установлен";
            case "in_storage" -> "На хранении";
            case "written_off" -> "Списан";
            default -> status;
        };
    }
}