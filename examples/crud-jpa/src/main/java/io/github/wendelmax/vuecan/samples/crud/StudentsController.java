package io.github.wendelmax.vuecan.samples.crud;

import io.github.wendelmax.vuecan.web.VuecanResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/students")
public class StudentsController {

    private final StudentRepository repository;

    public StudentsController(StudentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public VuecanResponse<?> index() {
        return VuecanResponse.ok(repository.findAll())
                .withView("students/index");
    }

    @GetMapping("/{id}")
    public VuecanResponse<?> details(@PathVariable Long id) {
        return VuecanResponse.ok(repository.findById(id))
                .withView("students/details");
    }

    @GetMapping("/create")
    public VuecanResponse<?> create() {
        return VuecanResponse.ok(new Student())
                .withView("students/create");
    }

    @PostMapping("/create")
    public VuecanResponse<?> createPost(@Valid @ModelAttribute Student student, BindingResult result) {
        if (result.hasErrors()) {
            return VuecanResponse.ok(student)
                    .withView("students/create")
                    .withErrors(result);
        }
        repository.save(student);
        return VuecanResponse.redirect("/students")
                .withContext("message", "Student created successfully!");
    }

    @GetMapping("/edit/{id}")
    public VuecanResponse<?> edit(@PathVariable Long id) {
        return VuecanResponse.ok(repository.findById(id))
                .withView("students/edit");
    }

    @PostMapping("/edit/{id}")
    public VuecanResponse<?> editPost(@PathVariable Long id, @Valid @ModelAttribute Student student,
            BindingResult result) {
        if (result.hasErrors()) {
            return VuecanResponse.ok(student)
                    .withView("students/edit")
                    .withErrors(result);
        }

        return repository.findById(id).map(existing -> {
            existing.setFirstName(student.getFirstName());
            existing.setLastName(student.getLastName());
            existing.setEnrollmentDate(student.getEnrollmentDate());
            repository.save(existing);
            return VuecanResponse.redirect("/students")
                    .withContext("message", "Student updated successfully!");
        }).orElseGet(VuecanResponse::notFound);
    }

    @GetMapping("/delete/{id}")
    public VuecanResponse<?> delete(@PathVariable Long id) {
        return VuecanResponse.ok(repository.findById(id))
                .withView("students/delete");
    }

    @PostMapping("/delete/{id}")
    public VuecanResponse<?> deletePost(@PathVariable Long id) {
        repository.deleteById(id);
        return VuecanResponse.redirect("/students")
                .withContext("message", "Student deleted successfully!");
    }
}
