package cl.duocuc.pagos.model;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 12)
    private String numeroComprobante;

    @Column(name = "compromiso_id", nullable = false, unique = true)
    private Long compromisoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    @Column(nullable = false, length = 20)
    private String folio;

    @Column(nullable = false, length = 80)
    private String empresa;

    @Column(name = "razon_social", nullable = false, length = 100)
    private String razonSocial;

    @Column(nullable = false, length = 80)
    private String concepto;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "total_cuotas")
    private Integer totalCuotas;

    @Column(name = "monto_cuota", nullable = false)
    private Integer montoCuota;

    @Column(nullable = false)
    private LocalDate vencimiento;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "monto_pagado", nullable = false)
    private Integer montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Canal canal;

    @Column(name = "medio_pago", nullable = false, length = 30)
    private String medioPago;

    @Column(name = "a_tiempo", nullable = false)
    private Boolean aTiempo;
}
