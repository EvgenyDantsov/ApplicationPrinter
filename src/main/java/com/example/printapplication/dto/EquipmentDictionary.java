package com.example.printapplication.dto;

public class EquipmentDictionary {
    private int id;
    private String name;
    private String model;
    private int equipmentTypeId;

    public EquipmentDictionary(int id, String name, String model, int equipmentTypeId) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.equipmentTypeId = equipmentTypeId;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public int getEquipmentTypeId() {
        return equipmentTypeId;
    }
}
