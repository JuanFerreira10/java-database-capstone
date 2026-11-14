package com.smartclinic.service;

import com.smartclinic.model.Appointment;
import com.smartclinic.model.Doctor;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Obtiene la lista de todos los doctores registrados.
     */
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Guarda un nuevo doctor validando que no exista previamente por correo.
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctor.getEmail() != null && doctorRepository.existsByEmail(doctor.getEmail())) {
                return -1; // Ya existe
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Actualiza la información de un doctor existente.
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1; // No encontrado
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Elimina un doctor por su ID.
     */
    @Transactional
    public int deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1; // No encontrado
            }
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Valida las credenciales de inicio de sesión de un doctor y genera un token JWT.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> validateDoctor(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(email);
        if (doctorOpt.isEmpty() || !doctorOpt.get().getPassword().equals(password)) {
            response.put("message", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Doctor doctor = doctorOpt.get();
        String token = tokenService.generateToken(doctor.getEmail(), "DOCTOR");

        response.put("message", "Inicio de sesión exitoso");
        response.put("token", token);
        response.put("doctor", doctor);
        return ResponseEntity.ok(response);
    }

    /**
     * Calcula los horarios disponibles de un doctor en una fecha específica (jornada de 9:00 a 17:00).
     */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        List<String> allSlots = generateStandardTimeSlots();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay
        );

        List<String> bookedTimes = bookedAppointments.stream()
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .toList();

        List<String> availableSlots = new ArrayList<>();
        for (String slot : allSlots) {
            if (!bookedTimes.contains(slot)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    /**
     * Filtra doctores por nombre, especialidad y turno (AM/PM).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> allDoctors = doctorRepository.findAll();

        List<Doctor> filtered = allDoctors.stream().filter(doc -> {
            boolean matchesName = (name == null || name.equalsIgnoreCase("null") || name.equalsIgnoreCase("all") ||
                    doc.getFullName().toLowerCase().contains(name.toLowerCase()));

            boolean matchesSpecialty = (specialty == null || specialty.equalsIgnoreCase("null") || specialty.equalsIgnoreCase("all") ||
                    doc.getSpecialty().toLowerCase().contains(specialty.toLowerCase()));

            boolean matchesTime = true;
            if (time != null && !time.equalsIgnoreCase("null") && !time.equalsIgnoreCase("all")) {
                // Suposición de horario matutino (AM) o vespertino (PM) según turno o disponibilidad
                if (time.equalsIgnoreCase("AM")) {
                    matchesTime = doc.getShift() != null && doc.getShift().equalsIgnoreCase("AM");
                } else if (time.equalsIgnoreCase("PM")) {
                    matchesTime = doc.getShift() != null && doc.getShift().equalsIgnoreCase("PM");
                }
            }

            return matchesName && matchesSpecialty && matchesTime;
        }).toList();

        response.put("doctors", filtered);
        return response;
    }

    private List<String> generateStandardTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime time = LocalTime.of(9, 0);
        while (!time.isAfter(LocalTime.of(17, 0))) {
            slots.add(time.toString());
            time = time.plusHours(1);
        }
        return slots;
    }
}
