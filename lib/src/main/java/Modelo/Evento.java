package Modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un Evento.
 * Mapeada a la tabla 'evento' con Hibernate.
 */
@Entity
@Table(name = "evento")
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "tipo")
	private String tipo;

	@Column(name = "fecha")
	private LocalDate fecha;

	@Column(name = "aforo_maximo", nullable = false)
	private int aforoMaximo;

	@Column(name = "precio_entrada", nullable = false)
	private double precioEntrada;

	
	@OneToMany(mappedBy = "evento", fetch = FetchType.LAZY)
	private List<Entrada> entradas = new ArrayList<>();

	public Evento() {
	}

	public Evento(String nombre, String tipo, LocalDate fecha, int aforoMaximo, double precioEntrada) {
		this.nombre = nombre;
		this.tipo = tipo;
		this.fecha = fecha;
		this.aforoMaximo = aforoMaximo;
		this.precioEntrada = precioEntrada;
	}

	// Getters y Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public int getAforoMaximo() {
		return aforoMaximo;
	}

	public void setAforoMaximo(int aforoMaximo) {
		this.aforoMaximo = aforoMaximo;
	}

	public double getPrecioEntrada() {
		return precioEntrada;
	}

	public void setPrecioEntrada(double precioEntrada) {
		this.precioEntrada = precioEntrada;
	}

	public List<Entrada> getEntradas() {
		return entradas;
	}

	public void setEntradas(List<Entrada> entradas) {
		this.entradas = entradas;
	}

	@Override
	public String toString() {
		return nombre; // Para que se vea bonito en el ComboBox
	}
}
