package com.ftc.containerView.infra.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    /**
     * Remove completamente qualquer HTML/JavaScript
     * Use para campos que NUNCA devem ter HTML (ex: nome, CPF, email)
     */
    public String sanitizePlainText(String input) {
        if (input == null) return null;

        // Remove todo HTML e mantém apenas texto
        String cleaned = Jsoup.clean(input, Safelist.none());

        // Remove caracteres de controle e normaliza espaços
        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        return cleaned;
    }

    /**
     * Permite HTML básico seguro (negrito, itálico, links)
     * Use para campos de descrição ou comentários
     */
    public String sanitizeBasicHtml(String input) {
        if (input == null) return null;

        // Permite apenas tags HTML seguras
        Safelist safelist = Safelist.basic()
                .addAttributes("a", "href", "target")
                .addProtocols("a", "href", "http", "https")
                .addAttributes("a", "rel"); // Para noopener noreferrer

        String cleaned = Jsoup.clean(input, safelist);

        // Força links externos a abrir em nova aba com segurança
        cleaned = cleaned.replaceAll(
                "<a href=\"(https?://[^\"]+)\"",
                "<a href=\"$1\" target=\"_blank\" rel=\"noopener noreferrer\""
        );

        return cleaned;
    }

    /**
     * Sanitiza para uso seguro em atributos HTML
     * Use quando o dado será inserido em atributos HTML no frontend
     */
    public String sanitizeForHtmlAttribute(String input) {
        if (input == null) return null;
        return Encode.forHtmlAttribute(input);
    }

    /**
     * Sanitiza para uso seguro em JavaScript
     * Use quando o dado será usado em contexto JavaScript no frontend
     */
    public String sanitizeForJavaScript(String input) {
        if (input == null) return null;
        return Encode.forJavaScript(input);
    }

    /**
     * Sanitiza para uso seguro em URLs
     * Use para parâmetros de URL
     */
    public String sanitizeForUrl(String input) {
        if (input == null) return null;
        return Encode.forUriComponent(input);
    }

    /**
     * Valida e sanitiza CEP
     */
    public String sanitizeCep(String cep) {
        if (cep == null) return null;
        // Remove tudo que não é número
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Valida e sanitiza CPF/CNPJ
     */
    public String sanitizeDocument(String document) {
        if (document == null) return null;
        // Remove tudo que não é número
        return document.replaceAll("[^0-9]", "");
    }

    /**
     * Valida e sanitiza números de telefone
     */
    public String sanitizePhone(String phone) {
        if (phone == null) return null;
        // Mantém apenas números e o símbolo +
        return phone.replaceAll("[^0-9+]", "");
    }

    /**
     * Limita o tamanho da string para prevenir ataques de buffer
     */
    public String truncate(String input, int maxLength) {
        if (input == null) return null;
        if (input.length() <= maxLength) return input;
        return input.substring(0, maxLength);
    }

}
