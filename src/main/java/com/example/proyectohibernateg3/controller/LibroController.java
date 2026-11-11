package com.example.proyectohibernateg3.controller;

import com.example.proyectohibernateg3.dao.CategoriaDAO;
import com.example.proyectohibernateg3.dao.LibroDAO;
import com.example.proyectohibernateg3.database.JPAUtil;
import com.example.proyectohibernateg3.model.Categoria;
import com.example.proyectohibernateg3.model.Libro;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ObservableValue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class LibroController {

    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, Long> colId;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, BigDecimal> colPrecio;
    @FXML private TableColumn<Libro, String> colCategoria;

    @FXML private TextField txtTitulo, txtAutor, txtAnio, txtPrecio;
    @FXML private ComboBox<Categoria> cmbCategoria;

    @FXML private Button btnModificar, btnEliminar, obtenerLibro, btnNuevo;

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Long idSeleccionado = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colCategoria.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
        });

        tblLibros.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Libro> observable, Libro oldValue, Libro newValue) -> {
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
        cargarCategorias();
    }

    @FXML
    private void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.listar();
            cmbCategoria.setItems(FXCollections.observableArrayList(categorias));
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar categorías: " + e.getMessage());
        }
    }

    @FXML
    private void cargarLibros() {
        tblLibros.getItems().setAll(libroDAO.listar());
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtPrecio.setText(String.valueOf(libro.getPrecio()));

        // CORRECCIÓN: Asignar la categoría al ComboBox al seleccionar una fila de la tabla
        cmbCategoria.setValue(libro.getCategoria());

        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        if (btnModificar != null) btnModificar.setDisable(sinSeleccion);
        if (btnEliminar != null) btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
        String autor = txtAutor.getText() != null ? txtAutor.getText().trim() : "";
        Categoria categoria = cmbCategoria.getValue();

        if (titulo.isEmpty() || autor.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El título y el autor son obligatorios.");
            return null;
        }

        if (categoria == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Debe seleccionar una categoría.");
            return null;
        }

        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "El año debe ser un número entero válido.");
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "El precio debe ser un número válido.");
            return null;
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setAnioPublicacion(anio);
        libro.setPrecio(precio);

        // CORRECCIÓN: Asignar la categoría seleccionada al objeto Libro
        libro.setCategoria(categoria);

        return libro;
    }

    @FXML
    private void guardarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            if (libro == null) return;

            libroDAO.guardar(libro);
            limpiarFormulario();
            cargarLibros();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Libro guardado exitosamente.");
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar el libro: " + ex.getMessage());
        }
    }

    @FXML
    private void modificarLibro() {
        if (idSeleccionado == null) return;
        try {
            Libro libro = obtenerLibroFormulario();
            if (libro == null) return;

            libro.setId(idSeleccionado);

            boolean modificado = modificar(libro);

            if (!modificado) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se encontró el libro a modificar en la base de datos.");
                return;
            }

            limpiarFormulario();
            cargarLibros();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Libro modificado exitosamente.");

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al modificar el libro: " + ex.getMessage());
        }
    }

    @FXML
    private void eliminarLibro() {
        if (idSeleccionado == null) return;
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmación de eliminación");
            confirmacion.setHeaderText("Eliminar libro");
            confirmacion.setContentText("¿Estás seguro que deseas eliminar el registro?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            boolean eliminado = eliminar(idSeleccionado);
            if (eliminado) {
                limpiarFormulario();
                cargarLibros();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Libro eliminado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "El libro ya no existe en la base de datos.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar el libro: " + ex.getMessage());
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
    private void limpiarFormulario() {
        tblLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();

        // CORRECCIÓN: Limpiar la selección del ComboBox
        cmbCategoria.getSelectionModel().clearSelection();

        actualizarBotones();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}