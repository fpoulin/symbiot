/*
 * The MIT License
 *
 * Copyright 2014 Florian Poulin - https://github.com/fpoulin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package la.alsocan.symbiot.api.to.bindings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import la.alsocan.jsonshapeshifter.bindings.Binding;
import la.alsocan.jsonshapeshifter.bindings.IllegalBindingException;
import la.alsocan.jsonshapeshifter.bindings.StringHandlebarsBinding;
import la.alsocan.symbiot.api.to.BindingTo;
import la.alsocan.symbiot.core.streams.Stream;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class StringHandlebarsBindingTo extends BindingTo {

	public static final String TYPE = "stringHandlebars";
	
	@JsonProperty
	private String template;
	
	@JsonProperty
	private Map<String, BindingTo> parameters;

	public StringHandlebarsBindingTo() {
		throw new UnsupportedOperationException("Handlebars bindings will come later...");
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Map<String, BindingTo> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, BindingTo> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Binding build(Stream s) {
		
		try {
			Map<String, Binding> resolvedParams = new TreeMap<>();
			for (String param : parameters.keySet()) {
				resolvedParams.put(param, parameters.get(param).build(s));
			}
			return new StringHandlebarsBinding(template, resolvedParams);
		} catch (IOException ex) {
			throw new IllegalBindingException();
		}
	}
}
