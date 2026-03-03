package io.github.wendelmax.vuecan.samples.basics;

import io.github.wendelmax.vuecan.web.VuecanResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class BasicsController {

    @GetMapping("/")
    public VuecanResponse<?> counter() {
        return VuecanResponse.ok()
                .with("initialCount", 10)
                .withContext("siteTheme", "dark")
                .withContext("userRole", "admin");
    }

    @GetMapping("/demo-flash")
    public String demoFlash(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("flashMessage", "Success! This came from TempData (Flash).");
        return "redirect:/";
    }
}
