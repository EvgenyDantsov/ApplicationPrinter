package com.example.printapplication.dto;

public class Printer {
    private int id;
    private String printerName;
    private String model;
    private String snNumber;
    private String note;
    private int officeId;
    private String status; // Добавляем это поле

    public Printer(int id, String printerName, String model, String snNumber, String note, String status, int officeId) {
        this.id = id;
        this.printerName = printerName;
        this.model = model;
        this.snNumber = snNumber;
        this.note = note;
        this.officeId = officeId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getPrinterName() {
        return printerName;
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

    public int getOfficeId() {
        return officeId;
    }

    public String getStatus() {
        return status;
    }
}