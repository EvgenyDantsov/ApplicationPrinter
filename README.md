# 🧰 EquipmentApplication

**[  Читать на русском](#-описание-проекта-на-русском)** | **[ Read in English](#-project-description-in-english)**

---

## 🇬🇧 Project Description (in English)

**EquipmentApplication** is a desktop program written in JavaFX for managing equipment in an organization.  
It helps to record where each item is installed, who is responsible, and what is its current status (installed, stored, or disposed).

---

### 🎯 Project Goal
The main goal is to make the process of equipment registration and tracking easier.  
You can:
- see all equipment and its location;
- assign responsible people;
- check statuses;
- and manage the full list in one place.

---

### ⚙️ Technologies
- ☕ **Java 17+**
- 🎨 **JavaFX** — user interface
- 🗄️ **MySQL** — database
- 🧩 **DAO / DTO pattern**
- ⚙️ **Maven / Build Artifacts**

---

### 📁 Project Structure

src/java/com/example/equipmentapplication/
├── config/
│ └── Config.java
│
├── dao/
│ ├── DepartmentDAO.java
│ ├── EquipmentDAO.java
│ ├── EquipmentDictionaryDAO.java
│ ├── EquipmentTypeDAO.java
│ ├── OfficeDAO.java
│ ├── SeniorDepartmentDAO.java
│ └── UltrasoundSensorDAO.java
│
├── dto/
│ ├── Department.java
│ ├── Equipment.java
│ ├── EquipmentDictionary.java
│ ├── EquipmentType.java
│ ├── Office.java
│ ├── SeniorDepartment.java
│ └── UltrasoundSensor.java
│
├── util/
│ ├── AlertUtils.java
│ └── WindowUtils.java
│
├── window/
│ ├── DepartmentWindow.java
│ ├── EquipmentDictionaryWindow.java
│ ├── EquipmentTypeWindow.java
│ ├── EquipmentWindow.java
│ ├── LoadingWindow.java
│ ├── MainWindow.java
│ ├── OfficeWindow.java
│ ├── SeniorDepartmentWindow.java
│ └── UltrasoundSensorWindow.java
│
├── DatabaseHelper.java
├── FieldValidator.java
└── HelloApplication.java

src/java/resources/

### 🧩 Main Features
✅ Add new equipment  
✅ Edit and delete items  
✅ Assign rooms and responsible persons  
✅ Change equipment status  
✅ View and search by list

### 🧠 Database Diagram
Simple ER text diagram:

department
├─ id (PK)
└─ department_name

seniordepartment
├─ id (PK)
├─ fio
└─ department_id (FK → department.id)

office
├─ id (PK)
├─ number_office
├─ name_office
└─ department_id (FK → department.id)

equipmenttype
├─ id (PK)
└─ name

equipment_dictionary
├─ id (PK)
├─ name
├─ model
└─ equipmenttype_id (FK → equipmenttype.id)

equipment
├─ id (PK)
├─ name
├─ model
├─ sn_number
├─ note
├─ office_id (FK → office.id)
├─ status (ENUM: installed | stored | disposed)
└─ equipmenttype_id (FK → equipmenttype.id)

ultrasoundsensors
├─ id (PK)
├─ sensor_name
├─ sensor_type
├─ sn_number
├─ note
└─ equipment_id (FK → equipment.id)

### 🚀 How to Run

#### 🔹 Prepare the database
1. Install **MySQL Server**.
2. Create database:
   ```sql
   CREATE DATABASE equipment_db;
Update application.properties:

db.url=jdbc:mysql://localhost:3306/equipment_db
db.user=root
db.password=your_password
🔹 Build and run
Open project in IntelliJ IDEA.

Build:
Build → Build Artifacts → EquipmentApplication.jar → Build

Run:
java -jar out/artifacts/EquipmentApplication/EquipmentApplication.jar
🖼️ Interface Example
![screenshot.png](src%2Fmain%2Fjava%2Fimages%2Fscreenshot.png)

👨‍💻 Author

EquipmentApplication — educational project for equipment management.
Author: EvgenyDantsov
License: MIT

🇷🇺 Описание проекта на русском

EquipmentApplication — настольное приложение на JavaFX для учёта оборудования в организации.
Оно помогает вести данные о том, где установлено оборудование, кто за него отвечает и в каком оно состоянии (установлено, на хранении, списано).

🎯 Цель проекта

Цель — облегчить процесс ввода и учёта оборудования.
Приложение позволяет:

видеть расположение оборудования;

назначать ответственных лиц;

отслеживать статусы;

управлять всей базой из одного окна.

⚙️ Технологии

☕ Java 17+

🎨 JavaFX — интерфейс пользователя

🗄️ MySQL — база данных

🧩 Паттерн DAO / DTO

⚙️ Maven / Build Artifacts

📁 Структура проекта

(та же, что и в английской версии, см. выше)

🧩 Основной функционал

✅ Добавление нового оборудования
✅ Редактирование и удаление записей
✅ Назначение кабинета и ответственного
✅ Управление статусами
✅ Просмотр и поиск по списку

🧠 Структура базы данных

(см. ER-диаграмму выше)

🚀 Запуск проекта

Установить MySQL Server и создать базу:

CREATE DATABASE equipment_db;


Настроить параметры подключения в application.properties.

Собрать артефакт в IntelliJ IDEA:
Build → Build Artifacts → EquipmentApplication.jar → Build

Запустить:

java -jar out/artifacts/EquipmentApplication/EquipmentApplication.jar

🧠 Автор

Автор: EvgenyDantsov
Лицензия: MIT