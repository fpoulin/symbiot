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
package la.alsocan.symbiot.api.to.drivers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class DriverTo {
	
	@JsonProperty
	private String id;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private String description;
	
	@JsonProperty
	private String version;
	
	@JsonProperty
	private Set<InputDefinitionTo> inputDefinitions;
	
	@JsonProperty
	private Set<OutputDefinitionTo> outputDefinitions;
	
	@JsonIgnore
	private Map<String, InputDefinitionTo> inputDefinitionsIndex;
	
	@JsonIgnore
	private Map<String, OutputDefinitionTo> outputDefinitionsIndex;

	public InputDefinitionTo getInputDefinition(String inputId) {
	
		if (inputDefinitionsIndex == null) {
			inputDefinitionsIndex = new TreeMap<>();
			inputDefinitions.stream().forEach((to) -> {
				inputDefinitionsIndex.put(to.getId(), to);
			});
		}
		return inputDefinitionsIndex.get(inputId);
	}
	
	public OutputDefinitionTo getOutputDefinition(String outputId) {
	
		if (outputDefinitionsIndex == null) {
			outputDefinitionsIndex = new TreeMap<>();
			outputDefinitions.stream().forEach((to) -> {
				outputDefinitionsIndex.put(to.getId(), to);
			});
		}
		return outputDefinitionsIndex.get(outputId);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Set<InputDefinitionTo> getInputDefinitions() {
		return inputDefinitions;
	}

	public void setInputDefinitions(Set<InputDefinitionTo> inputDefinitions) {
		this.inputDefinitions = inputDefinitions;
	}

	public Set<OutputDefinitionTo> getOutputDefinitions() {
		return outputDefinitions;
	}

	public void setOutputDefinitions(Set<OutputDefinitionTo> outputDefinitions) {
		this.outputDefinitions = outputDefinitions;
	}
}
