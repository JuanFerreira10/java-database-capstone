package com.smartclinic.service;

import com.smartclinic.model.Appointment;
import com.smartclinic.model.Appointment.AppointmentStatus;
import com.smartclinic.model.Doctor;
import com.smartclinic.model.Patient;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.DoctorRepository;
import com.smartclinic.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /**
     * Reserva una nueva cita médica validando disponibilidad y evitando solapamientos.
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            if (appointment.getDoctor() == null || appointment.getPatient() == null) {
                return 0;
            }

            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
            Optional<Patient> patientOpt = patientRepository.findById(appointment.getPatient().getId());

            if (doctorOpt.isEmpty() || patientOpt.isEmpty()) {
                return 0;
            }

            // Verificar si el doctor ya tiene una cita en esa misma fecha y hora
            boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusNot(
                    doctorOpt.get().getId(),
                    appointment.getAppointmentTime(),
                    AppointmentStatus.CANCELLED
            );

            if (exists) {
                return -1; // Horario no disponible
            }

            appointment.setDoctor(doctorOpt.get());
            appointment.setPatient(patientOpt.get());
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Actualiza una cita existente verificando que el paciente sea el propietario y que el horario esté libre.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (existingOpt.isEmpty()) {
            response.put("message", "La cita no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment existing = existingOpt.get();

        // Validar que el paciente que actualiza sea el mismo de la cita
        if (appointment.getPatient() != null &&
                !existing.getPatient().getId().equals(appointment.getPatient().getId())) {
            response.put("message", "No autorizado para modificar esta cita");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Validar disponibilidad en el nuevo horario si cambió la fecha/hora
        if (appointment.getAppointmentTime() != null &&
                !existing.getAppointmentTime().equals(appointment.getAppointmentTime())) {
            boolean conflict = appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusNot(
                    existing.getDoctor().getId(),
                    appointment.getAppointmentTime(),
                    AppointmentStatus.CANCELLED
            );
            if (conflict) {
                response.put("message", "El doctor no está disponible en el horario seleccionado");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            existing.setAppointmentTime(appointment.getAppointmentTime());
        }

        if (appointment.getReason() != null) {
            existing.setReason(appointment.getReason());
        }
        if (appointment.getStatus() != null) {
            existing.setStatus(appointment.getStatus());
        }

        appointmentRepository.save(existing);
        response.put("message", "Cita actualizada exitosamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Cancela una cita existente verificando la identidad del paciente mediante su token.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            response.put("message", "Cita no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment appointment = appointmentOpt.get();
        String patientEmail = tokenService.extractIdentifier(token);

        if (patientEmail == null || !appointment.getPatient().getEmail().equalsIgnoreCase(patientEmail)) {
            response.put("message", "No autorizado para cancelar esta cita");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        response.put("message", "Cita cancelada exitosamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene las citas de un doctor en una fecha específica, con filtro opcional por nombre de paciente.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();

        String doctorEmail = tokenService.extractIdentifier(token);
        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(doctorEmail);

        if (doctorOpt.isEmpty()) {
            response.put("message", "Doctor no encontrado o token inválido");
            return response;
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;
        if (pname != null && !pname.isBlank() && !pname.equalsIgnoreCase("null") && !pname.equalsIgnoreCase("all")) {
            appointments = appointmentRepository.findByDoctorIdAndPatientFullNameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorOpt.get().getId(), pname, startOfDay, endOfDay
            );
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorOpt.get().getId(), startOfDay, endOfDay
            );
        }

        response.put("appointments", appointments);
        return response;
    }

    /**
     * Cambia el estado de una cita (por ejemplo, a COMPLETED o NO_SHOW).
     */
    @Transactional
    public void changeStatus(long id, AppointmentStatus status) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
        });
    }
}
