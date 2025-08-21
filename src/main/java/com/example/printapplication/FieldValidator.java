package com.example.printapplication;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class FieldValidator {
    public static void highlightInvalidFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                field.getStyleClass().add("invalid-field");
            } else {
                field.getStyleClass().remove("invalid-field");
            }
        }
    }

    public static void clearFieldStyles(TextField... fields) {
        for (TextField field : fields) {
            field.getStyleClass().remove("invalid-field");
        }
    }

    public static void highlightInvalidComboBox(ComboBox<?> comboBox) {
        if (comboBox.getValue() == null) {
            comboBox.getStyleClass().add("invalid-combo");
        } else {
            comboBox.getStyleClass().remove("invalid-combo");
        }
    }

    public static void clearComboBoxStyle(ComboBox<?> comboBox) {
        comboBox.getStyleClass().remove("invalid-combo");
    }
}