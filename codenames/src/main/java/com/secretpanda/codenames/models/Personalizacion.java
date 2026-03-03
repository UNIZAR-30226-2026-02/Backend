package com.secretpanda.codenames.models;

import jakarta.persistence.*;

@Entity
@Table(name = "personalizacion")
public class Personalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Se autogenera por ser SERIAL
    @Column(name = "id_personalizacion")
    private Integer idPersonalizacion;

    @Column(nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_bala", nullable = false)
    private Integer precioBala;

    @Column(nullable = false, length = 64)
    private String tipo; // 'carta' o 'tablero'

    @Column(name = "valor_visual", columnDefinition = "TEXT")
    private String valorVisual; // Guardará la URL de la imagen

    @Column(nullable = false)
    private Boolean activo = true;

    public Personalizacion() {}

    public Integer getIdPersonalizacion() {
        return idPersonalizacion;
    }

    public void setIdPersonalizacion(Integer idPersonalizacion) {
        this.idPersonalizacion = idPersonalizacion;
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

    public Integer getPrecioBala() {
        return precioBala;
    }

    public void setPrecioBala(Integer precioBala) {
        this.precioBala = precioBala;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValorVisual() {
        return valorVisual;
    }

    public void setValorVisual(String valorVisual) {
        this.valorVisual = valorVisual;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}