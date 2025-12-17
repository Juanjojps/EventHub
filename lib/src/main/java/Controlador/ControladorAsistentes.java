package Controlador;

import Dao.AsistenteDAO;
import Dao.EntradaDAO;
import Dao.EventoDAO;
import Modelo.Asistente;
import Modelo.Entrada;
import Modelo.Evento;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorAsistentes {

    // --- DAOs ---
    private AsistenteDAO asistenteDAO = new AsistenteDAO();
    private EventoDAO eventoDAO = new EventoDAO();
    private EntradaDAO entradaDAO = new EntradaDAO();

    // --- TAB Asistentes ---
    @FXML
    private TableView<Asistente> tablaAsistentes;
    @FXML
    private TableColumn<Asistente, String> colDniAsistente;
    @FXML
    private TableColumn<Asistente, String> colNombreAsistente;
    @FXML
    private TableColumn<Asistente, String> colEmailAsistente;

    @FXML
    private TextField txtDniAsistente;
    @FXML
    private TextField txtNombreAsistente;
    @FXML
    private TextField txtEmailAsistente;
    @FXML
    private TextField txtTelefonoAsistente;

    // --- Venta de Entradas ---
    @FXML
    private ComboBox<Evento> comboEventos;
    @FXML
    private TextField txtAsistenteSeleccionado;
    @FXML
    private TableView<Entrada> tablaEntradas;
    @FXML
    private TableColumn<Entrada, Long> colEntradaId;
    @FXML
    private TableColumn<Entrada, String> colEntradaAsistente;
    @FXML
    private TableColumn<Entrada, LocalDateTime> colEntradaFecha;
    @FXML
    private TableColumn<Entrada, Double> colEntradaPrecio;

    private ObservableList<Asistente> listaAsistentes;
    private Asistente asistenteSeleccionado;

    @FXML
    public void initialize() {
        configurarTablaAsistentes();
        configurarTablaEntradas();
        cargarDatosIniciales();

        // Listener selección Asistente
        tablaAsistentes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                asistenteSeleccionado = newSelection;
                rellenarFormularioAsistente(newSelection);
                txtAsistenteSeleccionado.setText(newSelection.toString());
            }
        });

        // Listener ComboBox
        comboEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarEntradasDeEvento(newVal);
            }
        });
    }

    // NAVEGACIÓN
    @FXML
    public void irAEventos(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Vista/EventosView.fxml"));
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
            mostrarAlerta("Error de Navegación",
                    "No se pudo cargar la vista de Eventos(" + e.getClass() + ") " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDatosIniciales() {
        listaAsistentes = FXCollections.observableArrayList(asistenteDAO.listarTodos());
        tablaAsistentes.setItems(listaAsistentes);

        // Cargar eventos en el combo
        List<Evento> eventos = eventoDAO.listarTodos();
        comboEventos.setItems(FXCollections.observableArrayList(eventos));
    }

    private void configurarTablaAsistentes() {
        colDniAsistente.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombreAsistente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmailAsistente.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void rellenarFormularioAsistente(Asistente a) {
        txtDniAsistente.setText(a.getDni());
        txtNombreAsistente.setText(a.getNombre());
        txtEmailAsistente.setText(a.getEmail());
        txtTelefonoAsistente.setText(a.getTelefono());
    }

    @FXML
    public void nuevoAsistente() {
        asistenteSeleccionado = null;
        tablaAsistentes.getSelectionModel().clearSelection();
        txtDniAsistente.clear();
        txtNombreAsistente.clear();
        txtEmailAsistente.clear();
        txtTelefonoAsistente.clear();
    }

    @FXML
    public void guardarAsistente() {
        try {
            if (txtDniAsistente.getText().isEmpty() || txtNombreAsistente.getText().isEmpty()) {
                mostrarAlerta("Error", "DNI y Nombre son obligatorios.");
                return;
            }
            if (!txtEmailAsistente.getText().contains("@")) {
                mostrarAlerta("Error", "Formato de email inválido.");
                return;
            }

            String dni = txtDniAsistente.getText();
            if (!esDniValido(dni)) {
                mostrarAlerta("Error", "DNI inválido.");
                return;
            }

            String nombre = txtNombreAsistente.getText();
            String email = txtEmailAsistente.getText();
            String telefono = txtTelefonoAsistente.getText();

            if (!esTelefonoValido(telefono)) {
                mostrarAlerta("Error", "Teléfono inválido. Debe tener 9 dígitos y empezar por 6, 7, 8 o 9.");
                return;
            }

            if (asistenteSeleccionado == null) {
                Asistente nuevo = new Asistente(dni, nombre, email, telefono);
                asistenteDAO.guardar(nuevo);
                listaAsistentes.add(nuevo);
                mostrarMensaje("Éxito", "Asistente registrado.");
            } else {
                asistenteSeleccionado.setDni(dni);
                asistenteSeleccionado.setNombre(nombre);
                asistenteSeleccionado.setEmail(email);
                asistenteSeleccionado.setTelefono(telefono);
                asistenteDAO.actualizar(asistenteSeleccionado);
                tablaAsistentes.refresh();
                mostrarMensaje("Éxito", "Asistente actualizado.");
            }
            nuevoAsistente();
        } catch (Exception e) {
            mostrarAlerta("Error al guardar", "Error (posible DNI duplicado): " + e.getMessage());
        }
    }

    @FXML
    public void eliminarAsistente() {
        if (asistenteSeleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un asistente.");
            return;
        }
        try {
            asistenteDAO.eliminar(asistenteSeleccionado.getId());
            listaAsistentes.remove(asistenteSeleccionado);
            nuevoAsistente();
            mostrarMensaje("Eliminado", "Asistente eliminado.");
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void buscarAsistente() {
        String termino = txtNombreAsistente.getText();
        if (termino.isEmpty())
            termino = txtDniAsistente.getText();

        if (termino.isEmpty()) {
            tablaAsistentes.setItems(listaAsistentes);
        } else {
            List<Asistente> res = asistenteDAO.buscar(termino);
            tablaAsistentes.setItems(FXCollections.observableArrayList(res));
        }
    }

    private void configurarTablaEntradas() {
        colEntradaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEntradaAsistente.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAsistente().getNombre()));
        colEntradaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));
        colEntradaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioFinal"));
    }

    private void cargarEntradasDeEvento(Evento e) {
        List<Entrada> entradas = entradaDAO.listarPorEvento(e.getId());
        tablaEntradas.setItems(FXCollections.observableArrayList(entradas));
    }

    @FXML
    public void venderEntrada() {
        Evento evento = comboEventos.getValue();
        Asistente asistente = tablaAsistentes.getSelectionModel().getSelectedItem();

        if (evento == null || asistente == null) {
            mostrarAlerta("Datos incompletos", "Debe seleccionar un Evento (ComboBox) y un Asistente (Tabla).");
            return;
        }

        try {
            entradaDAO.venderEntrada(evento, asistente);
            mostrarMensaje("Venta Exitosa",
                    "Entrada vendida a " + asistente.getNombre() + " para " + evento.getNombre());
            cargarEntradasDeEvento(evento);
        } catch (Exception e) {
            mostrarAlerta("Error en Venta", e.getMessage());
        }
    }

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

    private boolean esDniValido(String dni) {
        if (dni == null)
            return false;
        dni = dni.replaceAll("[^0-9A-Za-z]", "").toUpperCase();

        // 8 digitos + letra (DNI) o Letra + 7 digitos + letra (NIE)
        return dni.matches("[0-9]{8}[A-Z]") || dni.matches("[XYZ][0-9]{7}[A-Z]");
    }

    private boolean esTelefonoValido(String telefono) {
        if (telefono == null)
            return false;
        telefono = telefono.replaceAll("[^0-9]", "");
        return telefono.matches("[6789]\\d{8}");
    }
}
