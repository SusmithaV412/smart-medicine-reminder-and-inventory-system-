# Smart Medicine Reminder and Inventory System

A comprehensive Java Swing desktop application designed to help users manage their medication schedules and inventory effectively. This system provides personalized medicine reminders with timely pop-up notifications to support better health management.

## 🚀 Features

### Core Functionality
- **Medicine Reminder System**: Set personalized reminders for different medications
- **Pop-up Notifications**: Timely alerts to ensure you never miss a dose
- **Inventory Management**: Track medicine stock levels and expiry dates
- **User-Friendly Interface**: Intuitive Java Swing GUI for easy navigation
- **Database Integration**: Secure data storage using MySQL database

### Key Capabilities
- Add, edit, and delete medicine entries
- Set multiple reminders per medicine
- View upcoming medication schedules
- Monitor medicine inventory levels
- Track expiry dates and receive alerts
- Generate medication reports

## 🛠️ Technologies Used

- **Frontend**: Java Swing (GUI)
- **Backend**: Java
- **Database**: MySQL
- **Database Connectivity**: JDBC
- **IDE**: NetBeans/Eclipse/IntelliJ IDEA

## 📋 Prerequisites

Before running this application, ensure you have:

- Java Development Kit (JDK) 8 or higher
- MySQL Server installed and running
- MySQL Connector/J (JDBC driver)
- IDE (NetBeans, Eclipse, or IntelliJ IDEA)

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/SusmithaV412/smart-medicine-reminder-and-inventory-system-.git
cd smart-medicine-reminder-and-inventory-system-
```

### 2. Database Setup
1. Start your MySQL server
2. Create a new database:
```sql
CREATE DATABASE medicine_reminder_db;
```
3. Import the database schema (if provided) or run the SQL scripts
4. Update database connection details in the configuration file

### 3. Configure Database Connection
Update the database connection parameters in your Java code:
```java
String url = "jdbc:mysql://localhost:3306/medicine_reminder_db";
String username = "your_username";
String password = "your_password";
```

### 4. Add MySQL Connector
- Download MySQL Connector/J from the official MySQL website
- Add the JAR file to your project's classpath

### 5. Run the Application
- Open the project in your preferred IDE
- Compile and run the main class
- The application window should appear

## 📱 How to Use

### Adding a Medicine
1. Click on "Add Medicine" button
2. Enter medicine details (name, dosage, frequency)
3. Set reminder times
4. Save the entry

### Setting Reminders
1. Select a medicine from the list
2. Click "Set Reminder"
3. Choose reminder frequency and times
4. Enable notifications

### Managing Inventory
1. Navigate to "Inventory" section
2. Add stock quantities and expiry dates
3. Monitor low stock alerts
4. Update inventory as needed

## 🗂️ Project Structure

```
smart-medicine-reminder-and-inventory-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── gui/          # GUI components
│   │   │   ├── database/     # Database connection classes
│   │   │   ├── models/       # Data models
│   │   │   └── utils/        # Utility classes
│   │   └── resources/        # Configuration files
├── lib/                      # External libraries
├── database/                 # SQL scripts
└── README.md
```

## 🔮 Future Enhancements

- [ ] Mobile app integration
- [ ] Cloud synchronization
- [ ] Doctor prescription integration
- [ ] Advanced reporting features
- [ ] Multi-user support
- [ ] SMS/Email notifications
- [ ] Medicine interaction warnings

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

⭐ **If you found this project helpful, please give it a star!** ⭐
