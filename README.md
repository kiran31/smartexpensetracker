# 📱 Smart Daily Expense Tracker  

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android&logoColor=white)  
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blueviolet?logo=kotlin&logoColor=white)  
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)  
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)  
![License](https://img.shields.io/badge/License-MIT-yellow)  

A modern, offline-first Android application designed to help small business owners efficiently track their daily expenses. Built with the latest Jetpack libraries, this app provides a clean, intuitive interface to capture, view, and analyze spending habits, ensuring no expense goes unrecorded.  

---

## ✨ Features  

### Full CRUD Operations  
- **Create:** Easily add new expenses with details like title, amount, category, and notes.  
- **Read:** View a clear, filterable list of all expenses.  
- **Update:** Click any expense to open an edit screen and correct details.  
- **Delete:** Securely delete incorrect or unwanted entries.  

### Expense List & Filtering  
- View expenses for any selected day using a native calendar picker.  
- **Live Search** functionality to filter expenses by title or notes in real-time.  
- Group expenses either by **Category** or **Time** for better organization.  

### Reporting & Analysis  
- A dedicated report screen summarizing spending over the last 7 days.  
- Visual **bar chart** showing daily totals.  
- Breakdown of spending by category with progress indicators.  

### Offline First  
- All data is stored locally in a **Room database**, making the app fully functional without an internet connection.  

### Smart Features  
- **Input Validation:** Ensures that the title is not empty and the amount is a positive number.  
- **Duplicate Detection:** Prevents accidental double-entry of the same expense within a short time frame.  
- **Data Export:** A share button on the report screen to export a plain-text summary.  

---

## 📸 Screenshots  

- ([Main Screen](https://github.com/user-attachments/assets/aa9512fe-b141-4b70-9c61-c03d658528a1))  
- (https://github.com/user-attachments/assets/135eaea4-8655-465c-8533-bf15ed85d9a7)
- (https://github.com/user-attachments/assets/846c2026-c781-4c69-aac4-4925e335f0fc)
 

---

## 🛠️ Tech Stack & Architecture  

This project is built following **modern Android development best practices** and an **MVVM (Model-View-ViewModel)** architecture.  

- **UI:** Jetpack Compose for a declarative and modern UI.  
- **State Management:** StateFlow & ViewModels for lifecycle-aware, reactive state handling.  
- **Database:** Room for robust, offline-first data persistence.  
- **Dependency Injection:** Dagger Hilt for managing dependencies and decoupling components.  
- **Navigation:** Jetpack Compose Navigation for screen transitions.  
- **Language:** 100% Kotlin.  

---

## 🤖 AI Usage Summary  

As part of the development process, I leveraged an AI assistant for a few specific tasks to improve efficiency, while the **core logic and architecture were developed by me**.  

- Used AI for generating initial boilerplate code.  
- Provided schema for the **Expense table** and generated corresponding Room Entity & DAO functions.  
- Added descriptive **KDoc comments** for complex ViewModel functions to improve readability.  

---

## 🚀 How to Build  

1. Clone this repository to your local machine.  
   ```bash
   git clone https://github.com/<your-username>/<your-repo>.git
