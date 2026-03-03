package com.secretpanda.codenames.models;

import jakarta.persistence.*;

@Entity
@Table(name = "logro")
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Al ser SERIAL en PostgreSQL, se autogenera
    @Column(name = "id_logro")
    private Integer idLogro;

    @Column(nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 64)
    private String tipo; // 'medalla' o 'logro'

    @Column(name = "estadistica_clave", nullable = false, length = 64)
    private String estadisticaClave; 

    @Column(name = "valor_objetivo", nullable = false)
    private Integer valorObjetivo;

    @Column(name = "balas_recompensa", nullable = false)
    private Integer balasRecompensa;

    @Column(nullable = false)
    private Boolean activo = true;

    // Constructor vacío obligatorio para JPA
    public Logro() {
    }

    public Integer getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(Integer idLogro) {
        this.idLogro = idLogro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstadisticaClave() {
        return estadisticaClave;
    }

    public void setEstadisticaClave(String estadisticaClave) {
        this.estadisticaClave = estadisticaClave;
    }

    public Integer getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Integer valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public Integer getBalasRecompensa() {
        return balasRecompensa;
    }

    public void setBalasRecompensa(Integer balasRecompensa) {
        this.balasRecompensa = balasRecompensa;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}