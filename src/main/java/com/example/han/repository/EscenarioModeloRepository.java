package com.example.han.repository;


import com.example.han.entity.EscenarioModelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EscenarioModeloRepository extends JpaRepository<EscenarioModelo, UUID> {
    List<EscenarioModelo> findByProyectoIdOrderByFechaGuardadoDesc(UUID proyectoId);
}