package Controlador;

import Dao.AsistenteDAO;
import Dao.EntradaDAO;
import Dao.EventoDAO;
import Modelo.Asistente;
import Modelo.Entrada;
import Modelo.Evento;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorPrincipal {

    // --- DAOs ---
    private EventoDAO eventoDAO = new EventoDAO();
    private AsistenteDAO asistenteDAO = new AsistenteDAO();
    private EntradaDAO entradaDAO = new EntradaDAO();

    // --- TAB Eventos ---
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

    // --- Variables de estado ---
    private ObservableList<Evento> listaEventos;
    private ObservableList<Asistente> listaAsistentes;
    private ObservableList<Entrada> listaEntradas;

    private Evento eventoSeleccionado;
    private Asistente asistenteSeleccionado;

    @FXML
    public void initialize() {
        configurarTablaEventos();
        configurarTablaAsistentes();
        configurarTablaEntradas();

        cargarDatosIniciales();

        // Listeners para selección
        tablaEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                eventoSeleccionado = newSelection;
                rellenarFormularioEvento(newSelection);
                // Si estamos en la pestaña de ventas, podriamos actualizar el combo o la tabla
                // de entradas
            }
        });

        tablaAsistentes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                asistenteSeleccionado = newSelection;
                rellenarFormularioAsistente(newSelection);
                txtAsistenteSeleccionado.setText(newSelection.toString());
            }
        });

        // Listener para combobox de eventos en venta
        comboEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarEntradasDeEvento(newVal);
            }
        });
    }

    private void cargarDatosIniciales() {
        listaEventos = FXCollections.observableArrayList(eventoDAO.listarTodos());
        tablaEventos.setItems(listaEventos);
        comboEventos.setItems(listaEventos); // Vincular la misma lista al combo

        listaAsistentes = FXCollections.observableArrayList(asistenteDAO.listarTodos());
        tablaAsistentes.setItems(listaAsistentes);
    }

    // ================== GESTIÓN DE EVENTOS ==================

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
            // Validaciones básicas
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
                // Nuevo
                Evento nuevo = new Evento(nombre, tipo, fecha, aforo, precio);
                eventoDAO.guardar(nuevo);
                listaEventos.add(nuevo);
                mostrarMensaje("Éxito", "Evento creado correctamente.");
            } else {
                // Editar
                eventoSeleccionado.setNombre(nombre);
                eventoSeleccionado.setTipo(tipo);
                eventoSeleccionado.setFecha(fecha);
                eventoSeleccionado.setAforoMaximo(aforo);
                eventoSeleccionado.setPrecioEntrada(precio);
                eventoDAO.actualizar(eventoSeleccionado);
                tablaEventos.refresh(); // Refrescar vista
                comboEventos.setItems(null);
                comboEventos.setItems(listaEventos); // Refrescar combo
                mostrarMensaje("Éxito", "Evento actualizado.");
            }
            nuevoEvento(); // Limpiar
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
            String nombre = txtNombreAsistente.getText();
            String email = txtEmailAsistente.getText();
            String telefono = txtTelefonoAsistente.getText();

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
        // Se puede usar un campo de busqueda o el mismo campo de nombre
        String termino = txtNombreAsistente.getText(); // Reutilizo el campo nombre para simplicidad visual o se puede
                                                       // añadir otro
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
        // Para propiedades complejas (Asistente.nombre) necesitamos un ValueFactory
        // custom
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
        // Asistente seleccionado en la tabla de asistentes, o buscalo
        Asistente asistente = tablaAsistentes.getSelectionModel().getSelectedItem();

        if (evento == null || asistente == null) {
            mostrarAlerta("Datos incompletos", "Debe seleccionar un Evento (ComboBox) y un Asistente (Tabla).");
            return;
        }

        try {
            // Llamada transaccional
            entradaDAO.venderEntrada(evento, asistente);
            mostrarMensaje("Venta Exitosa",
                    "Entrada vendida a " + asistente.getNombre() + " para " + evento.getNombre());

            // Refrescar tabla de entradas
            cargarEntradasDeEvento(evento);
        } catch (Exception e) {
            mostrarAlerta("Error en Venta", e.getMessage());
        }
    }

    // ================== INFORMES (TXT) ==================

    @FXML
    public void exportarAsistentes() {
        Evento evento = tablaEventos.getSelectionModel().getSelectedItem();
        if (evento == null) {
            mostrarAlerta("Aviso", "Seleccione un evento de la tabla.");
            return;
        }

        guardarFichero("Listado_Asistentes_" + evento.getNombre() + ".txt", file -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("=== LISTADO DE ASISTENTES ===");
                writer.newLine();
                writer.write("Evento: " + evento.getNombre());
                writer.newLine();
                writer.write("Fecha: " + evento.getFecha());
                writer.newLine();
                writer.write("-----------------------------");
                writer.newLine();

                List<Entrada> entradas = entradaDAO.listarPorEvento(evento.getId());
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
        Evento evento = tablaEventos.getSelectionModel().getSelectedItem();
        if (evento == null) {
            mostrarAlerta("Aviso", "Seleccione un evento.");
            return;
        }

        guardarFichero("Resumen_Ventas_" + evento.getNombre() + ".txt", file -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                List<Entrada> entradas = entradaDAO.listarPorEvento(evento.getId());
                int vendidas = entradas.size();
                double recaudacion = entradaDAO.calcularRecaudacion(evento.getId());
                double ocupacion = (double) vendidas / evento.getAforoMaximo() * 100;

                writer.write("=== RESUMEN DE VENTAS ===");
                writer.newLine();
                writer.write("Evento: " + evento.getNombre());
                writer.newLine();
                writer.write("Aforo Máximo: " + evento.getAforoMaximo());
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

        // Obtener stage desde algun nodo
        Stage stage = (Stage) txtNombreEvento.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            action.accept(file);
        }
    }

    // ================== UTILIDADES ==================

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
