package com.smartclinic.controller;

import com.smartclinic.model.Prescription;
import com.smartclinic.service.AppointmentService;
import com.smartclinic.service.PrescriptionService;
import com.smartclinic.service.TokenService;
import com.smartclinic.model.Appointment.AppointmentStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final TokenService tokenService;
    private final AppointmentService appointmentService;

    @Autowired
    public PrescriptionController(
            PrescriptionService prescriptionService,
            TokenService tokenService,
            AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.tokenService = tokenService;
        this.appointmentService = appointmentService;
    }

    /**
     * Guarda una nueva prescripción médica asociada a una cita (requiere token de doctor).
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @Valid @RequestBody Prescription prescription,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();

        if (!tokenService.validateToken(token, "doctor")) {
            response.put("message", "No autorizado: se requiere rol de doctor");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Marcar la cita como completada al emitir la prescripción
        if (prescription.getAppointmentId() != null) {
            appointmentService.changeStatus(prescription.getAppointmentId(), AppointmentStatus.COMPLETED);
        }

        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Obtiene la prescripción médica asociada a un ID de cita específico (requiere token de doctor o paciente).
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();

        if (!tokenService.validateToken(token, "doctor") && !tokenService.validateToken(token, "patient")) {
            response.put("message", "Token inválido o no autorizado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
