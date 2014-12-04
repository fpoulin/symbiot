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
import la.alsocan.symbiot.access.DriverDao;
import la.alsocan.symbiot.access.OutputDao;
import la.alsocan.symbiot.access.StreamDao;
import la.alsocan.symbiot.api.to.ErrorResponseTo;
import la.alsocan.symbiot.api.to.drivers.DriverTo;
import la.alsocan.symbiot.api.to.outputs.OutputTo;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/outputs")
public class OutputResource {

	private final DriverDao driverDao;
	private final OutputDao outputDao;
	private final StreamDao streamDao;

	public OutputResource(DriverDao driverDao, OutputDao outputDao, StreamDao streamDao) {
		this.driverDao = driverDao;
		this.outputDao = outputDao;
		this.streamDao = streamDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@Context UriInfo info, OutputTo outputTo) {
		
		// make sure driver and input definition exist
		DriverTo driver = driverDao.findById(outputTo.getDriverId());
		if (driver == null) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not find driver '" 
						  + outputTo.getDriverId() + "'")).build();
		}
		if (driver.getOutputDefinition(outputTo.getOutputDefinitionId()) == null) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not find output definition '" 
						  + outputTo.getOutputDefinitionId() + "' for driver '" 
						  + outputTo.getDriverId()+"'")).build();
		}
		
		// insert output
		int id = outputDao.insert(outputTo);
		
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
		return Response.ok(outputDao.findAll()).build();
	}
	
	@GET
	@Path(value = "{outputId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("outputId") int outputId) {
		
		OutputTo outputTo = outputDao.findById(outputId);
		if (outputTo == null) {
			return Response.status(404)	.build();
		}
		return Response.ok(outputTo).build();
	}
	
	@PUT
	@Path(value = "{outputId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response put(@PathParam("outputId") int outputId, OutputTo newTo) {
		
		OutputTo outputTo = outputDao.findById(outputId);
		if (outputTo == null) {
			return Response.status(404)	.build();
		}
		
		outputDao.update(outputId, newTo);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path(value = "{outputId}")
	public Response delete(@PathParam("outputId") int outputId) {
		
		OutputTo outputTo = outputDao.findById(outputId);
		if (outputTo == null) {
			return Response.status(404)	.build();
		}
		
		int count = streamDao.countByInput(outputId);
		if (count > 0) {
			return Response.status(422)
				.entity(new ErrorResponseTo("The output is currently used in streams"))
				.build();
		}
		outputDao.delete(outputId);
		return Response.noContent().build();
	}
}
