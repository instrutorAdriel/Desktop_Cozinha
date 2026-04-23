module com.example.desktop_cozinha {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.desktop_cozinha to javafx.fxml;
    exports com.example.desktop_cozinha;
}