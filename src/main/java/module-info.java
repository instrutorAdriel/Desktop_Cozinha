module com.example.desktop_cozinha {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires jakarta.mail;
    requires jdk.compiler;
    requires java.desktop;



    opens com.example.desktop_cozinha to javafx.fxml;
    exports com.example.desktop_cozinha;
    exports com.example.desktop_cozinha.config;
    opens com.example.desktop_cozinha.config to javafx.fxml;
    exports com.example.desktop_cozinha.controller;
    opens com.example.desktop_cozinha.controller to javafx.fxml;
    exports com.example.desktop_cozinha.model;
    opens com.example.desktop_cozinha.model to javafx.fxml;
}