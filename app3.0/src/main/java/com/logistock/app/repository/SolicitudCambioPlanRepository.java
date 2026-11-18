package com.logistock.app.repository;

import com.logistock.app.model.EstadoSolicitud;
import com.logistock.app.model.SolicitudCambioPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudCambioPlanRepository extends JpaRepository<SolicitudCambioPlan, Long> {

    List<SolicitudCambioPlan> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);

    List<SolicitudCambioPlan> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);

    Optional<SolicitudCambioPlan> findFirstByUsuarioIdAndEstadoOrderByFechaSolicitudDesc(Long usuarioId, EstadoSolicitud estado);

    boolean existsByUsuarioIdAndEstado(Long usuarioId, EstadoSolicitud estado);
}
