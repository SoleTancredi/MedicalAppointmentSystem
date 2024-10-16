package com.ts.medical_appointment_backend.service.impl;

import com.ts.medical_appointment_backend.controller.dtos.AppointmentDto;
import com.ts.medical_appointment_backend.entity.*;
import com.ts.medical_appointment_backend.exceptions.AppointmentNotFoundException;
import com.ts.medical_appointment_backend.repository.AppointmentRepository;
import com.ts.medical_appointment_backend.repository.DoctorRepository;
import com.ts.medical_appointment_backend.repository.PatientRepository;
import com.ts.medical_appointment_backend.service.AppointmentService;
import com.ts.medical_appointment_backend.service.mapper.AppointmentMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final DoctorRepository doctorRepository;

    private final PatientRepository patientRepository;

    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;

    //injection by constructor
    public AppointmentServiceImpl(DoctorRepository doctorRepository, PatientRepository patientRepository, AppointmentRepository appointmentRepository, AppointmentMapper appointmentMapper) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public boolean isDateTimeAvailable(Long doctorId, Date dateTimeAppointment) {
        return !appointmentRepository.existsByDoctorIdAndDateTimeAppointment(doctorId, dateTimeAppointment);
    }

    @Override
    public Appointment createAppointment(@Valid AppointmentDto appointmentDto) {

        Doctor doctor = doctorRepository.findByNameAndLastname(appointmentDto.getNameDoctor(), appointmentDto.getLastnameDoctor())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Patient patient = patientRepository.findByNameAndLastName(appointmentDto.getNamePatient(), appointmentDto.getLastnamePatient())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (!isDateTimeAvailable(doctor.getId(), appointmentDto.getDateTimeAppointment())) {
            throw new IllegalArgumentException("The date and time are already taken by another appointment for this doctor.");
        }

        // Create
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDateTimeAppointment(appointmentDto.getDateTimeAppointment());
        System.out.println(appointment.toString());

        return appointmentRepository.save(appointment);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));

        return appointmentMapper.toDto(appointment);
    }

    @Override
    public List<Appointment> getAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();

        System.out.println("muestro lista de appointments appointments: " + appointments.toString());
        //Convert the list of Appointment entities to a list of AppointmentDto entities using the mapper
        return appointments;
    }

    @Override
    public AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));

        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();

        if (patient != null) {
            patient.setName(appointmentDto.getNamePatient());
            patient.setLastName(appointmentDto.getLastnamePatient());
        }

        if (doctor != null) {
            doctor.setName(appointmentDto.getNameDoctor());
            doctor.setLastname(appointmentDto.getLastnameDoctor());
        }

        appointment.setDateTimeAppointment(appointmentDto.getDateTimeAppointment());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(updatedAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));

        appointmentRepository.delete(appointment);
    }
}
