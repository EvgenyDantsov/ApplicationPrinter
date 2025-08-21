package com.example.printapplication.window;

import com.example.printapplication.dao.DepartmentDAO;
import com.example.printapplication.dao.OfficeDAO;
import com.example.printapplication.dto.Department;
import com.example.printapplication.dto.Office;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Collections;

import static com.example.printapplication.FieldValidator.*;
import static com.example.printapplication.util.AlertUtils.showErrorAlert;
import static com.example.printapplication.util.WindowUtils.centerStageOnParent;

public class OfficeWindow {
    private TableView<Office> table;
    private ObservableList<Department> departmentList;
    private ObservableList<Office> officeList; // Список отделов
    private TextField numberOfficeField;
    private TextField nameOfficeField;
    private ComboBox<Department> departmentComboBox; // Выпадающий список для выбора отдела
    private Stage officeStage; // Сохраняем родительское окно
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String SELECT_OFFICE = "Выберите кабинет для изменения.";
    private static final String NUMBER_OFFICE_UNIQUE = "Номер кабинета должен быть уникальным";
    private static final String ADD_OFFICE_FAILED = "Не удалось добавить кабинет";
    private static final String UPDATE_OFFICE_FAILED = "Не удалось обновить принтер.";
    private static final String SELECT_DEPARTMENT_NAME = "Выберите название отделения.";
    private static final String SELECT_OFFICE_TO_DELETE = "Выберите кабинет для удаления.";

    public void start(Stage officeStage, Stage parentStage) {
        this.officeStage = officeStage;
        officeStage.setTitle("Кабинеты");
        // Инициализация таблицы
        table = new TableView<>();
        officeList = FXCollections.observableArrayList();
        departmentList = FXCollections.observableArrayList(); // Инициализация списка отделов
        // Колонки таблицы
        TableColumn<Office, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Office, String> numberColumn = new TableColumn<>("Номер кабинета");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("numberOffice"));
        TableColumn<Office, String> nameColumn = new TableColumn<>("Название кабинета");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nameOffice"));
        TableColumn<Office, Integer> departmentIdColumn = new TableColumn<>("ID Отдела");
        departmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
        Collections.addAll(table.getColumns(), idColumn, numberColumn, nameColumn, departmentIdColumn);
        idColumn.setPrefWidth(50);
        numberColumn.setPrefWidth(110);
        nameColumn.setPrefWidth(350);
        departmentIdColumn.setPrefWidth(50);
        // Заполнение таблицы данными
        loadOffice();
        loadDepartment(); // Загружаем список отделов

        // Поля для ввода данных
        numberOfficeField = new TextField();
        numberOfficeField.setPromptText("Номер кабинета");
        numberOfficeField.setPrefWidth(100); // Устанавливаем предпочтительную ширину
        numberOfficeField.setMaxWidth(100); // Ограничиваем максимальную ширину
        numberOfficeField.setMinWidth(100); // Ограничиваем минимальную ширину
        nameOfficeField = new TextField();
        nameOfficeField.setPromptText("Название кабинета");
        // Поле для названия кабинета (шире)
        nameOfficeField.setPrefWidth(250); // Устанавливаем предпочтительную ширину
        nameOfficeField.setMaxWidth(250); // Ограничиваем максимальную ширину
        nameOfficeField.setMinWidth(250); // Ограничиваем минимальную ширину
        // Выпадающий список для выбора отдела
        departmentComboBox = new ComboBox<>(departmentList);
        departmentComboBox.setPromptText("Название отделения");
        departmentComboBox.setPrefWidth(200); // Устанавливаем предпочтительную ширину
        departmentComboBox.setMaxWidth(200); // Ограничиваем максимальную ширину
        departmentComboBox.setMinWidth(200); // Ограничиваем минимальную ширину
        setupValidationListeners();
        departmentComboBox.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                return department.getName(); // Отображаем название отдела
            }

            @Override
            public Department fromString(String string) {
                return departmentComboBox.getItems().stream()
                        .filter(department -> department.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        // Слушатель для заполнения полей при выборе кабинета
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithSelectedPrint(newSelection); // Заполняем поля данными выбранного кабинета
            }
        });
        // Кнопки для добавления, изменения и удаления
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addOffice());
        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateOffice());
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteOffice());
        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> officeStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);
        // Форма для ввода данных
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Номер кабинета:"), numberOfficeField);
        inputForm.addRow(1, new Label("Название кабинета:"), nameOfficeField);
        inputForm.addRow(2, new Label("Название отделения:"), departmentComboBox);

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 600, 400);
        officeStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        // Позиционирование окна по центру относительно родительского окна
        officeStage.setOnShown(event -> centerStageOnParent(officeStage, parentStage));
        officeStage.show();
    }

    // Загрузка данных из базы данных в таблицу
    private void loadOffice() {
        officeList.setAll(OfficeDAO.getAllOffice());
        // Устанавливаем политику изменения ширины столбцов
        table.setItems(officeList);
    }

    // Загрузка данных из базы данных в таблицу
    private void loadDepartment() {
        departmentList.setAll(DepartmentDAO.getAllDepartments());
    }

    // Добавление нового кабинета
    private void addOffice() {
        // Проверяем обязательные поля
        if (!validateFields()) return;
        // Проверяем, выбрано ли отделение
        if (departmentComboBox.getValue() == null) {
            highlightInvalidComboBox(departmentComboBox);
            showErrorAlert(officeStage, ERROR_TITLE, SELECT_DEPARTMENT_NAME);
            return;
        }
        OfficeFormData officeFormData = getOfficeFormData();
        if (OfficeDAO.addOffice(officeFormData.numberOffice, officeFormData.nameOffice, officeFormData.department.getId())) {
            loadOffice();
            clearFields();
        } else {
            showErrorAlert(officeStage, ERROR_TITLE, ADD_OFFICE_FAILED);
        }
    }

    // Обновление выбранного кабинета
    private void updateOffice() {
        // Очищаем стили перед проверкой
        if (!validateFields()) return;
        Office selectedOffice = table.getSelectionModel().getSelectedItem();
        if (selectedOffice == null) {
            showErrorAlert(officeStage, ERROR_TITLE, SELECT_OFFICE);
            return;
        }
        OfficeFormData officeFormData = getOfficeFormData();
        if (OfficeDAO.updateOffice(selectedOffice.getId(), officeFormData.numberOffice, officeFormData.nameOffice, officeFormData.department.getId())) {
            loadOffice();
            clearFields();
        } else {
            showErrorAlert(officeStage, ERROR_TITLE, UPDATE_OFFICE_FAILED);
        }
    }

    // Удаление выбранного принтера
    private void deleteOffice() {
        Office selectedOffice = table.getSelectionModel().getSelectedItem();
        if (selectedOffice == null) {
            showErrorAlert(officeStage, ERROR_TITLE, SELECT_OFFICE_TO_DELETE);
            return;
        }
        if (OfficeDAO.deleteOffice(selectedOffice.getId())) {
            loadOffice();
            clearFields();
        } else {
            showErrorAlert(officeStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
        }
    }

    private boolean validateFields() {
        clearFieldStyles(numberOfficeField, nameOfficeField);
        clearComboBoxStyle(departmentComboBox);

        boolean isValid = true;

        if (numberOfficeField.getText().isEmpty() ||
                nameOfficeField.getText().isEmpty() ||
                departmentComboBox.getValue() == null) {

            highlightInvalidFields(numberOfficeField, nameOfficeField);
            highlightInvalidComboBox(departmentComboBox);
            showErrorAlert(officeStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    private void setupValidationListeners() {
        numberOfficeField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(numberOfficeField));
        nameOfficeField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(nameOfficeField));
        departmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(departmentComboBox));
    }

    // Метод для получения данных из формы и проверки обязательных полей
    private OfficeFormData getOfficeFormData() {
        String numberOffice = numberOfficeField.getText();
        String nameOffice = nameOfficeField.getText();
        Department selectedDepartment = departmentComboBox.getValue();
        return new OfficeFormData(numberOffice, nameOffice, selectedDepartment);
    }

    // Вспомогательный класс для хранения данных формы
    private static class OfficeFormData {
        String numberOffice, nameOffice;
        Department department;

        OfficeFormData(String numberOffice, String nameOffice, Department department) {
            this.numberOffice = numberOffice;
            this.nameOffice = nameOffice;
            this.department = department;
        }
    }

    private void fillFieldsWithSelectedPrint(Office office) {
        numberOfficeField.setText(office.getNumberOffice());
        nameOfficeField.setText(office.getNameOffice());

        // Находим соответствующий Department в списке departmentList
        Department selectedDepartment = departmentList.stream()
                .filter(department -> department.getId() == office.getDepartmentId())
                .findFirst()
                .orElse(null);
        departmentComboBox.getSelectionModel().select(selectedDepartment);
    }

    // Очистка полей ввода
    private void clearFields() {
        numberOfficeField.clear();
        nameOfficeField.clear();
        departmentComboBox.getSelectionModel().clearSelection();
    }
}