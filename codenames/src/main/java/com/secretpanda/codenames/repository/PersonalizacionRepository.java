package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

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

    // Buscar una personalización concreta por su nombre
    Optional<Personalizacion> findByNombre(String nombre);

    // Catálogo ordenado por precio
    List<Personalizacion> findByTipoAndActivoTrueOrderByPrecioBalaAsc(TipoPersonalizacion tipo);

    // Subconsulta JPQL pura y fuertemente tipada para excluir artículos ya comprados
    @Query("SELECT p FROM Personalizacion p WHERE p.activo = true AND p.tipo = :tipo " +
           "AND p.idPersonalizacion NOT IN " +
           "(SELECT ip.personalizacion.idPersonalizacion FROM InventarioPersonalizacion ip WHERE ip.jugador.idGoogle = :idJugador)")
    List<Personalizacion> findArticulosNoCompradosPorTipo(@Param("idJugador") String idJugador, @Param("tipo") TipoPersonalizacion tipo);
}