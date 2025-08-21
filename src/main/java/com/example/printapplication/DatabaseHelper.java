package com.example.printapplication;

import com.example.printapplication.config.Config;
import com.example.printapplication.dto.MainRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.List;

public class DatabaseHelper {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = Config.get("URL_SQL");
            String user = Config.get("USER_NAME_SQL");
            String password = Config.get("PASSWORD_SQL");
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    public static List<MainRecord> getAllView() {
        ObservableList<MainRecord> records = FXCollections.observableArrayList();
        String sql = "SELECT \n" +
                "    Office.name_office, \n" +
                "    Office.number_office, \n" +
                "    Department.department_name, \n" +
                "    Printer.printer_name, \n" +
                "    Printer.model, \n" +
                "    Printer.sn_number, \n" +
                "    Printer.note, \n" +
                "    SeniorDepartment.fio,\n" +
                "    Printer.id\n" +
                "FROM \n" +
                "    Office\n" +
                "INNER JOIN \n" +
                "    Department ON Office.Department_id = Department.id\n" +
                "INNER JOIN \n" +
                "    Printer ON Office.id = Printer.Office_id\n" +
                "INNER JOIN \n" +
                "    SeniorDepartment ON Department.id = SeniorDepartment.Department_id";
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                MainRecord record = new MainRecord(
                        resultSet.getString("name_office"),
                        resultSet.getString("number_office"),
                        resultSet.getString("department_name"),
                        resultSet.getString("printer_name"),
                        resultSet.getString("model"),
                        resultSet.getString("sn_number"),
                        resultSet.getString("note"),
                        resultSet.getString("fio")
                );record.setPrinterId(resultSet.getInt("id")); // Устанавливаем ID принтера
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}