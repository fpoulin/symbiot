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
package la.alsocan.jsonshapeshifterserver.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import la.alsocan.jsonshapeshifter.schemas.Schema;
import la.alsocan.jsonshapeshifter.schemas.UnsupportedJsonSchemaException;
import la.alsocan.jsonshapeshifterserver.api.ErrorResponseTo;
import la.alsocan.jsonshapeshifterserver.api.SchemaTo;
import la.alsocan.jsonshapeshifterserver.jdbi.SchemaDao;
import la.alsocan.jsonshapeshifterserver.jdbi.TransformationDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/schemas")
public class SchemaResource {

	private final SchemaDao schemaDao;
	private final TransformationDao transformationDao;

	public SchemaResource(SchemaDao schemaDao, TransformationDao transformationDao) {
		this.schemaDao = schemaDao;
		this.transformationDao = transformationDao;
	}
		
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@Context UriInfo info, String schema) {
		
		// read node
		ObjectMapper om = new ObjectMapper();
		JsonNode node;
		try {
			node = om.readTree(schema);
		} catch (IOException ex) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not read Json tree: " + ex.getMessage()))
				.build();
		}
		
		// parse schema
		try {
			Schema.buildSchema(node);
		} catch (UnsupportedJsonSchemaException ex) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Unsupported schema: " + ex.getMessage()))
				.build();
		}
		
		// store schema
		int id = schemaDao.insert(node.toString());
		
		// build response
		URI absoluteUri = info.getBaseUriBuilder()
				  .path(this.getClass())
				  .path(this.getClass(), "get")
				  .build(id);
		return Response.created(absoluteUri).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		return Response.ok(schemaDao.findAll()).build();
	}
	
	@GET
	@Path(value = "{schemaId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("schemaId") int schemaId) {
		
		SchemaTo schemaTo = schemaDao.findById(schemaId);
		if (schemaTo == null) {
			return Response.status(404)	.build();
		}
		
		return Response.ok(schemaTo).build();
	}
	
	@PUT
	@Path(value = "{schemaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response put(@PathParam("schemaId") int schemaId, String schema) {
		
		SchemaTo schemaTo = schemaDao.findById(schemaId);
		if (schemaTo == null) {
			return Response.status(404)	.build();
		}
		
		int count = transformationDao.countBySchema(schemaId);
		if (count > 0) {
			return Response.status(422)
				.entity(new ErrorResponseTo("The schema is currently used in transformations"))
				.build();
		}
		
		// read node
		ObjectMapper om = new ObjectMapper();
		JsonNode node;
		try {
			node = om.readTree(schema);
		} catch (IOException ex) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not read Json tree: " + ex.getMessage()))
				.build();
		}
		
		// parse schema
		try {
			Schema.buildSchema(node);
		} catch (UnsupportedJsonSchemaException ex) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Unsupported schema: " + ex.getMessage()))
				.build();
		}
		
		// store updated schema
		schemaDao.update(schemaId, node.toString());
		return Response.noContent().build();
	}
	
	@DELETE
	@Path(value = "{schemaId}")
	public Response delete(@PathParam("schemaId") int schemaId) {
		
		SchemaTo schemaTo = schemaDao.findById(schemaId);
		if (schemaTo == null) {
			return Response.status(404)	.build();
		}
		
		int count = transformationDao.countBySchema(schemaId);
		if (count > 0) {
			return Response.status(422)
				.entity(new ErrorResponseTo("The schema is currently used in transformations"))
				.build();
		}
		
		schemaDao.delete(schemaId);
		return Response.noContent().build();
	}
}
