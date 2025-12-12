package Modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un Asistente.
 */
@Entity
@Table(name = "asistente")
public class Asistente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "dni", nullable = false, unique = true)
	private String dni;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "email")
	private String email;

	@Column(name = "telefono")
	private String telefono;

	@OneToMany(mappedBy = "asistente", fetch = FetchType.LAZY)
	private List<Entrada> entradas = new ArrayList<>();

	public Asistente() {
	}

	public Asistente(String dni, String nombre, String email, String telefono) {
		this.dni = dni;
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
	}

	// Getters y Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public List<Entrada> getEntradas() {
		return entradas;
	}

	public void setEntradas(List<Entrada> entradas) {
		this.entradas = entradas;
	}

	@Override
	public String toString() {
		return nombre + " (" + dni + ")";
	}
}
