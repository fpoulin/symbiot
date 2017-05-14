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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import la.alsocan.jsonshapeshifter.bindings.Binding;
import la.alsocan.jsonshapeshifter.bindings.IllegalBindingException;
import la.alsocan.symbiot.core.streams.Stream;
import org.joda.time.DateTime;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes({
	@Type(value = ArrayConstantBindingTo.class, name = ArrayConstantBindingTo.TYPE),
	@Type(value = ArrayNodeBindingTo.class, name = ArrayNodeBindingTo.TYPE),
	@Type(value = BooleanConstantBindingTo.class, name = BooleanConstantBindingTo.TYPE),
	@Type(value = BooleanNodeBindingTo.class, name = BooleanNodeBindingTo.TYPE),
	@Type(value = IntegerConstantBindingTo.class, name = IntegerConstantBindingTo.TYPE),
	@Type(value = IntegerNodeBindingTo.class, name = IntegerNodeBindingTo.TYPE),
	@Type(value = NumberConstantBindingTo.class, name = NumberConstantBindingTo.TYPE),
	@Type(value = NumberNodeBindingTo.class, name = NumberNodeBindingTo.TYPE),
	@Type(value = StringConstantBindingTo.class, name = StringConstantBindingTo.TYPE),
	@Type(value = StringHandlebarsBindingTo.class, name = StringHandlebarsBindingTo.TYPE),
	@Type(value = StringNodeBindingTo.class, name = StringNodeBindingTo.TYPE)})
public abstract class BindingTo {
	
	@JsonProperty
	private int id;
	
	@JsonProperty
	private DateTime lastModificationDate;
	
	@JsonProperty
	private String targetNode;
	
	public BindingTo() {
	}

	public abstract String getType();
	
	/**
	 * Build a {@link Binding} object
	 * @param s The stream for which the binding must be built
	 * @return A binding
	 * @throws IllegalBindingException The binding could not be built
	 */
	public abstract Binding<?> build(Stream s) throws IllegalBindingException;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DateTime getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(DateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public String getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(String targetNode) {
		this.targetNode = targetNode;
	}
}
