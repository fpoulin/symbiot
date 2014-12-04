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

import la.alsocan.symbiot.api.to.bindings.BindingTo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import org.joda.time.DateTime;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class StreamTo {

	@JsonProperty
	private int id;
	
	@JsonProperty
	private DateTime creationDate;
	
	@JsonProperty
	private DateTime lastModificationDate;
	
	@JsonProperty
	private int inputId;
	
	@JsonProperty
	private int outputId;
	
	@JsonProperty
	private final List<BindingTo> bindings;
	
	@JsonProperty
	private NextBindingTo nextToBind;
	
	@JsonProperty
	private int totalBindings;
	
	@JsonProperty
	private int remainingBindings;
	
	@JsonProperty
	private final List<Link> links;
	
	public StreamTo() {
		this.links = new LinkedList<>();
		this.bindings = new LinkedList<>();
	}

	public StreamTo addLink(Link link) {
		this.links.add(link);
		return this;
	}
	
	public StreamTo addBinding(BindingTo binding) {
		this.bindings.add(binding);
		return this;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

	public DateTime getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(DateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public int getInputId() {
		return inputId;
	}

	public void setInputId(int inputId) {
		this.inputId = inputId;
	}

	public int getOutputId() {
		return outputId;
	}

	public void setOutputId(int outputId) {
		this.outputId = outputId;
	}

	public NextBindingTo getNextToBind() {
		return nextToBind;
	}

	public void setNextToBind(NextBindingTo nextToBind) {
		this.nextToBind = nextToBind;
	}

	public int getTotalBindings() {
		return totalBindings;
	}

	public void setTotalBindings(int totalBindings) {
		this.totalBindings = totalBindings;
	}

	public int getRemainingBindings() {
		return remainingBindings;
	}

	public void setRemainingBindings(int remainingBindings) {
		this.remainingBindings = remainingBindings;
	}
}
