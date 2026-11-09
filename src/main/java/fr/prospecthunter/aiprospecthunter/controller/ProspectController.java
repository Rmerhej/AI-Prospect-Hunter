package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import fr.prospecthunter.aiprospecthunter.service.ProspectScoringService;
@Controller
@RequestMapping("/prospects")
public class ProspectController {

    private final ProspectRepository prospectRepository;
    private final ProspectScoringService prospectScoringService;
    public ProspectController(
            ProspectRepository prospectRepository,
            ProspectScoringService prospectScoringService) {

        this.prospectRepository = prospectRepository;
        this.prospectScoringService = prospectScoringService;
    }

    // Afficher le formulaire
    @GetMapping("/nouveau")
    public String showForm(Model model) {
        model.addAttribute("prospect", new Prospect());
        return "prospects/form";
    }

    // Enregistrer le prospect
    @PostMapping
    public String saveProspect(
            @Valid @ModelAttribute("prospect") Prospect prospect,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "prospects/form";
        }

        boolean isNew = prospect.getId() == null;

        int score = prospectScoringService.calculateScore(prospect);

        prospect.setScore(score);

        String qualification =
                prospectScoringService.determineQualification(score);

        prospect.setQualification(qualification);

        prospectRepository.save(prospect);

        String message = isNew
                ? "Prospect enregistré avec succès !"
                : "Prospect modifié avec succès !";

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/prospects";
    }
    // Afficher la liste des prospects
    @GetMapping
    public String listProspects(Model model) {

        List<Prospect> prospects = prospectRepository.findAll();

        model.addAttribute("prospects", prospects);

        return "prospects/list";
    }
    // Afficher le formulaire de modification
    @GetMapping("/modifier/{id}")
    public String editProspect(@PathVariable Long id, Model model) {

        Prospect prospect = prospectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Prospect introuvable : " + id));

        model.addAttribute("prospect", prospect);

        return "prospects/form";
    }
    // Supprimer un prospect
    @PostMapping("/supprimer/{id}")
    public String deleteProspect(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        prospectRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Prospect supprimé avec succès !"
        );

        return "redirect:/prospects";
    }
}