package Controlador;

import Dao.EventoDAO;
import Dao.EntradaDAO; // For reports
import Modelo.Evento;
import Modelo.Entrada; // For reports
import Modelo.Asistente; // For reports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ControladorEventos {

    // --- DAOs ---
    private EventoDAO eventoDAO = new EventoDAO();
    private EntradaDAO entradaDAO = new EntradaDAO(); // Needed for reports

    // --- Tabla Eventos ---
    @FXML
    private TableView<Evento> tablaEventos;
    @FXML
    private TableColumn<Evento, String> colNombreEvento;
    @FXML
    private TableColumn<Evento, String> colTipoEvento;
    @FXML
    private TableColumn<Evento, LocalDate> colFechaEvento;
    @FXML
    private TableColumn<Evento, Integer> colAforoEvento;
    @FXML
    private TableColumn<Evento, Double> colPrecioEvento;

    @FXML
    private TextField txtNombreEvento;
    @FXML
    private TextField txtTipoEvento;
    @FXML
    private DatePicker dpFechaEvento;
    @FXML
    private TextField txtAforoEvento;
    @FXML
    private TextField txtPrecioEvento;
    @FXML
    private TextField txtBusquedaEvento;

    // --- Estado ---
    private ObservableList<Evento> listaEventos;
    private Evento eventoSeleccionado;

    @FXML
    public void initialize() {
        configurarTablaEventos();
        cargarDatosIniciales();

        // Listener selección
        tablaEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                eventoSeleccionado = newSelection;
                rellenarFormularioEvento(newSelection);
            }
        });
    }

    private void cargarDatosIniciales() {
        listaEventos = FXCollections.observableArrayList(eventoDAO.listarTodos());
        tablaEventos.setItems(listaEventos);
    }

    private void configurarTablaEventos() {
        colNombreEvento.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoEvento.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFechaEvento.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colAforoEvento.setCellValueFactory(new PropertyValueFactory<>("aforoMaximo"));
        colPrecioEvento.setCellValueFactory(new PropertyValueFactory<>("precioEntrada"));
    }

    private void rellenarFormularioEvento(Evento e) {
        txtNombreEvento.setText(e.getNombre());
        txtTipoEvento.setText(e.getTipo());
        dpFechaEvento.setValue(e.getFecha());
        txtAforoEvento.setText(String.valueOf(e.getAforoMaximo()));
        txtPrecioEvento.setText(String.valueOf(e.getPrecioEntrada()));
    }

    @FXML
    public void nuevoEvento() {
        eventoSeleccionado = null;
        tablaEventos.getSelectionModel().clearSelection();
        txtNombreEvento.clear();
        txtTipoEvento.clear();
        dpFechaEvento.setValue(null);
        txtAforoEvento.clear();
        txtPrecioEvento.clear();
    }

    @FXML
    public void guardarEvento() {
        try {
            if (txtNombreEvento.getText().isEmpty() || txtAforoEvento.getText().isEmpty()
                    || txtPrecioEvento.getText().isEmpty()) {
                mostrarAlerta("Error", "Campos obligatorios vacíos.");
                return;
            }

            String nombre = txtNombreEvento.getText();
            String tipo = txtTipoEvento.getText();
            LocalDate fecha = dpFechaEvento.getValue();
            int aforo = Integer.parseInt(txtAforoEvento.getText());
            double precio = Double.parseDouble(txtPrecioEvento.getText());

            if (aforo <= 0 || precio < 0) {
                mostrarAlerta("Error", "Aforo debe ser > 0 y precio >= 0");
                return;
            }

            if (eventoSeleccionado == null) {
                Evento nuevo = new Evento(nombre, tipo, fecha, aforo, precio);
                eventoDAO.guardar(nuevo);
                listaEventos.add(nuevo);
                mostrarMensaje("Éxito", "Evento creado correctamente.");
            } else {
                eventoSeleccionado.setNombre(nombre);
                eventoSeleccionado.setTipo(tipo);
                eventoSeleccionado.setFecha(fecha);
                eventoSeleccionado.setAforoMaximo(aforo);
                eventoSeleccionado.setPrecioEntrada(precio);
                eventoDAO.actualizar(eventoSeleccionado);
                tablaEventos.refresh();
                mostrarMensaje("Éxito", "Evento actualizado.");
            }
            nuevoEvento();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Aforo y precio deben ser números válidos.");
        } catch (Exception e) {
            mostrarAlerta("Error al guardar", e.getMessage());
        }
    }

    @FXML
    public void eliminarEvento() {
        if (eventoSeleccionado == null) {
            mostrarAlerta("Selección", "Debe seleccionar un evento para eliminar.");
            return;
        }
        try {
            eventoDAO.eliminar(eventoSeleccionado.getId());
            listaEventos.remove(eventoSeleccionado);
            nuevoEvento();
            mostrarMensaje("Eliminado", "Evento eliminado correctamente.");
        } catch (RuntimeException e) {
            mostrarAlerta("Error al eliminar", "No se puede eliminar: " + e.getMessage());
        }
    }

    @FXML
    public void buscarEvento() {
        String termino = txtBusquedaEvento.getText();
        if (termino == null || termino.isEmpty()) {
            tablaEventos.setItems(listaEventos);
        } else {
            List<Evento> resultados = eventoDAO.buscarPorNombre(termino);
            tablaEventos.setItems(FXCollections.observableArrayList(resultados));
        }
    }

    // NAVEGACIÓN
    @FXML
    public void irAAsistentes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/AsistentesView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);

            // Cargar CSS si existe
            java.net.URL cssUrl = getClass().getResource("/Modelo/application.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista de Asistentes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // INFORMES
    @FXML
    public void exportarAsistentes() {
        if (eventoSeleccionado == null) {
            mostrarAlerta("Aviso", "Seleccione un evento de la tabla.");
            return;
        }

        guardarFichero("Listado_Asistentes_" + eventoSeleccionado.getNombre() + ".txt", file -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("=== LISTADO DE ASISTENTES ===");
                writer.newLine();
                writer.write("Evento: " + eventoSeleccionado.getNombre());
                writer.newLine();
                writer.write("Fecha: " + eventoSeleccionado.getFecha());
                writer.newLine();
                writer.write("-----------------------------");
                writer.newLine();

                List<Entrada> entradas = entradaDAO.listarPorEvento(eventoSeleccionado.getId());
                for (Entrada ent : entradas) {
                    Asistente a = ent.getAsistente();
                    writer.write("DNI: " + a.getDni() + " | Nombre: " + a.getNombre() + " | Email: " + a.getEmail());
                    writer.newLine();
                }
                mostrarMensaje("Informe Generado", "Archivo guardado correctamente.");
            } catch (IOException e) {
                mostrarAlerta("Error E/S", "No se pudo escribir el archivo: " + e.getMessage());
            }
        });
    }

    @FXML
    public void exportarResumen() {
        if (eventoSeleccionado == null) {
            mostrarAlerta("Aviso", "Seleccione un evento.");
            return;
        }

        guardarFichero("Resumen_Ventas_" + eventoSeleccionado.getNombre() + ".txt", file -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                List<Entrada> entradas = entradaDAO.listarPorEvento(eventoSeleccionado.getId());
                int vendidas = entradas.size();
                double recaudacion = entradaDAO.calcularRecaudacion(eventoSeleccionado.getId());
                double ocupacion = (double) vendidas / eventoSeleccionado.getAforoMaximo() * 100;

                writer.write("=== RESUMEN DE VENTAS ===");
                writer.newLine();
                writer.write("Evento: " + eventoSeleccionado.getNombre());
                writer.newLine();
                writer.write("Aforo Máximo: " + eventoSeleccionado.getAforoMaximo());
                writer.newLine();
                writer.write("Entradas Vendidas: " + vendidas);
                writer.newLine();
                writer.write("Ocupación: " + String.format("%.2f", ocupacion) + "%");
                writer.newLine();
                writer.write("Recaudación Total: " + recaudacion + " €");
                writer.newLine();

                mostrarMensaje("Informe Generado", "Resumen guardado correctamente.");
            } catch (IOException e) {
                mostrarAlerta("Error E/S", e.getMessage());
            }
        });
    }

    private void guardarFichero(String nombreDefecto, java.util.function.Consumer<File> action) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Informe TXT");
        fileChooser.setInitialFileName(nombreDefecto);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));

        Stage stage = (Stage) txtNombreEvento.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            action.accept(file);
        }
    }

    // --- UTILIDADES ---
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarMensaje(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.show();
    }
}
