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
import javax.ws.rs.Consumes;
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
import la.alsocan.jsonshapeshifterserver.api.BindingTo;
import la.alsocan.jsonshapeshifterserver.api.TransformationTo;
import la.alsocan.jsonshapeshifterserver.jdbi.BindingDao;
import la.alsocan.jsonshapeshifterserver.jdbi.TransformationDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/transformations/{transformationId}/bindings")
public class BindingResource {
	
	private final BindingDao bindingDao;
	private final TransformationDao transformationDao;

	public BindingResource(BindingDao bindingDao, TransformationDao transformationDao) {
		this.bindingDao = bindingDao;
		this.transformationDao = transformationDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(
			  @Context UriInfo info, 
			  @PathParam("transformationId") int transformationId, 
			  BindingTo to) {
		
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404).build();
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
		
		BindingTo to = bindingDao.findById(bindingId, transformationId);
		if (to == null) {
			return Response.status(404).build();
		}
		bindingDao.update(bindingId, transformationId, newTo);
		return Response.noContent().build();
	}
}
