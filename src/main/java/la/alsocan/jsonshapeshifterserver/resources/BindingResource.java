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

import java.net.URI;
import java.util.List;
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
import la.alsocan.jsonshapeshifter.Transformation;
import la.alsocan.jsonshapeshifter.bindings.Binding;
import la.alsocan.jsonshapeshifter.bindings.IllegalBindingException;
import la.alsocan.jsonshapeshifter.schemas.SchemaNode;
import la.alsocan.jsonshapeshifterserver.api.BindingTo;
import la.alsocan.jsonshapeshifterserver.api.ErrorResponseTo;
import la.alsocan.jsonshapeshifterserver.api.TransformationTo;
import la.alsocan.jsonshapeshifterserver.core.TransformationBuilder;
import la.alsocan.jsonshapeshifterserver.jdbi.BindingDao;
import la.alsocan.jsonshapeshifterserver.jdbi.SchemaDao;
import la.alsocan.jsonshapeshifterserver.jdbi.TransformationDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/transformations/{transformationId}/bindings")
public class BindingResource {
	
	private final BindingDao bindingDao;
	private final SchemaDao schemaDao;
	private final TransformationDao transformationDao;

	public BindingResource(
			  BindingDao bindingDao,
			  SchemaDao schemaDao,
			  TransformationDao transformationDao) {
		
		this.bindingDao = bindingDao;
		this.schemaDao = schemaDao;
		this.transformationDao = transformationDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId, 
			  BindingTo to) {
		
		int count = bindingDao.countByTargetNode(to.getTargetNode(), transformationId);
		if (count > 0) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("A binding for '"+to.getTargetNode()+"' already exists"))
				.build();
		}
		
		// lookup and build transformation
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404).build();
		}
		List<BindingTo> bindings = bindingDao.findAll(transformationId);
		Transformation t = TransformationBuilder.build(transformationTo, schemaDao, bindings);
		
		// check target node
		SchemaNode targetNode = t.getTarget().at(to.getTargetNode());
		if (targetNode == null) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Could not find node '"+to.getTargetNode()+"' in target schema"))
				.build();
		}
		
		// build and apply binding
		try {
			Binding binding = to.build(t);
			t.bind(targetNode, binding);
		} catch (IllegalBindingException ex) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Cannot add binding, reason: " + ex.getMessage()))
				.build();
		}
		
		// store binding
		int id = bindingDao.insert(to, transformationTo.getId());
		
		// build response
		URI absoluteUri = info.getBaseUriBuilder()
			.path(this.getClass())
			.path(this.getClass(), "get")
			.build(transformationTo.getId(), id);
		return Response.created(absoluteUri).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId) {
		
		return Response.ok(bindingDao.findAll(transformationId)).build();
	}
	
	@GET
	@Path(value = "{bindingId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId, 
			  @PathParam("bindingId") int bindingId) {
		
		BindingTo to = bindingDao.findById(bindingId, transformationId);
		if (to == null) {
			return Response.status(404).build();
		}
		return Response.ok(to).build();
	}
	
	@PUT
	@Path(value = "{bindingId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId, 
			  @PathParam("bindingId") int bindingId,
			  BindingTo newTo) {
		
		// do some validation (check that target node is not different)
		BindingTo current = bindingDao.findById(bindingId, transformationId);
		if (current == null) {
			return Response.status(404).build();
		} else if (newTo.getTargetNode() != null && !current.getTargetNode().equals(newTo.getTargetNode())) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Expected a binding for node '"+current.getTargetNode()+"'"))
				.build();
		}
		
		// lookup and build transformation
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404).build();
		}
		List<BindingTo> bindings = bindingDao.findAll(transformationId);
		Transformation t = TransformationBuilder.build(transformationTo, schemaDao, bindings);
		
		// lookup target node (should not fail, unless the target schema got updated)
		SchemaNode targetNode = t.getTarget().at(current.getTargetNode());
		if (targetNode == null) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Could not find node '"+current.getTargetNode()+"' in target schema"))
				.build();
		}
		
		// build and apply binding
		try {
			Binding binding = newTo.build(t);
			t.bind(targetNode, binding);
		} catch (IllegalBindingException ex) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Cannot add binding, reason: " + ex.getMessage()))
				.build();
		}
		
		// update binding
		bindingDao.update(bindingId, transformationTo.getId(), newTo);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path(value = "{bindingId}")
	public Response delete(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId, 
			  @PathParam("bindingId") int bindingId) {
		
		BindingTo to = bindingDao.findById(bindingId, transformationId);
		if (to == null) {
			return Response.status(404).build();
		}
		
		bindingDao.delete(bindingId, transformationId);
		return Response.noContent().build();
	}
}
