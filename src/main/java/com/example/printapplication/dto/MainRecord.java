package com.example.printapplication.dto;

public class MainRecord {
    private String nameOffice;
    private String numberOffice;
    private String nameDepartment;
    private String namePrinter;
    private String model;
    private String snNumber;
    private String note;
    private String fio;
    private int printerId;

    public MainRecord(String nameOffice, String numberOffice, String nameDepartment,
                      String namePrinter, String model, String snNumber,
                      String note, String fio) {
        this.nameOffice = nameOffice;
        this.numberOffice = numberOffice;
        this.nameDepartment = nameDepartment;
        this.namePrinter = namePrinter;
        this.model = model;
        this.snNumber = snNumber;
        this.note = note;
        this.fio = fio;
    }

    // Геттеры
    public String getNameOffice() {
        return nameOffice;
    }

    public String getNumberOffice() {
        return numberOffice;
    }

    public String getNameDepartment() {
        return nameDepartment;
    }

    public String getNamePrinter() {
        return namePrinter;
    }

    public String getModel() {
        return model;
    }

    public String getSnNumber() {
        return snNumber;
    }

    public String getNote() {
        return note;
    }

    public String getFio() {
        return fio;
    }
    public int getPrinterId() {
        return printerId;
    }

    public void setPrinterId(int printerId) {
        this.printerId = printerId;
    }
}