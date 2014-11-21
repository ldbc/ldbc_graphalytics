package org.tudelft.graphalytics.reporting;

import java.io.File;
import java.util.Locale;

import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

public class TemplateEngine {
	
	org.thymeleaf.TemplateEngine thymeleafEngine;
	Context ctx;
	
	public TemplateEngine(String templateFolder) {
		FileTemplateResolver templateResolver = new FileTemplateResolver();
		templateResolver.setPrefix(templateFolder + File.separator);
		templateResolver.setSuffix(".html");
		
		thymeleafEngine = new org.thymeleaf.TemplateEngine();
		thymeleafEngine.setTemplateResolver(templateResolver);
		
		ctx = new Context(Locale.ENGLISH);
	}
	
	public String processTemplate(String templateName) {
		return thymeleafEngine.process(templateName, ctx);
	}
	
	public void putVariable(String key, Object value) {
		ctx.setVariable(key, value);
	}
	
}
