# PagoFlex · Microservicios

Backend de PagoFlex, la app que reúne en un solo lugar los pagos mensuales de una persona (arriendo, colegio,
créditos, clubes, seguros) y le avisa antes de que venzan. Las empresas registran los cobros y el equipo de
soporte atiende los reclamos.

Todos los datos viven en estos microservicios con MySQL, así que lo que hace una persona en un teléfono lo ven
todos los demás al instante. La app Android está en https://github.com/WesLLVR/PagoFlexApp

## Qué hay en el repositorio

```
eureka-server         8761   registro de los microservicios
api-gateway           8080   puerta de entrada única para la app
usuarios-service      8081   cuentas, login, recuperar clave, perfil y foto
compromisos-service   8082   empresas y compromisos de pago (cuotas)
pagos-service         8083   pagos, comprobantes, historial y cumplimiento
reportes-service      8084   reportes de problema y su seguimiento
```

Cada microservicio tiene su propia base de datos y no lee las tablas de otro. Cuando necesita un dato ajeno
se lo pide al otro servicio por HTTP con **WebClient**, usando el nombre con que quedó registrado en Eureka:

| Servicio | Base de datos | Le pregunta a |
|---|---|---|
| usuarios-service | `pagoflex_usuarios` | — |
| compromisos-service | `pagoflex_compromisos` | usuarios (buscar o crear a la persona por RUT) |
| pagos-service | `pagoflex_pagos` | compromisos (validar la cuota y marcarla pagada) |
| reportes-service | `pagoflex_reportes` | compromisos (dejarlo en revisión) y usuarios (validar al agente) |

La app solo conoce al gateway (`http://<ip>:8080/`), y el gateway reparte cada ruta a su servicio:

| Ruta | Servicio |
|---|---|
| `/api/usuarios/**` | usuarios-service |
| `/api/empresas/**`, `/api/compromisos/**` | compromisos-service |
| `/api/pagos/**` | pagos-service |
| `/api/reportes/**` | reportes-service |

## Cómo levantar todo

Se necesita JDK 21, IntelliJ IDEA y MySQL en el puerto 3306 con usuario `root` sin contraseña (Laragon o XAMPP).
Las bases de datos se crean solas la primera vez.

1. Iniciar MySQL en Laragon.
2. Abrir y ejecutar `eureka-server` y esperar a que diga *Started*. En `http://localhost:8761` se ve el panel.
3. Ejecutar `usuarios-service`, `compromisos-service`, `pagos-service` y `reportes-service` (en ese orden o en cualquiera).
4. Ejecutar `api-gateway` al final.
5. En el panel de Eureka deben aparecer los cinco servicios. Recién ahí abrir la app.

Si al llamar a un servicio aparece *503* justo después de levantarlo, hay que esperar unos segundos a que
Eureka lo registre.

La primera vez que arranca cada servicio se cargan datos de prueba. Si se quiere empezar de cero hay que borrar las
cuatro bases `pagoflex_*` en MySQL y volver a levantar los servicios (los datos de prueba dependen unos de otros,
por eso conviene borrar las cuatro juntas).

## Documentación con Swagger

Cada microservicio publica su documentación OpenAPI y se puede probar desde el navegador:

| Servicio | Swagger UI |
|---|---|
| usuarios-service | http://localhost:8081/swagger-ui.html |
| compromisos-service | http://localhost:8082/swagger-ui.html |
| pagos-service | http://localhost:8083/swagger-ui.html |
| reportes-service | http://localhost:8084/swagger-ui.html |

## Cuentas de prueba

Todas usan la contraseña `123456`.

| Perfil | Correo | Qué puede hacer |
|---|---|---|
| Usuario final | `camila@pagoflex.cl` | Tiene cuotas al día, una vencida y historial de 9 meses |
| Usuario final | `luis@pagoflex.cl` | Cuota pie de un departamento (36 cuotas), con una en revisión |
| Agente de soporte | `andres@pagoflex.cl`, `daniela@pagoflex.cl` | Toma y resuelve reportes |
| Ejecutivo | `paula@pagoflex.cl` (Altos del Maipo), `claudia@pagoflex.cl` (Crédito Andino) | Registra, anula y elimina compromisos |

Hay más personas: `rosa`, `matias`, `fernanda`, `hector`, `javiera` y `benjamin` (usuarios finales), `ricardo`,
`valentina` y `tomas` (agentes) y `marco`, `felipe`, `gloria`, `sebastian` y `monica` (ejecutivos), todos `@pagoflex.cl`.

Cuando un ejecutivo registra un compromiso para un RUT que no existe, se crea la persona sin contraseña.
Esa persona entra a la app con **¿Olvidaste tu contraseña?**: recibe un código de 6 números como
notificación y crea su clave.

## Fotos

Las fotos (perfil y comprobante del reporte) se guardan como archivo en la carpeta `uploads/` de cada
microservicio. En MySQL solo queda el nombre del archivo y la respuesta entrega la URL para mostrarla.

## Endpoints

Todas las rutas se pueden probar a través del gateway en `http://localhost:8080`.
Los errores responden con `{"message": "..."}` y la app muestra ese texto.

### usuarios-service

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/usuarios?buscar=&rol=&pagina=&tamano=` | Lista paginada |
| GET | `/api/usuarios/{id}` | Un usuario · 404 |
| GET | `/api/usuarios/rut/{rut}` | Buscar por RUT · 404 |
| POST | `/api/usuarios` | Crear persona (lo usa compromisos-service) · 201 · 409 si el RUT o correo existe |
| POST | `/api/usuarios/login` | `{"correo","contrasena"}` · 401 si no coincide |
| POST | `/api/usuarios/recuperar` | `{"correo"}` · entrega el código |
| POST | `/api/usuarios/restablecer` | `{"correo","codigo","nueva"}` · 204 · 400 si el código no sirve |
| PUT | `/api/usuarios/{id}` | Actualizar perfil |
| PUT | `/api/usuarios/{id}/contrasena` | `{"actual","nueva"}` · 204 · 401 si la actual es incorrecta |
| POST | `/api/usuarios/{id}/avatar` | Subir foto (multipart, campo `archivo`) |
| GET | `/api/usuarios/{id}/avatar` | Ver la foto |
| DELETE | `/api/usuarios/{id}/avatar` | Quitar la foto |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario · 204 |

### compromisos-service

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/api/empresas`, `/api/empresas/{id}` | Empresas cliente |
| GET | `/api/compromisos?usuarioId=&empresaId=&estado=&buscar=&mes=&pagina=&tamano=` | Lista paginada. `estado` puede ser `VIGENTE`, `PENDIENTE`, `VENCIDO`, `EN_REVISION`, `PAGADO` o `ANULADO` |
| GET | `/api/compromisos/{id}` | Un compromiso (con recargo si está vencido) |
| GET | `/api/compromisos/{id}/cuotas` | Todas las cuotas de la misma deuda |
| GET | `/api/compromisos/{id}/pagable` | Si se puede pagar o qué cuota hay que pagar antes |
| GET | `/api/compromisos/resumen-mes?usuarioId=` | Total del mes, próximo vencimiento y vencidos |
| GET | `/api/compromisos/proximos?usuarioId=&dias=` | Pendientes por vencer y vencidos (para los avisos) |
| GET | `/api/compromisos/considerados?usuarioId=` | Compromisos de los últimos 12 meses (para el cumplimiento) |
| GET | `/api/compromisos/resumen-cobranza?empresaId=&mes=` | Resumen del mes para la empresa |
| POST | `/api/compromisos` | Registrar compromiso · 201 · 400 · 409 |
| PATCH | `/api/compromisos/{id}/estado` | Cambiar estado (lo usan pagos y reportes) · 409 si el cambio no corresponde |
| PATCH | `/api/compromisos/{id}/anular` | `{"motivo","comentario","ejecutivoId"}` |
| DELETE | `/api/compromisos/{id}` | Eliminar (solo pendientes) · 204 |

### pagos-service

| Método | Ruta | Para qué |
|---|---|---|
| POST | `/api/pagos` | `{"compromisoId","medioPago"}` · 201 con el pago y la siguiente cuota · 409 si ya está pagado o hay una cuota anterior |
| GET | `/api/pagos?usuarioId=&empresa=&desde=&hasta=&buscar=&pagina=&tamano=` | Historial paginado |
| GET | `/api/pagos/empresas?usuarioId=` | Empresas con pagos (para el filtro) |
| GET | `/api/pagos/cumplimiento?usuarioId=` | Porcentaje de pagos a tiempo |
| GET | `/api/pagos/compromiso/{id}` | Comprobante de un compromiso · 404 |
| GET | `/api/pagos/{id}` | Un pago |

### reportes-service

| Método | Ruta | Para qué |
|---|---|---|
| POST | `/api/reportes` | Crear reporte · deja el compromiso *En revisión* |
| GET | `/api/reportes?estado=&usuarioId=&agenteId=&buscar=&soloAtrasados=&pagina=&tamano=` | Lista paginada (los atrasados primero) |
| GET | `/api/reportes/resumen` | Contadores por estado |
| GET | `/api/reportes/{id}` | Un reporte con su historial |
| GET | `/api/reportes/compromiso/{id}` | Último reporte de un compromiso · 404 |
| PATCH | `/api/reportes/{id}/tomar` | `{"agenteId"}` · Abierto → En gestión |
| PATCH | `/api/reportes/{id}/resolver` | `{"agenteId","respuesta"}` · En gestión → Resuelto |
| PATCH | `/api/reportes/{id}/reabrir` | `{"usuarioId","comentario"}` · Resuelto → En gestión |
| PATCH | `/api/reportes/{id}/cerrar` | `{"usuarioId"}` · Resuelto → Cerrado y el compromiso vuelve a pendiente |
| POST | `/api/reportes/{id}/foto` | Subir foto del comprobante (multipart, campo `archivo`) |
| GET | `/api/reportes/{id}/foto` | Ver la foto |
| DELETE | `/api/reportes/{id}/foto` | Quitar la foto |

## Pruebas unitarias y cobertura

Cada servicio tiene pruebas de su `Service` y de sus clientes WebClient (con Mockito), de su `Controller` (con MockMvc),
del almacenamiento de fotos, de los datos iniciales y de las utilidades.

| Servicio | Pruebas | Cobertura de líneas |
|---|---|---|
| usuarios-service | 41 | 96% |
| compromisos-service | 40 | 99% |
| pagos-service | 26 | 98% |
| reportes-service | 38 | 97% |

La cobertura se mide con JaCoCo. En cada servicio:

```
mvn verify
```

Ejecuta las pruebas, genera el informe en `target/site/jacoco/index.html` y falla si la cobertura de líneas baja
del 80%. En IntelliJ también se puede usar clic derecho en `src/test/java` → *Run 'All Tests' with Coverage*.
## Notas

- En Spring Boot 4 la propiedad para que el error incluya el mensaje es `spring.web.error.include-message=always`
  (la antigua `server.error.include-message` ya no funciona).
- El gateway tiene `trusted-proxies: ".*"` para que las URL de las fotos se armen con la dirección del gateway y no
  con el puerto interno del servicio.
