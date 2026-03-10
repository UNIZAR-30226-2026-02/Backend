package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.model.Personalizacion.TipoPersonalizacion;

@Repository
public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Integer> {
    
    // Obtener todas las personalizaciones disponibles en la tienda
    List<Personalizacion> findByActivoTrue();
    
    // Filtrar la tienda por tipo y que estén activos
    List<Personalizacion> findByTipoAndActivoTrue(TipoPersonalizacion tipo);
    
    // Validar que no haya cosméticos con el mismo nombre
    boolean existsByNombre(String nombre);

    // Catálogo ordenado por precio
    List<Personalizacion> findByTipoAndActivoTrueOrderByPrecioBalaAsc(TipoPersonalizacion tipo);

    // Subconsulta para excluir artículos ya comprados
    @Query(value = "SELECT p.* FROM personalizacion p WHERE p.activo = true AND p.tipo = :#{#tipo.name()} " +
                   "AND p.id_personalizacion NOT IN " +
                   "(SELECT ip.id_personalizacion FROM inventario_personalizacion ip WHERE ip.id_jugador = :idJugador)", 
           nativeQuery = true)
    List<Personalizacion> findArticulosNoCompradosPorTipo(@Param("idJugador") String idJugador, @Param("tipo") TipoPersonalizacion tipo);
}