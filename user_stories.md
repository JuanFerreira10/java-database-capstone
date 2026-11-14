# Historias de Usuario - Sistema de Gestión de Clínicas Inteligentes

Este documento detalla las historias de usuario funcionales definidas para los diferentes roles dentro de la plataforma del **Sistema de Gestión de Clínicas Inteligentes**, siguiendo el formato estándar de desarrollo ágil (*Como [rol], quiero [acción], para [beneficio]*) junto con sus criterios de aceptación.

---

## 👨‍⚕️ Rol: Doctor

### HU-DOC-01: Gestión de agenda y disponibilidad
* **Como** Doctor,
* **Quiero** configurar y actualizar mis horarios de atención y disponibilidad diaria,
* **Para** que los pacientes solo puedan reservar citas en los bloques horarios en los que realmente estoy disponible.
* **Criterios de Aceptación:**
  * El doctor puede definir días laborables y franjas horarias.
  * El sistema valida que no existan solapamientos de horarios.
  * Los cambios se reflejan inmediatamente en el portal de reservas.

### HU-DOC-02: Consulta de citas programadas
* **Como** Doctor,
* **Quiero** visualizar mi lista de citas programadas filtradas por fecha (hoy, semana, mes),
* **Para** organizar mi jornada clínica y prepararme con antelación para cada consulta.
* **Criterios de Aceptación:**
  * Se muestra el nombre del paciente, hora de la cita, motivo de consulta y estado (Pendiente, Confirmada, Completada).
  * Posibilidad de acceder directamente al perfil del paciente desde la cita.

### HU-DOC-03: Registro de historial clínico y diagnósticos
* **Como** Doctor,
* **Quiero** registrar notas médicas, diagnósticos y observaciones durante o después de una consulta,
* **Para** mantener actualizado el historial clínico electrónico del paciente en la base de datos.
* **Criterios de Aceptación:**
  * El registro se almacena de forma segura en la base de datos documental (NoSQL) vinculada al ID del paciente.
  * Incluye marca de tiempo automática y firma digital (ID del doctor).
  * Los registros previos son de solo lectura para mantener la integridad médica.

### HU-DOC-04: Emisión de recetas médicas electrónicas
* **Como** Doctor,
* **Quiero** generar prescripciones médicas especificando medicamentos, dosis, frecuencia y duración del tratamiento,
* **Para** que el paciente reciba indicaciones claras y pueda descargarlas desde su portal.
* **Criterios de Aceptación:**
  * Validación de campos obligatorios antes de guardar la receta.
  * Generación de un identificador único de receta vinculado a la cita médica.

---

## 🧑‍💼 Rol: Paciente

### HU-PAC-01: Registro y gestión de perfil personal
* **Como** Paciente,
* **Quiero** registrarme en la plataforma y actualizar mis datos personales y de contacto,
* **Para** acceder a los servicios médicos de la clínica de manera personalizada.
* **Criterios de Aceptación:**
  * Validación de formato de correo electrónico, teléfono y documento de identidad único.
  * Contraseña almacenada de forma encriptada.

### HU-PAC-02: Búsqueda de médicos y especialidades
* **Como** Paciente,
* **Quiero** buscar doctores filtrando por especialidad médica, nombre o disponibilidad,
* **Para** encontrar al profesional adecuado según mi necesidad de salud.
* **Criterios de Aceptación:**
  * El buscador devuelve resultados en tiempo real consumiendo la API REST correspondiente.
  * Se muestra la especialidad, calificación/experiencia y próximos horarios disponibles del doctor.

### HU-PAC-03: Reserva, reprogramación y cancelación de citas
* **Como** Paciente,
* **Quiero** agendar una cita médica en un horario disponible, así como reprogramarla o cancelarla si surge un imprevisto,
* **Para** gestionar mi atención médica de forma autónoma sin llamar por teléfono.
* **Criterios de Aceptación:**
  * El sistema impide reservar dos citas a la misma hora con el mismo doctor.
  * Las cancelaciones deben realizarse con al menos 24 horas de antelación.
  * El paciente recibe una confirmación visual del estado de su cita.

### HU-PAC-04: Consulta de historial médico y recetas
* **Como** Paciente,
* **Quiero** visualizar mis consultas pasadas, diagnósticos y recetas emitidas,
* **Para** llevar un seguimiento de mis tratamientos y presentar mis recetas en la farmacia.
* **Criterios de Aceptación:**
  * Acceso exclusivo a los registros propios mediante autenticación segura.
  * Visualización clara de medicamentos recetados e instrucciones del doctor.

---

## 🛡️ Rol: Administrador

### HU-ADM-01: Gestión de usuarios y roles (CRUD)
* **Como** Administrador,
* **Quiero** dar de alta, editar, desactivar y asignar roles a los usuarios del sistema (Doctores, Pacientes y otros Administradores),
* **Para** mantener el control de acceso y la seguridad operativa de la clínica.
* **Criterios de Aceptación:**
  * Solo usuarios con rol `ADMIN` pueden acceder a estos endpoints.
  * Validación de integridad referencial antes de desactivar a un doctor con citas pendientes.

### HU-ADM-02: Administración de especialidades y consultorios
* **Como** Administrador,
* **Quiero** registrar y gestionar las especialidades médicas y salas de consulta disponibles en la clínica,
* **Para** mantener estructurada la oferta de servicios médicos.
* **Criterios de Aceptación:**
  * No se permiten especialidades duplicadas en la base de datos relacional.
  * Posibilidad de asociar uno o más doctores a una especialidad.

### HU-ADM-03: Generación de reportes operativos y estadísticos
* **Como** Administrador,
* **Quiero** ejecutar reportes sobre citas atendidas, tasas de cancelación y demanda por especialidad en un rango de fechas,
* **Para** tomar decisiones informadas sobre la gestión de recursos de la clínica.
* **Criterios de Aceptación:**
  * Los datos se extraen mediante procedimientos almacenados optimizados en la base de datos.
  * Los resultados se presentan de forma estructurada y pueden exportarse o visualizarse en el panel administrativo.

### HU-ADM-04: Supervisión y auditoría de citas del sistema
* **Como** Administrador,
* **Quiero** visualizar el estado global de todas las citas de la clínica e intervenir en caso de emergencias o ausencias de médicos,
* **Para** garantizar la continuidad del servicio y reasignar pacientes si es necesario.
* **Criterios de Aceptación:**
  * Filtros avanzados por doctor, paciente, fecha y estado de la cita.
  * Registro de auditoría cuando un administrador modifica una cita existente.