package io.github.wendelmax.vuecan.samples.forms;

import io.github.wendelmax.vuecan.web.VuecanResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FormController {

    @GetMapping("/")
    public VuecanResponse<?> index() {
        return VuecanResponse.ok().withView("forms/contact");
    }

    @PostMapping("/contact")
    public VuecanResponse<?> submitContact(@Valid ContactDTO contact,
            org.springframework.validation.BindingResult result) {
        if (result.hasErrors()) {
            return VuecanResponse.ok(contact)
                    .withView("forms/contact")
                    .withErrors(result);
        }

        // Simulating processing
        System.out.println("Processing contact: " + contact);

        return VuecanResponse.redirect("/?success=true");
    }

    public record ContactDTO(
            @NotBlank(message = "Name is required") String name,

            @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email,

            @NotBlank(message = "Message cannot be empty") @Size(min = 10, message = "Message must be at least 10 characters") String message) {
    }
}
