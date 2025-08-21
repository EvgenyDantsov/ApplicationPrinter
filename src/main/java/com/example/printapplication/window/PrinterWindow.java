package com.example.printapplication.window;

import com.example.printapplication.dao.OfficeDAO;
import com.example.printapplication.dao.PrinterDAO;

import com.example.printapplication.dto.Office;
import com.example.printapplication.dto.Printer;
import com.example.printapplication.util.WindowUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import static com.example.printapplication.util.AlertUtils.*;
import static com.example.printapplication.util.WindowUtils.*;

public class PrinterWindow {
    private TableView<Printer> table;
    private ObservableList<Printer> printList;
    private ObservableList<Office> officeList; // Список кабинетов
    private TextField printNameField;
    private TextField modelField;
    private TextField snNumberField;
    private TextField noteField;
    private TextField filterOfficeField; // Поле для ввода номера кабинета
    private Button clearFilterButton; // Кнопка для сброса фильтра
    private ComboBox<Office> officeComboBox; // Выпадающий список для выбора номера кабинета
    private ComboBox<String> statusComboBox;
    private Stage printStage; // Сохраняем родительское окно
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String SELECT_PRINTER = "Выберите принтер для изменения.";
    private static final String SN_NUMBER_UNIQUE = "Серийный номер должен быть уникальным.";
    private static final String ADD_PRINTER_FAILED = "Не удалось добавить принтер.";
    private static final String UPDATE_PRINTER_FAILED = "Не удалось обновить принтер.";
    private static final String SELECT_OFFICE = "Выберите номер кабинета.";
    private static final String SELECT_PRINTER_TO_DELETE = "Выберите принтер для удаления.";

    public void start(Stage printStage, Stage parentStage) {
        this.printStage = printStage;
        printStage.setTitle("Принтеры");
        // Инициализация таблицы
        table = new TableView<>();
        printList = FXCollections.observableArrayList();
        officeList = FXCollections.observableArrayList(); // Инициализация списка отделов

        // Колонки таблицы
        TableColumn<Printer, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Printer, String> nameColumn = new TableColumn<>("Название принтера");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("printerName"));
        TableColumn<Printer, String> modelColumn = new TableColumn<>("Модель принтера");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        TableColumn<Printer, String> snColumn = new TableColumn<>("Серийный номер");
        snColumn.setCellValueFactory(new PropertyValueFactory<>("snNumber"));
        TableColumn<Printer, String> noteColumn = new TableColumn<>("Примечание");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<Printer, Integer> officeIdColumn = new TableColumn<>("ID Кабинета");
        officeIdColumn.setCellValueFactory(new PropertyValueFactory<>("officeId"));
        // Новая колонка для статуса
        TableColumn<Printer, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusTableCellFactory());

        Collections.addAll(table.getColumns(), idColumn, nameColumn, modelColumn, snColumn, noteColumn, statusColumn, officeIdColumn);
        // Заполнение таблицы данными
        loadPrints();
        loadOffice(); // Загружаем список отделов

        // Поля для ввода данных
        printNameField = new TextField();
        printNameField.setPromptText("Название принтера...");
        modelField = new TextField();
        modelField.setPromptText("Модель...");
        snNumberField = new TextField();
        snNumberField.setPromptText("Серийный номер...");
        noteField = new TextField();
        noteField.setPromptText("Примечание...");
        // Добавляем элементы для фильтрации
        filterOfficeField = new TextField();
        filterOfficeField.setPromptText("Введите номер кабинета...");

        clearFilterButton = new Button("Сбросить фильтр");
        clearFilterButton.setOnAction(e -> {
            filterOfficeField.clear();
            table.setItems(printList); // Показываем все записи
        });
        // Слушатель для фильтрации при вводе текста
        filterOfficeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(printList);
            } else {
                filterTableByOfficeNumber(newVal);
            }
        });
        // Выпадающий список для выбора кабинета
        officeComboBox = new ComboBox<>(officeList);
        officeComboBox.setPromptText("Номер кабинета");
        // Новый комбобокс для статуса
        statusComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Установлен", "На хранении", "Списан"
        ));
        statusComboBox.setPromptText("Статус принтера");
        statusComboBox.getSelectionModel().selectFirst(); // По умолчанию "Активный"

        setupValidationListeners();
        officeComboBox.setConverter(new StringConverter<Office>() {
            @Override
            public String toString(Office office) {
                return office.getNumberOffice(); // Отображаем название кабинета
            }

            @Override
            public Office fromString(String string) {
                return officeComboBox.getItems().stream()
                        .filter(office -> office.getNumberOffice().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        // Слушатель для заполнения полей при выборе принтера
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithSelectedPrint(newSelection); // Заполняем поля данными выбранного принтера
            }
        });
        // Кнопки для добавления, изменения и удаления
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addPrint());
        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updatePrint());
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deletePrint());
        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> printStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton, clearFilterButton);

        // Форма для ввода данных
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Название принтера:"), printNameField);
        inputForm.addRow(0, new Label("Модель:"), modelField);
        inputForm.addRow(1, new Label("Серийный номер:"), snNumberField);
        inputForm.addRow(1, new Label("Фильтр по номеру кабинета:"), filterOfficeField);
        inputForm.addRow(2, new Label("Примечание:"), noteField);
        inputForm.addRow(2, new Label("Статус:"), statusComboBox);
        inputForm.addRow(3, new Label("Номер кабинета:"), officeComboBox);
        inputForm.add(clearFilterButton, 3, 3); // Колонка 3, строка 2
        GridPane.setHalignment(clearFilterButton, HPos.LEFT); // Выравнивание по левому краю

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 800, 500);
        printStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        // Позиционирование окна по центру относительно родительского окна
        printStage.setOnShown(event -> centerStageOnParent(printStage, parentStage));
        printStage.show();
    }

    // Загрузка данных из базы данных в таблицу
    private void loadPrints() {
        printList.setAll(PrinterDAO.getAllPrints());
        table.setItems(printList);
    }

    // Метод для фильтрации таблицы по номеру кабинета
    private void filterTableByOfficeNumber(String officeNumber) {
        ObservableList<Printer> filteredList = printList.filtered(p -> {
            // Находим кабинет по ID принтера
            Office office = officeList.stream()
                    .filter(o -> o.getId() == p.getOfficeId())
                    .findFirst()
                    .orElse(null);

            // Проверяем, содержит ли номер кабинета введенный текст (без учета регистра)
            return office != null && office.getNumberOffice().toLowerCase()
                    .contains(officeNumber.toLowerCase());
        });
        table.setItems(filteredList);
    }

    // Загрузка данных из базы данных в таблицу
    private void loadOffice() {
        officeList.setAll(OfficeDAO.getAllOffice());
    }

    // Добавление нового принтера
    private void addPrint() {
        // Проверяем обязательные поля
        if (!validateFields()) return;
        // Проверяем, выбран ли кабинет
        if (officeComboBox.getValue() == null) {
            highlightInvalidComboBox(officeComboBox);
            showErrorAlert(printStage, ERROR_TITLE, SELECT_OFFICE);
            return;
        }
        PrintFormData printFormData = getPrintFormData();
        String status = getStatusFromComboBox();
        // Проверка уникальности snNumber
        if (PrinterDAO.isSnNumberUnique(printFormData.snNumber, -1)) { // -1 означает, что это новая запись
            showErrorAlert(printStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (PrinterDAO.addPrint(printFormData.printerName, printFormData.model, printFormData.snNumber, printFormData.note, status, printFormData.office.getId())) {
            loadPrints();
            clearFields();
        } else {
            showErrorAlert(printStage, ERROR_TITLE, ADD_PRINTER_FAILED);
        }
    }

    // Обновление выбранного принтера
    private void updatePrint() {
        // Очищаем стили перед проверкой
        if (!validateFields()) return;
        Printer selectedPrint = table.getSelectionModel().getSelectedItem();
        if (selectedPrint == null) {
            showErrorAlert(printStage, ERROR_TITLE, SELECT_PRINTER);
            return;
        }
        PrintFormData printFormData = getPrintFormData();
        String status = getStatusFromComboBox();
        // Проверка уникальности snNumber (исключая текущую запись)
        if (PrinterDAO.isSnNumberUnique(printFormData.snNumber, selectedPrint.getId())) {
            showErrorAlert(printStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (PrinterDAO.updatePrint(selectedPrint.getId(), printFormData.printerName, printFormData.model, printFormData.snNumber, printFormData.note, status, printFormData.office.getId())) {
            loadPrints();
            clearFields();
        } else {
            showErrorAlert(printStage, ERROR_TITLE, UPDATE_PRINTER_FAILED);
        }
    }

    // Удаление выбранного принтера
    private void deletePrint() {
        Printer selectedPrint = table.getSelectionModel().getSelectedItem();
        if (selectedPrint == null) {
            showErrorAlert(printStage, ERROR_TITLE, SELECT_PRINTER_TO_DELETE);
            return;
        }
        if (PrinterDAO.deletePrint(selectedPrint.getId())) {
            loadPrints();
            clearFields();
        } else {
            showErrorAlert(printStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
        }
    }

    private void setupValidationListeners() {
        printNameField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(printNameField));
        modelField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(modelField));
        snNumberField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(snNumberField));
        officeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(officeComboBox));
    }

    private boolean validateFields() {
        clearFieldStyles(printNameField, modelField, snNumberField);
        clearComboBoxStyle(officeComboBox);

        boolean isValid = true;

        if (printNameField.getText().isEmpty() ||
                modelField.getText().isEmpty() ||
                snNumberField.getText().isEmpty() ||
                officeComboBox.getValue() == null) {

            highlightInvalidFields(printNameField, modelField, snNumberField);
            highlightInvalidComboBox(officeComboBox);
            showErrorAlert(printStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    // Метод для получения данных из формы и проверки обязательных полей
    private PrintFormData getPrintFormData() {
        String printerName = printNameField.getText();
        String model = modelField.getText();
        String snNumber = snNumberField.getText();
        String note = noteField.getText();
        Office selectedOffice = officeComboBox.getValue();

        return new PrintFormData(printerName, model, snNumber, note, selectedOffice);
    }

    // Вспомогательный класс для хранения данных формы
    private static class PrintFormData {
        String printerName, model, snNumber, note;
        Office office;

        PrintFormData(String printerName, String model, String snNumber, String note, Office office) {
            this.printerName = printerName;
            this.model = model;
            this.snNumber = snNumber;
            this.note = note;
            this.office = office;
        }
    }

    private void fillFieldsWithSelectedPrint(Printer print) {
        printNameField.setText(print.getPrinterName());
        modelField.setText(print.getModel());
        snNumberField.setText(print.getSnNumber());
        noteField.setText(print.getNote());
        // Устанавливаем статус
        String statusDisplayName = getStatusDisplayName(print.getStatus());
        statusComboBox.getSelectionModel().select(statusDisplayName);
        // Находим соответствующий Office в списке officeList
        Office selectedOffice = officeList.stream()
                .filter(office -> office.getId() == print.getOfficeId())
                .findFirst()
                .orElse(null);
        officeComboBox.getSelectionModel().select(selectedOffice);
    }

    // Очистка полей ввода
    private void clearFields() {
        printNameField.clear();
        modelField.clear();
        snNumberField.clear();
        noteField.clear();
        officeComboBox.getSelectionModel().clearSelection();
    }
    private String getStatusFromComboBox() {
        String selectedStatus = statusComboBox.getValue();
        return switch (selectedStatus) {
            case "Установлен" -> "active";
            case "На хранении" -> "in_storage";
            case "Списан" -> "written_off";
            default -> "active";
        };
    }
}