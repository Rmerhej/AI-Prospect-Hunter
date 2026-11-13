package fr.prospecthunter.aiprospecthunter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Brouillon d'e-mail modifié par l'utilisateur sur la fiche prospect.
 */
public class EmailDraftForm {

    @NotBlank(message = "L'objet est obligatoire")
    @Size(max = 200, message = "200 caractères maximum")
    private String subject;

    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 3800, message = "3800 caractères maximum")
    private String body;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
