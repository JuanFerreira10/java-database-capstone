package com.smartclinic.repository;

import com.smartclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Busca un paciente por su dirección de correo electrónico.
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Busca un paciente por su correo electrónico o número de teléfono.
     */
    Optional<Patient> findByEmailOrPhone(String email, String phone);

    /**
     * Verifica si ya existe un paciente registrado con el correo electrónico proporcionado.
     */
    boolean existsByEmail(String email);
}
