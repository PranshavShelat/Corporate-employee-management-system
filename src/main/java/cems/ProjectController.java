package cems;

import cems.models.Department;
import cems.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProjectController {
    // GRASP PRINCIPLE 4: CONTROLLER (Member 3)
    // Separating Department/Project routing from the main Employee Controller
    
    @Autowired private DepartmentRepo deptRepo;
    @Autowired private ProjectRepo projRepo;

    @PostMapping("/add-department")
    public String addDept(@RequestParam String deptName, @RequestParam String location) {
        Department d = new Department();
        d.setDeptName(deptName);
        d.setLocation(location);
        deptRepo.save(d);
        return "redirect:/";
    }

    @PostMapping("/add-project")
    public String addProject(@RequestParam String title) {
        Project p = new Project();
        p.setTitle(title);
        p.setProgressPercent(0);
        projRepo.save(p);
        return "redirect:/";
    }
}