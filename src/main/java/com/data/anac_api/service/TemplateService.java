package com.data.anac_api.service;

import java.util.Map;

public interface TemplateService {

    String populateTemplate(String templateContent, Map<String, String> variables);

    String loadTemplate(String templateName);

}
