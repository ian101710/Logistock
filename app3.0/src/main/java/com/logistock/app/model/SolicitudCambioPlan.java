package com.logistock.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Solicitud de cambio de plan (BASICO <-> PREMIUM) creada por un usuario
 * desde "planes.html". El usuario NUNCA cambia su propio plan directamente:
 * solo crea esta solicitud, que queda PENDIENTE hasta que un ROLE_ADMIN la
 * aprueba o la rechaza desde el panel de administración.
 *
 * Es, a la vez, el mecanismo de "notificación al administrador": mientras
 * exista una fila con estado PENDIENTE, aparece listada en
 * /admin/solicitudes.
 */
@Entity
@Table(name = "solicitudes_cambio_plan")
public class SolicitudCambioPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_actual", nullable = false, length = 20)
    private Plan planActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_solicitado", nullable = false, length = 20)
    private Plan planSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Plan getPlanActual() { return planActual; }
    public void setPlanActual(Plan planActual) { this.planActual = planActual; }

    public Plan getPlanSolicitado() { return planSolicitado; }
    public void setPlanSolicitado(Plan planSolicitado) { this.planSolicitado = planSolicitado; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
