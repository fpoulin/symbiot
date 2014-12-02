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
package la.alsocan.symbiot.api.to;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class NextBindingTo {
	
	@JsonProperty
	private String targetNode;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private Set<String> legalBindingTypes;
	
	@JsonProperty
	private Set<SourceNodeTo> legalSourceNodes;

	public NextBindingTo() {
		this.legalBindingTypes = new TreeSet<>();
		this.legalSourceNodes = new TreeSet<>();
	}

	public NextBindingTo(String targetNode, String type) {
		this();
		this.targetNode = targetNode;
		this.type = type;
	}
	
	public NextBindingTo addLegalSourceNode(SourceNodeTo to) {
		legalSourceNodes.add(to);
		return this;
	}
	
	public NextBindingTo addLegalBindingType(String bindingType) {
		legalBindingTypes.add(bindingType);
		return this;
	}

	public String getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(String targetNode) {
		this.targetNode = targetNode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
