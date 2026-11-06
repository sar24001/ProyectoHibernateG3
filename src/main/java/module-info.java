module com.example.proyectohibernateg3 {
    // Módulos necesarios
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    opens com.example.proyectohibernateg3.controller to javafx.fxml;
    opens com.example.proyectohibernateg3 to javafx.fxml;

    //Permite a Hibernate y JavaFX acceder a los campos privados de tus entidades
    opens com.example.proyectohibernateg3.model to org.hibernate.orm.core, javafx.base;

    exports com.example.proyectohibernateg3;
    exports com.example.proyectohibernateg3.controller;
    exports com.example.proyectohibernateg3.model;
}