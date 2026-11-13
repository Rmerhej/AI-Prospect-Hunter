package fr.prospecthunter.aiprospecthunter.controller;

import fr.prospecthunter.aiprospecthunter.client.ApiUnavailableException;
import fr.prospecthunter.aiprospecthunter.dto.ImportReport;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.service.CompanyImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final CompanyImportService importService;

    public ImportController(CompanyImportService importService) {
        this.importService = importService;
    }

    @ModelAttribute("segments")
    public TargetSegment[] segments() {
        return TargetSegment.values();
    }

    @ModelAttribute("allDepartements")
    public Departement[] allDepartements() {
        return Departement.values();
    }

    /** Formulaire de recherche, et résultats si un segment est choisi. */
    @GetMapping
    public String showImport(@RequestParam(required = false) TargetSegment segment,
                             @RequestParam(required = false) List<Departement> departements,
                             @RequestParam(defaultValue = "1") int page,
                             Model model) {

        List<Departement> selected = orAll(departements);
        model.addAttribute("segment", segment);
        model.addAttribute("departements", selected);

        if (segment != null) {
            try {
                model.addAttribute("result", importService.search(segment, selected, page));
            } catch (ApiUnavailableException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return "import/search";
    }

    /** Import des entreprises cochées, puis retour sur la même page de résultats. */
    @PostMapping
    public String importSelected(@RequestParam TargetSegment segment,
                                 @RequestParam(required = false) List<Departement> departements,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) List<String> sirens,
                                 RedirectAttributes redirectAttributes) {

        List<Departement> selected = orAll(departements);

        try {
            ImportReport report = importService.importCompanies(segment, selected, page, sirens);
            if (report.imported() == 0 && report.alreadyPresent() == 0 && report.notFound() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cochez au moins une entreprise.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", report.toMessage());
            }
        } catch (ApiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        String url = UriComponentsBuilder.fromPath("/import")
                .queryParam("segment", segment.name())
                .queryParam("departements", selected.stream().map(Enum::name).toArray())
                .queryParam("page", page)
                .toUriString();
        return "redirect:" + url;
    }

    private static List<Departement> orAll(List<Departement> departements) {
        return departements == null || departements.isEmpty()
                ? List.of(Departement.values())
                : departements.stream().distinct().toList();
    }
}
