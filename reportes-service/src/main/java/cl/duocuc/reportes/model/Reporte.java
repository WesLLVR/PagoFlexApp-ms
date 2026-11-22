package cl.duocuc.reportes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "compromiso_id", nullable = false)
    private Long compromisoId;

    @Column(nullable = false, length = 20)
    private String folio;

    @Column(nullable = false, length = 80)
    private String concepto;

    @Column(nullable = false, length = 80)
    private String empresa;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoReporte motivo;

    @Column(nullable = false, length = 300)
    private String comentario;

    @Column(length = 100)
    private String foto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado;

    @Column(name = "agente_id")
    private Long agenteId;

    @Column(name = "nombre_agente", length = 100)
    private String nombreAgente;

    @Column(length = 300)
    private String respuesta;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_toma")
    private LocalDateTime fechaToma;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
}
