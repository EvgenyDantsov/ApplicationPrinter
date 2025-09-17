package com.example.printapplication.dao;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dto.EquipmentDictionary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDictionaryDAO {
    public static List<EquipmentDictionary> getAllEntries() {
        List<EquipmentDictionary> entries = new ArrayList<>();
        String sql = "SELECT id, equipmenttype_id, name, model FROM equipment_dictionary ORDER BY equipmenttype_id, name, model";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int equipmentTypeId = rs.getInt("equipmenttype_id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                entries.add(new EquipmentDictionary(id, name, model, equipmentTypeId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }
    // Получить список производителей по типу оборудования
    public static List<String> getManufacturersByType(int typeId) {
        List<String> manufacturers = new ArrayList<>();
        String sql = "SELECT DISTINCT name FROM equipment_dictionary WHERE equipmenttype_id = ? ORDER BY name";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, typeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                manufacturers.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return manufacturers;
    }

    // Получить список моделей по типу и производителю
    public static List<String> getModelsByTypeAndManufacturer(int typeId, String manufacturer) {
        List<String> models = new ArrayList<>();
        String sql = "SELECT model FROM equipment_dictionary WHERE equipmenttype_id = ? AND name = ? ORDER BY model";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, typeId);
            stmt.setString(2, manufacturer);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                models.add(rs.getString("model"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return models;
    }

    // Добавить новую запись (производитель + модель)
    public static boolean addDictionaryEntry(int equipmentTypeId, String manufacturer, String model) {
        String sql = "INSERT INTO equipment_dictionary (equipmenttype_id, name, model) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentTypeId);
            stmt.setString(2, manufacturer);
            stmt.setString(3, model);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Обновить запись
    public static boolean updateDictionaryEntry(int id, String manufacturer, String model, int equipmentTypeId) {
        String sql = "UPDATE equipment_dictionary SET name = ?, model = ?, equipmenttype_id=? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, manufacturer);
            stmt.setString(2, model);
            stmt.setInt(3, equipmentTypeId);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить запись
    public static boolean deleteDictionaryEntry(int id) {
        String sql = "DELETE FROM equipment_dictionary WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isModelUnique(String model, int excludeId) {
        String sql = "SELECT COUNT(*) FROM equipment_dictionary WHERE model = ? AND id != ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, model);
            statement.setInt(2, excludeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 0; // true, если модель уникальна
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
