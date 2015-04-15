/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.reporting.html;

import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.util.Locale;

/**
 * Wrapper class for the Thymeleaf TemplateEngine, with convenience methods to set variables and to process pages.
 *
 * @author Tim Hegeman
 */
public class TemplateEngine {

	private org.thymeleaf.TemplateEngine thymeleafEngine;
	private Context ctx;

	/**
	 * Initialize the Thymeleaf template engine by setting a template resolver based on the template path,
	 * setting the file extension to HTML, etc.
	 *
	 * @param templateFolder directory containing the HTML report template
	 */
	public TemplateEngine(String templateFolder) {
		FileTemplateResolver templateResolver = new FileTemplateResolver();
		templateResolver.setPrefix(templateFolder + File.separator);
		templateResolver.setSuffix(".html");

		thymeleafEngine = new org.thymeleaf.TemplateEngine();
		thymeleafEngine.setTemplateResolver(templateResolver);

		ctx = new Context(Locale.ENGLISH);
	}

	/**
	 * @param templateName the name of a template (filename relative to template folder, excluding the extension)
	 * @return the generated HTML page
	 */
	public String processTemplate(String templateName) {
		return thymeleafEngine.process(templateName, ctx);
	}

	/**
	 * @param key   the name of the variable to be set in the Thymeleaf engine
	 * @param value the new value of the variable
	 */
	public void putVariable(String key, Object value) {
		ctx.setVariable(key, value);
	}

}
