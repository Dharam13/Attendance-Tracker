package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SubjectMaster;
import com.charusat.attendancetracker.repository.SubjectMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SubjectMasterService {
    List<SubjectMaster> getAllSubjects();
    SubjectMaster getSubjectById(Long id);
    void saveSubject(SubjectMaster subject);
    void deleteSubject(Long id);
}