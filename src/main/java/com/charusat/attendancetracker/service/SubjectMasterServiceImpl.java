package com.charusat.attendancetracker.service;

import com.charusat.attendancetracker.entity.SubjectMaster;
import com.charusat.attendancetracker.repository.SubjectMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SubjectMasterServiceImpl implements SubjectMasterService {

    @Autowired
    private SubjectMasterRepository subjectMasterRepository;

    @Override
    public List<SubjectMaster> getAllSubjects() {
        return Collections.emptyList(); // placeholder
    }

    @Override
    public SubjectMaster getSubjectById(Long id) {
        return null; // placeholder
    }

    @Override
    public void saveSubject(SubjectMaster subject) {
        // placeholder - do nothing
    }

    @Override
    public void deleteSubject(Long id) {
        // placeholder - do nothing
    }
}
