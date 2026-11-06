package com.example.proyectohibernateg3.controller;

import com.example.proyectohibernateg3.model.Libro;
import com.example.proyectohibernateg3.dao.LibroDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ObservableValue;

import java.math.BigDecimal;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import com.example.proyectohibernateg3.database.JPAUtil;

public class LibroController {

    @FXML
    private TableView<Libro> tblLibros;

    @FXML
    private TableColumn<Libro, Long> colId;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, String> colAutor;

    @FXML
    private TableColumn<Libro, Integer> colAnio;

    @FXML
    private TableColumn<Libro, BigDecimal> colPrecio;

    @FXML
    private TextField txtTitulo, txtAutor, txtAnio, txtPrecio;

    @FXML
    private Button btnModificar, btnEliminar;

    @FXML
    private Button obtenerLibro;

    @FXML
    private Button btnNuevo;

    private final LibroDAO libroDAO = new LibroDAO();

    // ID del registro seleccionado
    private Long idSeleccionado = null;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colAutor.setCellValueFactory(
                new PropertyValueFactory<>("autor")
        );

        colAnio.setCellValueFactory(
                new PropertyValueFactory<>("anioPublicacion")
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        tblLibros.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (ObservableValue<? extends Libro> observable,
                         Libro oldValue, Libro newValue) -> {

                            if (newValue != null) {
                                seleccionarLibro(newValue);
                            } else {
                                idSeleccionado = null;
                                actualizarBotones();
                            }
                        }
                );

        actualizarBotones();
        cargarLibros();
    }

    @FXML
    private void cargarLibros() {
        tblLibros.getItems().setAll(libroDAO.listar());
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(
                String.valueOf(libro.getAnioPublicacion())
        );
        txtPrecio.setText(
                String.valueOf(libro.getPrecio())
        );
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        if (btnModificar != null) btnModificar.setDisable(sinSeleccion);
        if (btnEliminar != null) btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText();
        String autor = txtAutor.getText();
        int anio = Integer.valueOf(txtAnio.getText());
        BigDecimal precio = new BigDecimal(txtPrecio.getText());
        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setAnioPublicacion(anio);
        libro.setPrecio(precio);
        return libro;
    }

    @FXML
    private void guardarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            libroDAO.guardar(libro);
            cargarLibros();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void modificarLibro() {
        if (idSeleccionado == null) return;
        try {
            Libro libro = obtenerLibroFormulario();
            libro.setId(idSeleccionado);

            boolean modificado = modificar(libro);

            if (!modificado) {
                // Alert para indicar que hubo un error a nivel de transacción
                return;
            }

            cargarLibros();

            // Alert para indicar que la modificación fue exitosa

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void eliminarLibro() {
        if (idSeleccionado == null) return;
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmacion de eliminacion de datos");
            confirmacion.setHeaderText("Eliminar libro");
            confirmacion.setContentText(
                    "Estas seguro que desea eliminar el registro?"
            );

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            boolean elimiando = eliminar(idSeleccionado);
            if (elimiando) {
                // Alert
                limpiarFormulario();
                cargarLibros();
            }

            // Alert para indicar que la operacion fue realizada

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean modificar(Libro libro) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.find(Libro.class, libro.getId()) == null) {
                tx.rollback();
                return false;
            }
            em.merge(libro);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private boolean eliminar(Long id) {
        if (id == null) return false;
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Libro libro = em.find(Libro.class, id);
            if (libro == null) {
                tx.rollback();
                return false;
            }
            em.remove(libro);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
    @FXML
    private void limpiarFormulario(){
        tblLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();
        actualizarBotones();
    }

}