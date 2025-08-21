package com.example.printapplication.dto;

public class Office {
    private int id;
    private String nameOffice;
    private String numberOffice;
    private int departmentId;

    public Office(int id, String numberOffice, String nameOffice, int departmentId) {
        this.id = id;
        this.numberOffice = numberOffice;
        this.nameOffice = nameOffice;
        this.departmentId = departmentId;
    }

    public int getId() {
        return id;
    }

    public String getNameOffice() {
        return nameOffice;
    }

    public String getNumberOffice() {
        return numberOffice;
    }

    public int getDepartmentId() {
        return departmentId;
    }
}