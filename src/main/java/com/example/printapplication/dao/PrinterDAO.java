package com.example.printapplication.dao;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dto.Printer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class PrinterDAO {
    public static ObservableList<Printer> getAllPrints() {
        ObservableList<Printer> printList = FXCollections.observableArrayList();
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT * FROM printer";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String printerName = resultSet.getString("printer_name");
                String model = resultSet.getString("model");
                String snNumber = resultSet.getString("sn_number");
                String note = resultSet.getString("note");
                int officeId = resultSet.getInt("Office_id");
                printList.add(new Printer(id, printerName, model, snNumber, note, officeId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return printList;
    }

    public static boolean addPrint(String printerName, String model, String snNumber, String note, int officeId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO printer (printer_name, model, sn_number, note, Office_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, printerName);
            statement.setString(2, model);
            statement.setString(3, snNumber);
            statement.setString(4, note);
            statement.setInt(5, officeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePrint(int id, String printerName, String model, String snNumber, String note, int officeId) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "UPDATE printer SET printer_name = ?, model = ?, sn_number = ?, note = ?, Office_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, printerName);
            statement.setString(2, model);
            statement.setString(3, snNumber);
            statement.setString(4, note);
            statement.setInt(5, officeId);
            statement.setInt(6, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deletePrint(int id) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String sql = "DELETE FROM printer WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Проверка уникальности snNumber
    public static boolean isSnNumberUnique(String snNumber, int excludeId) {
        String sql = "SELECT COUNT(*) FROM printer WHERE sn_number = ? AND id != ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, snNumber);
            statement.setInt(2, excludeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) != 0; // Если count == 0, snNumber уникален
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean updatePrinterOffice(int printerId, int newOfficeId) {
        String sql = "UPDATE printer SET office_id = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newOfficeId);
            pstmt.setInt(2, printerId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean movePrinter(int printerId, int newOfficeId, String note) {
        String sql = "UPDATE Printer SET Office_id = ?, note = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newOfficeId);
            pstmt.setString(2, note);
            pstmt.setInt(3, printerId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}