package com.example.printapplication.util;

import javafx.stage.Stage;

public class WindowUtils {
    public static void centerStageOnParent(Stage childStage, Stage parentStage) {
        if (parentStage != null && parentStage.isShowing()) {
            double centerX = parentStage.getX() + (parentStage.getWidth() / 2) - (childStage.getWidth() / 2);
            double centerY = parentStage.getY() + (parentStage.getHeight() / 2) - (childStage.getHeight() / 2);
            childStage.setX(centerX);
            childStage.setY(centerY);
        }
    }
}