package com.data.anac_api.service.impl;

import com.data.anac_api.service.TemplateService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final ResourceLoader resourceLoader;

    public TemplateServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String loadTemplate(String templateName) {
        Resource resource = resourceLoader.getResource("classpath:templates/emails/" + templateName);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec du chargement du template: " + templateName, e);
        }
    }

    public String populateTemplate(String templateContent, Map<String, String> variables) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

}
