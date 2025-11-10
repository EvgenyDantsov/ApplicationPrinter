package com.example.equipmentapplication.window;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dao.EquipmentTypeDAO;
import com.example.equipmentapplication.dao.OfficeDAO;
import com.example.equipmentapplication.dao.EquipmentDAO;
import com.example.equipmentapplication.dto.UltrasoundSensor;
import com.example.equipmentapplication.dao.UltrasoundSensorDAO;
import com.example.equipmentapplication.dto.EquipmentType;
import com.example.equipmentapplication.dto.MainRecord;
import com.example.equipmentapplication.dto.Office;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.AlertUtils.showInformationAlert;
import static com.example.equipmentapplication.util.WindowUtils.*;

public class MainWindow {
    private TableView<MainRecord> table;
    private ObservableList<MainRecord> allDataList = FXCollections.observableArrayList();
    private Stage mainStage;
    private TextField officeNameFilter;
    private TextField numberOfficeFilter;
    private TextField departmentFilter;
    private TextField equipmentFilter;
    private TextField modelFilter;
    private TextField snFilter;
    private TextField noteFilter;
    private TextField statusFilter;
    private TextField fioFilter;
    private FilteredList<MainRecord> filteredData;
    private Label totalEquipmentLabel;
    private Label totalDepartmentsLabel;
    private Label totalResponsiblesLabel;
    private Label totalFilteredLabel;
    private Label lastUpdateLabel;
    private Label totalOfficesLabel;
    private Label totalStorageLabel;
    private Label totalWrittenOffLabel;
    private ComboBox<EquipmentType> equipmentTypeCombo; // NEW
    private ToolBar statusBar;
    private String activeStatusFilter = null;
    //private UltrasoundSensorDAO ultrasoundSensorDao;
    private int ultrasoundTypeId = -1;

    public void start(Stage mainStage, Stage primaryStage) {
        this.mainStage = mainStage;
        mainStage.setTitle("Главное окно");
        // Инициализация UI компонентов
        initializeUI();
        setupFiltering();
        setupContextMenu();
        loadEquipmentTypeId();

        mainStage.setOnShown(event -> centerStageOnParent(mainStage, primaryStage));
    }

    private void initializeUI() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("Файл");
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> mainStage.close());
        fileMenu.getItems().add(exitItem);
        //Оборудование
        Menu equipmentMenu = new Menu("Учет");
        // динамически добавляем типы
        EquipmentTypeDAO.getAllEquipmentTypes().forEach(type -> {
            MenuItem typeItemMI = new MenuItem(type.getName());
            typeItemMI.setOnAction(e2 -> openEquipmentWindow(type)); // перегрузка ниже
            equipmentMenu.getItems().add(typeItemMI);
        });
        //Организация
        Menu orgMenu = new Menu("Организация");
        MenuItem depItem = new MenuItem("Отделения");
        depItem.setOnAction(e -> openDepartmentWindow());
        MenuItem officeItem = new MenuItem("Кабинеты");
        officeItem.setOnAction(e -> openOfficeWindow());
        MenuItem seniorItem = new MenuItem("Старшие отделений");
        seniorItem.setOnAction(e -> openSeniorDepartmentWindow());
        orgMenu.getItems().addAll(depItem, seniorItem, officeItem);
        //Справочник
        Menu dictionaryMenu = new Menu("Справочники");
        MenuItem manageItem = new MenuItem("Тип оборудования");
        manageItem.setOnAction(e -> openEquipmentTypeWindow());
        MenuItem equipmentDictionaryItem = new MenuItem("Справочник оборудования");
        equipmentDictionaryItem.setOnAction(e -> openEquipmentDictionaryWindow());
        dictionaryMenu.getItems().addAll(manageItem, equipmentDictionaryItem);

        //Отчеты
        Menu reportMenu = new Menu("Отчеты");
        MenuItem genReport = new MenuItem("Сформировать отчет");
        genReport.setOnAction(e -> generateReport());
        reportMenu.getItems().add(genReport);
        // Добавляем меню в меню-бар
        menuBar.getMenus().addAll(fileMenu, equipmentMenu, orgMenu, dictionaryMenu, reportMenu);
        // Создание кнопок для работы с таблицами
        equipmentTypeCombo = new ComboBox<>(); // NEW
        equipmentTypeCombo.setPromptText("Все типы оборудования"); // NEW
        refreshEquipmentTypeCombo(); // NEW - отдельный метод
        equipmentTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> { // NEW
            if (newVal != null) {
                activeStatusFilter = null;
                if (newVal.getId() == -1) { // спец.пункт "обновить"
                    EquipmentType currentType = oldVal != null ? oldVal : new EquipmentType(0, "Все");
                    reloadByType(currentType);
                    // Возвращаем выбранное значение обратно
                    equipmentTypeCombo.setValue(currentType);
                } else {
                    reloadByType(newVal);
                }
                applyFilters();
            }
        });
        equipmentTypeCombo.setOnAction(evt -> {
            EquipmentType selected = equipmentTypeCombo.getValue();
            if (selected == null) return;

            // Сброс фильтра по статусу (пользователь сменил тип -> статусный фильтр снимаем)
            activeStatusFilter = null;

            // Перезагрузим данные в allDataList в зависимости от типа
            reloadByType(selected);
            resetFilters();
        });
        // Чтобы гарантированно обрабатывать "повторный клик" на уже выбранном значении,
        // добавим простую обработку на мышь — если пользователь кликнул по combobox, то
        // снова перезагрузим текущий тип (полезно, если пользователь хочет "обновить" представление)
        equipmentTypeCombo.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            EquipmentType selected = equipmentTypeCombo.getValue();
            if (selected != null) {
                // Снимаем статусный фильтр при явном клике (как пользователь ожидает)
                activeStatusFilter = null;
                reloadByType(selected);
                resetFilters();
            }
        });
        statusBar = createStatusBar();
        Button closeButton = new Button("Выход");
        closeButton.getStyleClass().add("exit-button");
        closeButton.setOnAction(e -> mainStage.close());
        table = new TableView<>();
        // Создание колонок таблицы
        TableColumn<MainRecord, String> officeNameColumn = new TableColumn<>("Название кабинета");
        officeNameColumn.setCellValueFactory(new PropertyValueFactory<>("nameOffice"));
        TableColumn<MainRecord, String> numberOfficeColumn = new TableColumn<>("Кабинет");
        numberOfficeColumn.setCellValueFactory(new PropertyValueFactory<>("numberOffice"));
        TableColumn<MainRecord, String> departmentColumn = new TableColumn<>("Отделение");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("nameDepartment"));
        TableColumn<MainRecord, String> equipmentColumn = new TableColumn<>("Оборудование");
        equipmentColumn.setStyle("-fx-alignment: CENTER;");
        equipmentColumn.setCellValueFactory(new PropertyValueFactory<>("nameEquipment"));
        TableColumn<MainRecord, String> modelColumn = new TableColumn<>("Модель");
        modelColumn.setStyle("-fx-alignment: CENTER;");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        TableColumn<MainRecord, String> snColumn = new TableColumn<>("Серийный номер");
        snColumn.setStyle("-fx-alignment: CENTER;");
        snColumn.setCellValueFactory(new PropertyValueFactory<>("snNumber"));
        TableColumn<MainRecord, String> noteColumn = new TableColumn<>("Заметки");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<MainRecord, String> statusColumn = new TableColumn<>("Cтатус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusTableCellFactory());
        TableColumn<MainRecord, String> fioColumn = new TableColumn<>("Старшая отделения");
        fioColumn.setCellValueFactory(new PropertyValueFactory<>("fio"));
        Collections.addAll(table.getColumns(), numberOfficeColumn, officeNameColumn, equipmentColumn, modelColumn, snColumn, departmentColumn, noteColumn, statusColumn, fioColumn);
        loadAllData();
        filteredData = new FilteredList<>(allDataList, p -> true);
        table.setItems(filteredData);
        // Размещение кнопок в верхней части окна (горизонтально)
        HBox filterControls = new HBox(10, equipmentTypeCombo);
        filterControls.setAlignment(Pos.CENTER_LEFT);
        filterControls.setPadding(new Insets(10));
        filterControls.getStyleClass().add("top-buttons");

        // Размещение кнопки выхода внизу окна
        HBox bottomExitButton = new HBox();
        bottomExitButton.getChildren().add(closeButton);
        bottomExitButton.setAlignment(Pos.BOTTOM_RIGHT); // Выравнивание по правому нижнему углу
        bottomExitButton.setPadding(new Insets(10));
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(5));
        filterBox.getStyleClass().add("filter-box");

        officeNameFilter = createFilterField("Название кабинета...");
        numberOfficeFilter = createFilterField("Кабинет...");
        departmentFilter = createFilterField("Отделение...");
        equipmentFilter = createFilterField("Оборудование...");
        modelFilter = createFilterField("Модель...");
        snFilter = createFilterField("Серийный номер...");
        noteFilter = createFilterField("Заметки...");
        statusFilter = createFilterField("Статус...");
        fioFilter = createFilterField("Старшая отделения...");

        // Добавляем все поля в HBox
        filterBox.getChildren().addAll(numberOfficeFilter, officeNameFilter, equipmentFilter, modelFilter, snFilter, departmentFilter, noteFilter, statusFilter, fioFilter);
        VBox mainLayout = new VBox(menuBar, filterControls, table, filterBox, bottomExitButton, statusBar);
        VBox.setVgrow(table, Priority.ALWAYS); // Таблица будет растягиваться
        Scene mainScene = new Scene(mainLayout, 1200, 600);
        URL stylesheetUrl = getClass().getResource("/styles.css");
        mainScene.getStylesheets().add(stylesheetUrl != null ? stylesheetUrl.toExternalForm() : "");
        setupFiltering();
        //setupContextMenu();
        mainStage.setScene(mainScene);
    }

    private TextField createFilterField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setMaxWidth(130); // Устанавливаем ширину
        textField.getStyleClass().add("filter-field");
        return textField;
    }

    private ToolBar createStatusBar() {
        statusBar = new ToolBar();
        statusBar.getStyleClass().add("status-bar");
        // Создаем иконки или decorative элементы
        Label equipmentIcon = createIconLabel("\uD83D\uDCE0", "Оборудования: 0");
        Label departmentIcon = createIconLabel("\uD83C\uDFDB", "Отделения: 0");
        Label responsibleIcon = createIconLabel("\uD83D\uDC68\u200D\uD83D\uDCBC", "Ответственные: 0");
        Label officeIcon = createIconLabel("\uD83D\uDEAA", "Кабинеты: 0");
        Label storageIcon = createIconLabel("\uD83D\uDCE6", "На хранении: 0"); // 📦
        Label writtenOffIcon = createIconLabel("❌", "Списано: 0"); // 📛
        totalFilteredLabel = new Label("📋 0/0");
        lastUpdateLabel = new Label("🕐 --:--");
        totalEquipmentLabel = equipmentIcon;
        totalDepartmentsLabel = departmentIcon;
        totalResponsiblesLabel = responsibleIcon;
        totalOfficesLabel = officeIcon;
        totalStorageLabel = storageIcon;
        totalWrittenOffLabel = writtenOffIcon;
        // Добавляем клики на "На хранении" и "Списано"
        // --- Фильтры по статусу ---
        totalStorageLabel.setOnMouseClicked(e -> {
            EquipmentType currentType = equipmentTypeCombo.getValue();

            long count = filteredData.stream()
                    .filter(r -> (currentType == null || currentType.getId() == 0 || r.getEquipmentTypeId() == currentType.getId()))
                    .filter(r -> "in_storage".equals(r.getStatus()))
                    .count();

            if (count == 0) {
                return; // ничего не делаем
            }
            activeStatusFilter = "in_storage";
            resetFilters();
            applyFilters();
        });

        totalWrittenOffLabel.setOnMouseClicked(e -> {
            EquipmentType currentType = equipmentTypeCombo.getValue();

            long count = filteredData.stream()
                    .filter(r -> (currentType == null || currentType.getId() == 0 || r.getEquipmentTypeId() == currentType.getId()))
                    .filter(r -> "written_off".equals(r.getStatus()))
                    .count();

            if (count == 0) {
                return; // ничего не делаем
            }
            activeStatusFilter = "written_off";
            resetFilters();
            applyFilters();
        });

        totalEquipmentLabel.setOnMouseClicked(e -> {
            activeStatusFilter = null;
            EquipmentType currentType = equipmentTypeCombo.getValue();
            if (currentType != null) {
                reloadByType(currentType);
            } else {
                loadAllData();
            }
            applyFilters();
            resetFilters();
        });
        // Стилизуем метки
        totalEquipmentLabel.getStyleClass().add("status-item");
        totalDepartmentsLabel.getStyleClass().add("status-item");
        totalResponsiblesLabel.getStyleClass().add("status-item");
        totalOfficesLabel.getStyleClass().add("status-item");
        totalStorageLabel.getStyleClass().add("status-item");
        totalWrittenOffLabel.getStyleClass().add("status-item");
        totalFilteredLabel.getStyleClass().add("status-item");
        lastUpdateLabel.getStyleClass().add("status-item-right");

        // Добавляем разделители с кастомными стилями
        Separator[] separators = new Separator[7];
        for (int i = 0; i < separators.length; i++) {
            separators[i] = createStyledSeparator();
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getItems().addAll(
                totalFilteredLabel, separators[0], // Самый важный - первым!
                totalEquipmentLabel, separators[1],
                totalDepartmentsLabel, separators[2],
                totalResponsiblesLabel, separators[3],
                totalOfficesLabel, separators[4],
                totalStorageLabel, separators[5],     // Новое
                totalWrittenOffLabel, separators[6],
                spacer,
                lastUpdateLabel
        );
        return statusBar;
    }

    // --- Новый метод: единый фильтр ---
    private void applyFilters() {
        if (filteredData == null) return;

        filteredData.setPredicate(record -> {
            boolean matchesType = true;
            boolean matchesStatus = true;

            // Фильтр по типу оборудования из комбобокса
            EquipmentType currentType = equipmentTypeCombo.getValue();
            if (currentType != null && currentType.getId() != 0) {
                matchesType = record.getEquipmentTypeId() == currentType.getId();
            }

            // Фильтр по статусу (на хранении/списано)
            if (activeStatusFilter != null) {
                matchesStatus = activeStatusFilter.equals(record.getStatus());
            }

            return matchesType && matchesStatus;
        });

        updateStatusBar();
    }

    private Label createIconLabel(String icon, String text) {
        Label label = new Label(icon + " " + text);
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setGraphicTextGap(5);
        return label;
    }

    private void refreshEquipmentTypeCombo() { // NEW
        List<EquipmentType> types = new ArrayList<>();
        types.add(new EquipmentType(0, "Все")); // универсальный пункт
        types.addAll(EquipmentTypeDAO.getAllEquipmentTypes());

        equipmentTypeCombo.setItems(FXCollections.observableArrayList(types));
        equipmentTypeCombo.setValue(types.get(0));
    }

    private Separator createStyledSeparator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.getStyleClass().add("status-separator");
        return separator;
    }

    private void updateStatusBar() {
        if (filteredData == null) {
            return;
        }
        // Обновляем статистику
        int equipmentCount = filteredData.size();
        long storageCount = filteredData.stream()
                .filter(record -> "in_storage".equals(record.getStatus()))
                .count();
        long writtenOffCount = filteredData.stream()
                .filter(record -> "written_off".equals(record.getStatus()))
                .count();
        long departmentCount = filteredData.stream()
                .map(MainRecord::getNameDepartment)
                .distinct()
                .count();
        long responsibleCount = filteredData.stream()
                .map(MainRecord::getFio)
                .filter(fio -> fio != null && !fio.trim().isEmpty())
                .distinct()
                .count();
        long officeCount = filteredData.stream()
                .map(MainRecord::getNumberOffice)
                .distinct()
                .count();
        int totalCount = allDataList.size();
        int filteredCount = filteredData.size();
        // Обновляем текст
        totalEquipmentLabel.setText("\uD83D\uDCE0 Оборудования: " + equipmentCount);
        totalDepartmentsLabel.setText("\uD83C\uDFDB Отделения: " + departmentCount);
        totalResponsiblesLabel.setText("\uD83D\uDC68\u200D\uD83D\uDCBC Ответственные: " + responsibleCount);
        totalOfficesLabel.setText("\uD83D\uDEAA Кабинеты: " + officeCount);
        totalStorageLabel.setText("\uD83D\uDCE6 На хранении: " + storageCount);      // Новое
        totalWrittenOffLabel.setText("❌ Списано: " + writtenOffCount);    // Новое
        totalFilteredLabel.setText("📋 " + filteredCount + "/" + totalCount);
        lastUpdateLabel.setText("🕐 " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void setupContextMenu() {
        table.setRowFactory(tv -> {
            TableRow<MainRecord> row = new TableRow<>();
            // Создаём меню один раз для строки
            ContextMenu rowMenu = new ContextMenu();
            // Обрабатываем запрос контекстного меню
            row.setOnContextMenuRequested(event -> {
                MainRecord selectedRecord = row.getItem();
                if (selectedRecord == null) return;
                rowMenu.getItems().clear();
                // Пункт "Переместить оборудование"
                MenuItem moveEquipmentItem = new MenuItem("Переместить оборудование");
                moveEquipmentItem.setOnAction(e -> {
                    // Используем выбранную строку для перемещения
                    if (selectedRecord != null) {
                        movePrinter();
                    }
                });
                rowMenu.getItems().add(moveEquipmentItem);

                // Пункт "Просмотреть датчики" только для УЗИ аппаратов
                if (selectedRecord.getEquipmentTypeId() == ultrasoundTypeId) {
                    MenuItem viewSensorsItem = new MenuItem("Просмотреть датчики");
                    viewSensorsItem.setOnAction(e -> {
                        MainRecord current = table.getSelectionModel().getSelectedItem();
                        if (current != null) {
                            UltrasoundSensorWindow sensorWindow = new UltrasoundSensorWindow(current.getEquipmentId());
                            Stage sensorStage = new Stage();
                            sensorWindow.start(sensorStage, mainStage); // открываем поверх главного окна
                        }
                    });
                    rowMenu.getItems().add(viewSensorsItem);
                }
                // Закрываем, если меню уже открыто (чтобы не копилось)
                if (rowMenu.isShowing()) {
                    rowMenu.hide();
                }
                rowMenu.show(row, event.getScreenX(), event.getScreenY());
            });

            return row;
        });
    }

    private void movePrinter() {
        MainRecord selectedRecord = table.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showErrorAlert(mainStage, "Ошибка", "Выберите оборудование для перемещения");
            return;
        }
        // Получаем список всех кабинетов и проверяем на null
        ObservableList<Office> offices = FXCollections.observableArrayList();
        List<Office> allOffices = OfficeDAO.getAllOffice();
        offices.addAll(allOffices);

        // Создаем диалоговое окно для выбора нового кабинета
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Перемещение оборудования");
        dialog.setHeaderText("Перемещение оборудования: " + selectedRecord.getNameEquipment() +
                "\nТекущий кабинет: " + selectedRecord.getNumberOffice() +
                "\nТекущий статус: " + getStatusDisplayName(selectedRecord.getStatus()));

        ButtonType moveButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(moveButtonType, ButtonType.CANCEL);
        ComboBox<Office> officeCombo = new ComboBox<>(offices);
        officeCombo.setConverter(new StringConverter<Office>() {
            @Override
            public String toString(Office office) {
                // Добавляем проверку на null
                return office == null ? "" : office.getNumberOffice() + " (" + office.getNameOffice() + ")";
            }

            @Override
            public Office fromString(String string) {
                return null;
            }
        });

        Office currentOffice = offices.stream()
                .filter(office -> office.getNumberOffice().equals(selectedRecord.getNumberOffice())).findFirst()
                .orElse(null);
        officeCombo.setValue(currentOffice);        // Добавляем комбобокс для выбора статуса
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Установлен", "На хранении", "Списан"));

        statusCombo.setPromptText("Выберите статус");
        String currentStatusDisplay = getStatusDisplayName(selectedRecord.getStatus());
        statusCombo.getSelectionModel().select(currentStatusDisplay);

        TextField noteField = new TextField();
        noteField.setPromptText("Введите примечание");
        noteField.setText(selectedRecord.getNote() != null ? selectedRecord.getNote() : "");

        // Устанавливаем обработчик для предотвращения выбора null значения
        officeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                officeCombo.setValue(oldVal);
            }
        });
        // Устанавливаем текущее примечание из выбранного принтера
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Новый кабинет:"), 0, 0);
        grid.add(officeCombo, 1, 0);
        grid.add(new Label("Новый статус:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(new Label("Примечание:"), 0, 2);
        grid.add(noteField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(officeCombo::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == moveButtonType) {
                // Получаем выбранный статус и преобразуем в формат БД
                String selectedStatus = statusCombo.getValue();
                String statusDb = switch (selectedStatus) {
                    case "Установлен" -> "active";
                    case "На хранении" -> "in_storage";
                    case "Списан" -> "written_off";
                    default -> selectedRecord.getStatus();
                };

                // Формируем обновленное примечание
                String updatedNote = noteField.getText();

                // Если статус изменился, добавляем информацию в примечание
                if (!selectedRecord.getStatus().equals(statusDb)) {
                    if (!updatedNote.isEmpty()) {
                        updatedNote += "\n";
                    }
                }
                return EquipmentDAO.moveEquipment(selectedRecord.getEquipmentId(),
                        officeCombo.getValue().getId(),
                        updatedNote, statusDb);
            }
            return false;
        });
        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(success -> {
            if (success) {
                showInformationAlert(mainStage, "Успех", "Оборудование успешно перемещено");
                loadAllData(); // Явно обновляем данные
                resetFilters();
                setupFiltering();
                updateStatusBar();
            } else {
                showErrorAlert(mainStage, "Ошибка", "Не удалось переместить оборудование");
            }
        });
    }

    private void setupFiltering() {
        if (filteredData == null) {
            filteredData = new FilteredList<>(allDataList, p -> true);
            table.setItems(filteredData);
        }

        List<TextField> filters = Arrays.asList(
                numberOfficeFilter, officeNameFilter, equipmentFilter,
                modelFilter, snFilter, departmentFilter, noteFilter, statusFilter, fioFilter
        );

        ChangeListener<String> filterListener = (observable, oldValue, newValue) -> {
            filteredData.setPredicate(record -> {
                // Сначала учитываем текущий статус и тип оборудования
                boolean matchesType = true;
                boolean matchesStatus = true;

                EquipmentType currentType = equipmentTypeCombo.getValue();
                if (currentType != null && currentType.getId() != 0) {
                    matchesType = record.getEquipmentTypeId() == currentType.getId();
                }

                if (activeStatusFilter != null) {
                    matchesStatus = activeStatusFilter.equals(record.getStatus());
                }

                // Теперь фильтры из текстовых полей
                boolean matchesTextFilters =
                        (numberOfficeFilter.getText().isEmpty() || record.getNumberOffice().toLowerCase().contains(numberOfficeFilter.getText().toLowerCase())) &&
                                (officeNameFilter.getText().isEmpty() || record.getNameOffice().toLowerCase().contains(officeNameFilter.getText().toLowerCase())) &&
                                (equipmentFilter.getText().isEmpty() || record.getNameEquipment().toLowerCase().contains(equipmentFilter.getText().toLowerCase())) &&
                                (modelFilter.getText().isEmpty() || record.getModel().toLowerCase().contains(modelFilter.getText().toLowerCase())) &&
                                (snFilter.getText().isEmpty() || record.getSnNumber().toLowerCase().contains(snFilter.getText().toLowerCase())) &&
                                (departmentFilter.getText().isEmpty() || record.getNameDepartment().toLowerCase().contains(departmentFilter.getText().toLowerCase())) &&
                                (noteFilter.getText().isEmpty() || record.getNote().toLowerCase().contains(noteFilter.getText().toLowerCase())) &&
                                (statusFilter.getText().isEmpty() || getStatusDisplayName(record.getStatus()).toLowerCase().contains(statusFilter.getText().toLowerCase())) &&
                                (fioFilter.getText().isEmpty() || record.getFio().toLowerCase().contains(fioFilter.getText().toLowerCase()));

                return matchesType && matchesStatus && matchesTextFilters;
            });

            updateStatusBar();
        };

        for (TextField filter : filters) {
            filter.textProperty().addListener(filterListener);
        }

        table.setItems(filteredData);
        updateStatusBar();
    }

    private void resetFilters() {
        numberOfficeFilter.setText("");
        officeNameFilter.setText("");
        equipmentFilter.setText("");
        modelFilter.setText("");
        snFilter.setText("");
        departmentFilter.setText("");
        noteFilter.setText("");
        statusFilter.setText("");
        fioFilter.setText("");
    }

    private void openDepartmentWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage departmentStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new DepartmentWindow().start(departmentStage, parentStage);
        // Обновляем таблицу после закрытия окна
        departmentStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openEquipmentTypeWindow() { // UPDATED
        Stage parentStage = (Stage) table.getScene().getWindow();
        Stage equipmentTypeStage = new Stage();
        new EquipmentTypeWindow().start(equipmentTypeStage, parentStage);
        equipmentTypeStage.setOnHidden(event -> {
            refreshEquipmentTypeCombo(); // после закрытия окна обновляем список
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openEquipmentDictionaryWindow() { // UPDATED
        Stage parentStage = (Stage) table.getScene().getWindow();
        Stage equipmentDictionaryStage = new Stage();
        new EquipmentDictionaryWindow().start(equipmentDictionaryStage, parentStage);
        equipmentDictionaryStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openEquipmentWindow(EquipmentType type) {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage equipmentStage = new Stage();
        EquipmentWindow equipmentWindow = new EquipmentWindow();
        equipmentWindow.setInitialTypeId(type.getId());     // <— добавь этот сеттер в EquipmentWindow
        equipmentWindow.setInitialTypeName(type.getName()); // опционально для заголовка окна
        equipmentWindow.start(equipmentStage, parentStage);
        equipmentStage.setOnHidden(e -> {
            // Обновляем данные в allDataList из базы
            List<MainRecord> updatedData = DatabaseHelper.getAllView(); // получаем все оборудование
            allDataList.setAll(updatedData);

            // Сохраняем выбранный тип и статус фильтра
            applyFilters(); // применяем текущие фильтры, ничего не сбрасывая
            updateStatusBar();
        });
    }

    private void openOfficeWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage officeStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new OfficeWindow().start(officeStage, parentStage);
        // Обновляем таблицу после закрытия окна
        officeStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openSeniorDepartmentWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage seniorDepartmentStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new SeniorDepartmentWindow().start(seniorDepartmentStage, parentStage);
        // Обновляем таблицу после закрытия окна
        seniorDepartmentStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void loadAllData() {
        allDataList.setAll(DatabaseHelper.getAllView());
        applyFilters();
    }

    private void loadDataByType(int typeId) {
        allDataList.setAll(DatabaseHelper.getAllViewByType(typeId));
        applyFilters();
    }

    private void reloadByType(EquipmentType type) {
        if (type == null) return;
        if (type.getId() == 0) {
            loadAllData();
        } else {
            loadDataByType(type.getId());
        }
        applyFilters();
    }

    private void loadEquipmentTypeId() {
        List<EquipmentType> types = EquipmentTypeDAO.getAllEquipmentTypes();
        for (EquipmentType type : types) {
            if ("Ультразвуковой аппарат".equalsIgnoreCase(type.getName())) {
                ultrasoundTypeId = type.getId();
                break;
            }
        }
    }

    private void generateReport() {
        Stage parentStage = (Stage) table.getScene().getWindow();
        // Создаем новый workbook и лист
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Отчет");
        // Создаем заголовки столбцов
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i <= 3; i++) { // 0..3 включительно = до "Модель"
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(table.getColumns().get(i).getText());
        }

// Вставляем новый заголовок "Датчики"
        Cell sensorsHeaderCell = headerRow.createCell(4);
        sensorsHeaderCell.setCellValue("Датчики");

// Копируем оставшиеся заголовки с TableView, сдвигаем индекс на +1
        for (int i = 4; i < table.getColumns().size(); i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(table.getColumns().get(i).getText());
        }

        for (int i = 0; i < filteredData.size(); i++) {
            Row row = sheet.createRow(i + 1);
            MainRecord record = filteredData.get(i);

            row.createCell(0).setCellValue(record.getNumberOffice());
            row.createCell(1).setCellValue(record.getNameOffice());
            row.createCell(2).setCellValue(record.getNameEquipment());
            row.createCell(3).setCellValue(record.getModel());

            // --- НОВЫЙ СТОЛБЕЦ ДАТЧИКОВ (после модели) ---
            Cell sensorsCell;
            if (record.getEquipmentTypeId() == ultrasoundTypeId) {
                List<UltrasoundSensor> sensors =
                        UltrasoundSensorDAO.getSensorsByEquipmentId(record.getEquipmentId());

                String sensorsInfo = sensors.stream()
                        .map(s -> s.getSensorName() + " (sn:" + s.getSnNumber() + ")" + "Тип: " + s.getSensorType())
                        .collect(Collectors.joining("\n"));

                sensorsCell = row.createCell(4);
                sensorsCell.setCellValue(sensorsInfo);

                // Включаем перенос строк
                CellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                sensorsCell.setCellStyle(style);

                // Устанавливаем высоту строки
                if (!sensors.isEmpty()) {
                    float baseHeight = sheet.getDefaultRowHeightInPoints();
                    row.setHeightInPoints(baseHeight * sensors.size());
                }
            } else {
                sensorsCell = row.createCell(4);
                sensorsCell.setCellValue("-"); // пустое значение для других типов
            }

            // Сдвигаем остальные колонки на +1
            row.createCell(5).setCellValue(record.getSnNumber());
            row.createCell(6).setCellValue(record.getNameDepartment());
            row.createCell(7).setCellValue(record.getNote());
            String statusInRussian = getStatusDisplayName(record.getStatus());
            row.createCell(8).setCellValue(statusInRussian);
            row.createCell(9).setCellValue(record.getFio());
        }
        int sensorsColumnIndex = table.getColumns().size();
        // Авторазмер для всех столбцов
        for (int i = 0; i < table.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
            // Увеличиваем ширину столбца на 2 символа для корректного отображения
            int currentWidth = sheet.getColumnWidth(i);
            int newWidth = currentWidth + 2 * 256; // 1 символ = 256 единиц 
            sheet.setColumnWidth(i, newWidth);
        }
        sheet.autoSizeColumn(sensorsColumnIndex);
        int sensorsWidth = sheet.getColumnWidth(sensorsColumnIndex);
        sheet.setColumnWidth(sensorsColumnIndex, sensorsWidth + 2 * 256);
        // Создаем таблицу в Excel
        CellRangeAddress range = new CellRangeAddress(0, allDataList.size(), 0, table.getColumns().size());
        sheet.setAutoFilter(range);
        // Формируем имя файла с текущей датой
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String fileName = "Отчет_" + LocalDate.now().format(formatter) + ".xlsx";
        // Сохраняем файл
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.setInitialFileName(fileName); // Устанавливаем имя файла
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(parentStage);

        if (file != null) {
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                workbook.close();
                showInformationAlert(parentStage, "Отчет сформирован", "Отчет успешно сохранен!");
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert(parentStage, "Ошибка", "Не удалось сохранить отчет.");
            }
        }
    }
}