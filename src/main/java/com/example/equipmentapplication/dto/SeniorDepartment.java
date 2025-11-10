package com.example.equipmentapplication.dto;

public class SeniorDepartment {
    private int id;
    private String fio;
    private int departmentId;

    public SeniorDepartment(int id, String fio, int departmentId) {
        this.id = id;
        this.fio = fio;
        this.departmentId = departmentId;
    }

    public int getId() {
        return id;
    }

    public String getFio() {
        return fio;
    }

    public int getDepartmentId() {
        return departmentId;
    }
}