package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.dto.SearchProfileForm;
import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.service.SearchProfileService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Page "Profil de recherche" : poste visé, résumé du profil, zone géographique, signature.
 */
@Controller
@RequestMapping("/profil")
public class SearchProfileController {

    private final SearchProfileService profileService;

    public SearchProfileController(SearchProfileService profileService) {
        this.profileService = profileService;
    }

    @ModelAttribute("allDepartements")
    public Departement[] allDepartements() {
        return Departement.values();
    }

    @GetMapping
    public String showProfile(Model model) {
        model.addAttribute("profil", SearchProfileForm.fromEntity(profileService.get()));
        return "profil";
    }

    @PostMapping
    public String saveProfile(@Valid @ModelAttribute("profil") SearchProfileForm form,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profil";
        }
        SearchProfile profile = profileService.get();
        form.applyTo(profile);
        profileService.save(profile);
        redirectAttributes.addFlashAttribute("successMessage",
                "Profil enregistré. Il s'applique aux prochains imports et aux prochaines analyses IA.");
        return "redirect:/profil";
    }
}
