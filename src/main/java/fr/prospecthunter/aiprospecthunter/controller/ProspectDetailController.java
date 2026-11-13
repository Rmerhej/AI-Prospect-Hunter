package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.ai.AiUnavailableException;
import fr.prospecthunter.aiprospecthunter.dto.EmailDraftForm;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.TrancheEffectif;
import fr.prospecthunter.aiprospecthunter.exception.ProspectNotFoundException;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import fr.prospecthunter.aiprospecthunter.service.ProspectAnalysisService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Fiche d'un prospect : informations, analyse IA et brouillon d'e-mail.
 */
@Controller
@RequestMapping("/prospects/{id}")
public class ProspectDetailController {

    private final ProspectRepository prospectRepository;
    private final ProspectAnalysisService analysisService;

    public ProspectDetailController(ProspectRepository prospectRepository,
                                    ProspectAnalysisService analysisService) {
        this.prospectRepository = prospectRepository;
        this.analysisService = analysisService;
    }

    @GetMapping
    public String showDetail(@PathVariable Long id, Model model) {
        Prospect prospect = find(id);
        model.addAttribute("prospect", prospect);
        model.addAttribute("trancheLibelle", TrancheEffectif.libelle(prospect.getTrancheEffectif()));

        // Formulaire du brouillon, pré-rempli (sauf si on revient d'une erreur de validation)
        if (!model.containsAttribute("draft")) {
            EmailDraftForm draft = new EmailDraftForm();
            draft.setSubject(prospect.getEmailSubject());
            draft.setBody(prospect.getEmailDraft());
            model.addAttribute("draft", draft);
        }
        return "prospects/detail";
    }

    @PostMapping("/analyser")
    public String analyze(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            analysisService.analyze(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Analyse terminée. Relisez-la avant d'utiliser le brouillon.");
        } catch (AiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prospects/" + id;
    }

    @PostMapping("/appliquer-besoin")
    public String applyNeed(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (analysisService.applyProposedNeed(id)) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Besoin enregistré, score recalculé.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Aucun besoin proposé : lancez d'abord l'analyse.");
        }
        return "redirect:/prospects/" + id;
    }

    @PostMapping("/brouillon")
    public String saveDraft(@PathVariable Long id,
                            @Valid @ModelAttribute("draft") EmailDraftForm draft,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // On réaffiche la fiche avec les erreurs, sans perdre le texte saisi
            Prospect prospect = find(id);
            model.addAttribute("prospect", prospect);
            model.addAttribute("trancheLibelle", TrancheEffectif.libelle(prospect.getTrancheEffectif()));
            return "prospects/detail";
        }
        analysisService.saveDraft(id, draft.getSubject(), draft.getBody());
        redirectAttributes.addFlashAttribute("successMessage", "Brouillon enregistré.");
        return "redirect:/prospects/" + id;
    }

    private Prospect find(Long id) {
        return prospectRepository.findById(id).orElseThrow(() -> new ProspectNotFoundException(id));
    }
}
