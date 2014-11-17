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
import la.alsocan.jsonshapeshifterserver.api.TransformationTo;
import la.alsocan.jsonshapeshifterserver.jdbi.TransformationDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/transformations")
public class TransformationResource {
	
	private final TransformationDao transformationDao;

	public TransformationResource(TransformationDao transformationDao) {
		this.transformationDao = transformationDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@Context UriInfo info) {
		
		// store transformation
		int id = transformationDao.insert();
		
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
		return Response.ok(transformationDao.findAll()).build();
	}
	
	@GET
	@Path(value = "{transformationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("transformationId") int transformationId) {
		
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404)	.build();
		}
		
		return Response.ok(transformationTo).build();
	}
	
	@PUT
	@Path(value = "{transformationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response put(@PathParam("transformationId") int transformationId) {
		
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404)	.build();
		}
		
		// store updated transformation
		transformationDao.update(transformationId);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path(value = "{transformationId}")
	public Response delete(@PathParam("transformationId") int transformationId) {
		
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404)	.build();
		}
		
		transformationDao.delete(transformationId);
		return Response.noContent().build();
	}
}