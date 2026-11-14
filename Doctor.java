package com.smartclinic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario asociado es obligatorio")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "La especialidad médica es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @NotBlank(message = "El nombre del doctor es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "El apellido del doctor es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "El número de licencia médica es obligatorio")
    @Size(max = 50, message = "El número de licencia no puede exceder los 50 caracteres")
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Pattern(regexp = "^[+]?[0-9\\s-]{7,20}$", message = "Formato de teléfono inválido")
    @Column(name = "phone", length = 20)
    private String phone;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorSchedule> schedules = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    // Constructores
    public Doctor() {
    }

    public Doctor(User user, Specialty specialty, String firstName, String lastName, String licenseNumber, String phone) {
        this.user = user;
        this.specialty = specialty;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<DoctorSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<DoctorSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    // Método auxiliar para nombre completo
    public String getFullName() {
        return "Dr. " + this.firstName + " " + this.lastName;
    }
}