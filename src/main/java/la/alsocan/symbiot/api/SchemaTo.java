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
package la.alsocan.symbiot.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class SchemaTo {

	@JsonProperty
	private int id;
	
	@JsonProperty
	private DateTime creationDate;
	
	@JsonProperty
	private DateTime lastModificationDate;
	
	@JsonProperty
	private JsonNode schemaNode;

	public SchemaTo() {
	}

	public SchemaTo(int id, DateTime creationDate, DateTime lastModificationDate, JsonNode schemaNode) {
		this.id = id;
		this.creationDate = creationDate;
		this.lastModificationDate = lastModificationDate;
		this.schemaNode = schemaNode;
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

	public JsonNode getSchemaNode() {
		return schemaNode;
	}

	public void setSchemaNode(JsonNode schemaNode) {
		this.schemaNode = schemaNode;
	}
}
