module com.example.proyectohibernateg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;


    opens com.example.proyectohibernateg3 to javafx.fxml;
    exports com.example.proyectohibernateg3;
}