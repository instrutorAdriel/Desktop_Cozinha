module com.example.desktop_cozinha {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.example.desktop_cozinha.controller to javafx.fxml;
    exports com.example.desktop_cozinha;
}