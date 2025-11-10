package com.example.equipmentapplication.dao;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.dto.Department;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DepartmentDAO {
    public static ObservableList<Department> getAllDepartments() {
        ObservableList<Department> departments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM department ORDER BY department_name";
        try (Connection connection = DatabaseHelper.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("department_name");
                departments.add(new Department(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public static boolean addDepartment(String name) {
        String sql = "INSERT INTO department (department_name) VALUES (?)";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateDepartment(int id, String newName) {
        String sql = "UPDATE department SET department_name = ? WHERE id = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newName);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDepartment(int id) {
        String sql = "DELETE FROM department WHERE id = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean departmentExists(String name) {
        String sql = "SELECT COUNT(*) FROM department WHERE department_name = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Если count > 0, отделение существует
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}