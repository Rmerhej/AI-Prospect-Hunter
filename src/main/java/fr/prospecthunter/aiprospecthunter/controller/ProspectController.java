package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.HorizonProjet;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import fr.prospecthunter.aiprospecthunter.service.ProspectSearchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import fr.prospecthunter.aiprospecthunter.enums.StatutFinancement;
import org.springframework.web.bind.annotation.ModelAttribute;
import fr.prospecthunter.aiprospecthunter.service.ProspectScoringService;
@Controller
@RequestMapping("/prospects")
public class ProspectController {

    private final ProspectRepository prospectRepository;
    private final ProspectScoringService prospectScoringService;
    private final ProspectSearchService prospectSearchService;
    public ProspectController(
            ProspectRepository prospectRepository,
            ProspectScoringService prospectScoringService,
           ProspectSearchService prospectSearchService) {

        this.prospectRepository = prospectRepository;
        this.prospectScoringService = prospectScoringService;
        this.prospectSearchService = prospectSearchService;
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
    @GetMapping
    public String listProspects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String qualification,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Prospect> prospects = prospectSearchService.search(
                name, city, qualification, page, size
        );
        model.addAttribute("prospects", prospects);
        model.addAttribute("name", name);
        model.addAttribute("city", city);
        model.addAttribute("qualification", qualification);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prospects.getTotalPages());
        model.addAttribute("totalItems", prospects.getTotalElements());
        model.addAttribute("pageSize", size);

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
    @ModelAttribute("statutsFinancement")
    public StatutFinancement[] statutsFinancement() {
        return StatutFinancement.values();
    }
    @ModelAttribute("horizonsProjet")
    public HorizonProjet[] horizonsProjet() {
        return HorizonProjet.values();
    }
}