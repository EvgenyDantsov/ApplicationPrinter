package com.example.printapplication.window;

import com.example.printapplication.dao.DepartmentDAO;
import com.example.printapplication.dto.Department;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Collections;

import static com.example.printapplication.FieldValidator.*;
import static com.example.printapplication.util.AlertUtils.*;

import static com.example.printapplication.util.WindowUtils.centerStageOnParent;

public class DepartmentWindow {
    private TableView<Department> table;
    private ObservableList<Department> departmentList;
    private Stage departmentStage;
    private TextField departmentNameField;
    private static final String ERROR_TITLE = "Ошибка";
    private static final String ENTER_DEPARTMENT_NAME = "Введите название отделения.";
    private static final String SELECT_DEPARTMENT_TO_UPDATE = "Выберите отделение для изменения.";
    private static final String ENTER_NEW_DEPARTMENT_NAME = "Введите новое название отделения.";
    private static final String ADD_DEPARTMENT_FAILED = "Не удалось добавить отделение.";
    private static final String UPDATE_DEPARTMENT_FAILED = "Не удалось обновить отделение.";
    private static final String SELECT_DEPARTMENT_TO_DELETE = "Выберите отделение для удаления.";
    private static final String DELETE_DEPARTMENT_FAILED = "Не удалось удалить отделение.";

    public void start(Stage departmentStage, Stage parentStage) {
        this.departmentStage = departmentStage;
        departmentStage.setTitle("Отделения");
        // Инициализация таблицы
        table = new TableView<>();
        departmentList = FXCollections.observableArrayList();
        // Колонки таблицы
        TableColumn<Department, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Department, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        Collections.addAll(table.getColumns(), idColumn, nameColumn);
        // Заполнение таблицы данными
        loadDepartments();
        departmentNameField = new TextField();
        departmentNameField.setPromptText("Название отделения");
        // Поле для ввода названия отделения
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Название отделения:"), departmentNameField);
        // Слушатель для заполнения полей при выборе отдела
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithSelectedDepartment(newSelection); // Заполняем поля данными выбранного отдела
            }
        });
        // Кнопки для добавления, изменения и удаления
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addDepartment());
        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateDepartment());
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteDepartment());
        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> departmentStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 500, 400);
        departmentStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        // Позиционирование окна по центру относительно родительского окна
        departmentStage.setOnShown(event -> centerStageOnParent(departmentStage, parentStage));
        departmentStage.show();
    }

    // Загрузка данных из базы данных в таблицу
    private void loadDepartments() {
        departmentList.setAll(DepartmentDAO.getAllDepartments());
        table.setItems(departmentList);
    }

    // Добавление нового отделения
    private void addDepartment() {
        if (!validateFields()) return;
        String name = departmentNameField.getText();
        if (name.isEmpty()) {
            showErrorAlert(departmentStage, ERROR_TITLE, ENTER_DEPARTMENT_NAME);
            return;
        }
        // Проверяем, существует ли отделение с таким именем
        if (DepartmentDAO.departmentExists(name)) {
            showErrorAlert(departmentStage, ERROR_TITLE, "Отделение с таким названием уже существует.");
            return;
        }
        if (DepartmentDAO.addDepartment(name)) {
            loadDepartments();
            departmentNameField.clear();
        } else {
            showErrorAlert(departmentStage, ERROR_TITLE, ADD_DEPARTMENT_FAILED);
        }
    }

    // Обновление выбранного отделения
    private void updateDepartment() {
        if (!validateFields()) return;
        Department selectedDepartment = table.getSelectionModel().getSelectedItem();
        if (selectedDepartment == null) {
            showErrorAlert(departmentStage, ERROR_TITLE, SELECT_DEPARTMENT_TO_UPDATE);
            return;
        }
        String name = departmentNameField.getText();
        if (name.isEmpty()) {
            showErrorAlert(departmentStage, ERROR_TITLE, ENTER_NEW_DEPARTMENT_NAME);
            return;
        }
        // Проверяем, существует ли отделение с таким именем
        if (DepartmentDAO.departmentExists(name)) {
            showErrorAlert(departmentStage, ERROR_TITLE, "Отделение с таким названием уже существует.");
            return;
        }
        if (DepartmentDAO.updateDepartment(selectedDepartment.getId(), name)) {
            loadDepartments();
            departmentNameField.clear();
        } else {
            showErrorAlert(departmentStage, ERROR_TITLE, UPDATE_DEPARTMENT_FAILED);
        }
    }

    // Удаление выбранного отделения
    private void deleteDepartment() {
        Department selectedDepartment = table.getSelectionModel().getSelectedItem();
        if (selectedDepartment == null) {
            showErrorAlert(departmentStage, ERROR_TITLE, SELECT_DEPARTMENT_TO_DELETE);
            return;
        }
        if (DepartmentDAO.deleteDepartment(selectedDepartment.getId())) {
            loadDepartments();
            departmentNameField.clear();
        } else {
            showErrorAlert(departmentStage, ERROR_TITLE, DELETE_DEPARTMENT_FAILED);
        }
    }

    private boolean validateFields() {
        clearFieldStyles(departmentNameField);

        boolean isValid = true;

        if (departmentNameField.getText().isEmpty()) {
            highlightInvalidFields(departmentNameField);
            showErrorAlert(departmentStage, ERROR_TITLE, ENTER_DEPARTMENT_NAME);
            isValid = false;
        }
        return isValid;
    }

    private void fillFieldsWithSelectedDepartment(Department department) {
        departmentNameField.setText(department.getName());
    }
}