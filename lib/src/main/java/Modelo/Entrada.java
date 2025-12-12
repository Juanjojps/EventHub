package Modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Clase que representa una Entrada vendida.
 */
@Entity
@Table(name = "entrada")
public class Entrada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "fecha_compra")
	private LocalDateTime fechaCompra;

	@Column(name = "precio_final")
	private double precioFinal;

	// Relación Muchas Entradas pertenecen a Un Evento
	@ManyToOne
	@JoinColumn(name = "evento_id", nullable = false)
	private Evento evento;

	// Relación Muchas Entradas pertenecen a Un Asistente
	@ManyToOne
	@JoinColumn(name = "asistente_id", nullable = false)
	private Asistente asistente;

	public Entrada() {
	}

	public Entrada(Evento evento, Asistente asistente, LocalDateTime fechaCompra, double precioFinal) {
		this.evento = evento;
		this.asistente = asistente;
		this.fechaCompra = fechaCompra;
		this.precioFinal = precioFinal;
	}

	// Getters y Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(LocalDateTime fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	public double getPrecioFinal() {
		return precioFinal;
	}

	public void setPrecioFinal(double precioFinal) {
		this.precioFinal = precioFinal;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Asistente getAsistente() {
		return asistente;
	}

	public void setAsistente(Asistente asistente) {
		this.asistente = asistente;
	}
}
