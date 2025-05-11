package com.charusat.attendancetracker.controller;

import com.charusat.attendancetracker.entity.SubjectMaster;
import com.charusat.attendancetracker.service.SubjectMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/subjects")
public class SubjectController {

    @Autowired
    private SubjectMasterService subjectMasterService;

    /**
     * Display subjects management page
     */
    @GetMapping
    public String showSubjectsManagement(Model model) {
        model.addAttribute("subjects", subjectMasterService.getAllSubjects());
        return "admin_dashboard";
    }

    /**
     * Save a new subject or update an existing one
     */
    @PostMapping("/save")
    public String saveSubject(
            @RequestParam(required = false) Long subjectId,
            @RequestParam String subjectCode,
            @RequestParam String subjectName,
            @RequestParam String subjectDepartment,
            @RequestParam Integer subjectSemester,
            RedirectAttributes redirectAttributes) {

        try {
            SubjectMaster subject = new SubjectMaster();
            if (subjectId != null) {
                subject.setId(subjectId);
            }

            subject.setSubjectCode(subjectCode);
            subject.setSubjectName(subjectName);
            subject.setDepartment(subjectDepartment);
            subject.setSemester(subjectSemester);

            subjectMasterService.saveSubject(subject);

            redirectAttributes.addFlashAttribute("successMessage",
                    subjectId == null ?
                            "Subject added successfully!" :
                            "Subject updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error saving subject: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Delete a subject
     */
    @PostMapping("/delete")
    public String deleteSubject(
            @RequestParam Long id,
            RedirectAttributes redirectAttributes) {

        try {
            subjectMasterService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Subject deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting subject: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Edit a subject
     */
    @PostMapping("/edit")
    public String editSubject(
            @RequestParam Long id,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            SubjectMaster subject = subjectMasterService.getSubjectById(id);
            model.addAttribute("subjectToEdit", subject);
            model.addAttribute("subjects", subjectMasterService.getAllSubjects());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error loading subject for edit: " + e.getMessage());
        }

        return "admin_dashboard";
    }
}