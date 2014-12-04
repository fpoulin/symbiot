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
package la.alsocan.symbiot.api.resources;

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
import la.alsocan.jsonshapeshifter.bindings.Binding;
import la.alsocan.jsonshapeshifter.bindings.IllegalBindingException;
import la.alsocan.jsonshapeshifter.schemas.SchemaNode;
import la.alsocan.symbiot.api.to.bindings.BindingTo;
import la.alsocan.symbiot.api.to.ErrorResponseTo;
import la.alsocan.symbiot.api.to.StreamTo;
import la.alsocan.symbiot.core.streams.Stream;
import la.alsocan.symbiot.access.BindingDao;
import la.alsocan.symbiot.access.DriverDao;
import la.alsocan.symbiot.access.InputDao;
import la.alsocan.symbiot.access.OutputDao;
import la.alsocan.symbiot.access.StreamDao;
import la.alsocan.symbiot.core.streams.StreamBuilder;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/streams/{streamId}/bindings")
public class BindingResource {
	
	private final BindingDao bindingDao;
	private final DriverDao driverDao;
	private final InputDao inputDao;
	private final OutputDao outputDao;
	private final StreamDao streamDao;

	public BindingResource(BindingDao bindingDao, DriverDao driverDao, InputDao inputDao, OutputDao outputDao, StreamDao streamDao) {
		this.bindingDao = bindingDao;
		this.driverDao = driverDao;
		this.inputDao = inputDao;
		this.outputDao = outputDao;
		this.streamDao = streamDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(
			  @Context UriInfo info, 
			  @PathParam("streamId") int streamId, 
			  BindingTo to) {
		
		int count = bindingDao.countByTargetNode(to.getTargetNode(), streamId);
		if (count > 0) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("A binding for '"+to.getTargetNode()+"' already exists"))
				.build();
		}
		
		// lookup and build stream
		StreamTo streamTo = streamDao.findById(streamId);
		if (streamTo == null) {
			return Response.status(404).build();
		}
		List<BindingTo> bindings = bindingDao.findAll(streamId);
		Stream s = StreamBuilder.build(streamTo, driverDao, inputDao, outputDao, bindings);
		
		// check target node
		SchemaNode targetNode = s.getT().getTarget().at(to.getTargetNode());
		if (targetNode == null) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Could not find node '"+to.getTargetNode()+"' in target schema"))
				.build();
		}
		
		// build and apply binding
		try {
			Binding binding = to.build(s);
			s.getT().bind(targetNode, binding);
		} catch (IllegalBindingException ex) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Cannot add binding, reason: " + ex.getMessage()))
				.build();
		}
		
		// store binding
		int id = bindingDao.insert(to, streamId);
		
		// build response
		URI absoluteUri = info.getBaseUriBuilder()
			.path(this.getClass())
			.path(this.getClass(), "get")
			.build(streamTo.getId(), id);
		return Response.created(absoluteUri).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll(
			  @Context UriInfo info, 
			  @PathParam("streamId") int streamId) {
		
		return Response.ok(bindingDao.findAll(streamId)).build();
	}
	
	@GET
	@Path(value = "{bindingId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			  @Context UriInfo info, 
			  @PathParam("streamId") int streamId, 
			  @PathParam("bindingId") int bindingId) {
		
		BindingTo to = bindingDao.findById(bindingId, streamId);
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
			  @PathParam("streamId") int streamId, 
			  @PathParam("bindingId") int bindingId,
			  BindingTo newTo) {
		
		// do some validation (check that target node is not different)
		BindingTo current = bindingDao.findById(bindingId, streamId);
		if (current == null) {
			return Response.status(404).build();
		} else if (newTo.getTargetNode() != null && !current.getTargetNode().equals(newTo.getTargetNode())) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Expected a binding for node '"+current.getTargetNode()+"'"))
				.build();
		}
		
		// lookup and build stream
		StreamTo streamTo = streamDao.findById(streamId);
		if (streamTo == null) {
			return Response.status(404).build();
		}
		List<BindingTo> bindings = bindingDao.findAll(streamId);
		Stream s = StreamBuilder.build(streamTo, driverDao, inputDao, outputDao, bindings);
		
		// lookup target node (should not fail, unless the target schema got updated)
		SchemaNode targetNode = s.getT().getTarget().at(current.getTargetNode());
		if (targetNode == null) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Could not find node '"+current.getTargetNode()+"' in target schema"))
				.build();
		}
		
		// build and apply binding
		try {
			Binding binding = newTo.build(s);
			s.getT().bind(targetNode, binding);
		} catch (IllegalBindingException ex) {
			return Response
				.status(422)
				.entity(new ErrorResponseTo("Cannot add binding, reason: " + ex.getMessage()))
				.build();
		}
		
		// update binding
		bindingDao.update(bindingId, streamTo.getId(), newTo);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path(value = "{bindingId}")
	public Response delete(
			  @Context UriInfo info, 
			  @PathParam("streamId") int streamId, 
			  @PathParam("bindingId") int bindingId) {
		
		BindingTo to = bindingDao.findById(bindingId, streamId);
		if (to == null) {
			return Response.status(404).build();
		}
		
		bindingDao.delete(bindingId, streamId);
		return Response.noContent().build();
	}
}
