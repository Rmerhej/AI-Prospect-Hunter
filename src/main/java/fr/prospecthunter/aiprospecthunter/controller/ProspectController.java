package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.dto.ProspectForm;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.HorizonProjet;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.enums.StatutFinancement;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.exception.ProspectNotFoundException;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import fr.prospecthunter.aiprospecthunter.service.ProspectScoringService;
import fr.prospecthunter.aiprospecthunter.service.ProspectSearchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prospects")
public class ProspectController {

    private final ProspectRepository prospectRepository;
    private final ProspectScoringService prospectScoringService;
    private final ProspectSearchService prospectSearchService;

    public ProspectController(ProspectRepository prospectRepository,
                              ProspectScoringService prospectScoringService,
                              ProspectSearchService prospectSearchService) {
        this.prospectRepository = prospectRepository;
        this.prospectScoringService = prospectScoringService;
        this.prospectSearchService = prospectSearchService;
    }

    // ----- Listes déroulantes, disponibles dans toutes les vues de ce contrôleur -----

    @ModelAttribute("statuses")
    public ProspectStatus[] statuses() {
        return ProspectStatus.values();
    }

    @ModelAttribute("statutsFinancement")
    public StatutFinancement[] statutsFinancement() {
        return StatutFinancement.values();
    }

    @ModelAttribute("segments")
    public TargetSegment[] segments() {
        return TargetSegment.values();
    }

    @ModelAttribute("horizonsProjet")
    public HorizonProjet[] horizonsProjet() {
        return HorizonProjet.values();
    }

    // ----- Liste + recherche multicritère paginée -----

    @GetMapping
    public String listProspects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String qualification,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Prospect> prospects = prospectSearchService.search(name, city, qualification, page, size);

        model.addAttribute("prospects", prospects);
        model.addAttribute("name", name);
        model.addAttribute("city", city);
        model.addAttribute("qualification", qualification);
        // Valeurs corrigées par le service (page négative, taille trop grande...)
        model.addAttribute("currentPage", prospects.getNumber());
        model.addAttribute("pageSize", prospects.getSize());
        model.addAttribute("totalPages", prospects.getTotalPages());
        model.addAttribute("totalItems", prospects.getTotalElements());
        // Vrai si au moins un filtre est rempli : sert à adapter le message "aucun résultat"
        model.addAttribute("hasFilters", isFilled(name) || isFilled(city) || isFilled(qualification));

        return "prospects/list";
    }

    // ----- Création / modification -----

    @GetMapping("/nouveau")
    public String showForm(Model model) {
        model.addAttribute("prospect", new ProspectForm());
        return "prospects/form";
    }

    @GetMapping("/modifier/{id}")
    public String editProspect(@PathVariable Long id, Model model) {
        model.addAttribute("prospect", ProspectForm.fromEntity(findProspect(id)));
        return "prospects/form";
    }

    @PostMapping
    public String saveProspect(@Valid @ModelAttribute("prospect") ProspectForm form,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "prospects/form";
        }

        boolean isNew = form.getId() == null;

        // En modification, on repart de l'entité en base : createdAt et les autres champs sont conservés
        Prospect prospect = isNew ? new Prospect() : findProspect(form.getId());
        form.applyTo(prospect);

        int score = prospectScoringService.calculateScore(prospect);
        prospect.setScore(score);
        prospect.setQualification(prospectScoringService.determineQualification(score));

        prospectRepository.save(prospect);

        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Prospect enregistré avec succès !" : "Prospect modifié avec succès !");

        return "redirect:/prospects";
    }

    // ----- Suppression -----

    @PostMapping("/supprimer/{id}")
    public String deleteProspect(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        prospectRepository.delete(findProspect(id));
        redirectAttributes.addFlashAttribute("successMessage", "Prospect supprimé avec succès !");
        return "redirect:/prospects";
    }

    private Prospect findProspect(Long id) {
        return prospectRepository.findById(id)
                .orElseThrow(() -> new ProspectNotFoundException(id));
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
