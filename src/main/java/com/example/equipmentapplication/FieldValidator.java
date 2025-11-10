package com.example.equipmentapplication;

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

    public static void highlightInvalidComboBox(ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            if (comboBox.getValue() == null) {
                if (!comboBox.getStyleClass().contains("invalid-combo")) {
                    comboBox.getStyleClass().add("invalid-combo");
                }
            } else {
                comboBox.getStyleClass().remove("invalid-combo");
            }
        }
    }

    public static void clearComboBoxStyle(ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            comboBox.getStyleClass().remove("invalid-combo");
        }
    }
}