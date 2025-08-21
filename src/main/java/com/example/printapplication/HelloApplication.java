package com.example.printapplication;

import com.example.printapplication.window.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Учет принтеров");

        // Создание кнопок для начального окна
        Button startButton = new Button("Начать");
        startButton.setOnAction(e -> {
            primaryStage.close();
            openMainWindow(primaryStage);
        });
        Button exitButton = new Button("Выход");
        exitButton.setOnAction(e -> System.exit(0));

        // Размещение кнопок в строку
        HBox topButtons = new HBox(20, startButton);  // горизонтальная панель с кнопками
        topButtons.setAlignment(Pos.CENTER);
        topButtons.setPadding(new Insets(10));

        // Кнопка выхода внизу в правом углу
        HBox bottomExitButton = new HBox();
        bottomExitButton.getChildren().add(exitButton);
        bottomExitButton.setAlignment(Pos.BOTTOM_RIGHT);  // выравнивание по правому нижнему углу
        bottomExitButton.setPadding(new Insets(10));

        // Основной контейнер с вертикальным расположением
        VBox mainLayout = new VBox(0, topButtons);// Уменьшаем отступ между элементами
        mainLayout.setPadding(new Insets(10));// Уменьшаем общий отступ
        mainLayout.getChildren().add(bottomExitButton); // Добавление кнопки выхода внизу

        // Установка сцены
        Scene scene = new Scene(mainLayout, 280, 120);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openMainWindow(Stage primaryStage) {
	LoadingWindow loadingWindow = new LoadingWindow();
    	loadingWindow.show();
        // Создаем новое окно
        Stage mainStage = new Stage();
     new Thread(() -> {
        try {
            // Обновляем сообщение
            Platform.runLater(() -> loadingWindow.updateMessage("Подключение к базе данных..."));
            
            // Инициализация базы данных
            DatabaseHelper.getConnection();
            
            // Обновляем сообщение
            Platform.runLater(() -> loadingWindow.updateMessage("Загрузка данных..."));
            
            // Создаем и инициализируем главное окно
            MainWindow mainWindow = new MainWindow();
            
            // В UI-потоке запускаем главное окно
            Platform.runLater(() -> {
                mainWindow.start(mainStage, primaryStage);
                loadingWindow.close();
                mainStage.show();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                loadingWindow.close();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не удалось загрузить данные");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }).start();
}

    @Override
    public void stop() {
        DatabaseHelper.disconnect();
    }
}