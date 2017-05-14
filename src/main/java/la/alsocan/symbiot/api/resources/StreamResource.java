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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import la.alsocan.jsonshapeshifter.schemas.ENodeType;
import la.alsocan.jsonshapeshifter.schemas.SchemaNode;
import la.alsocan.symbiot.api.to.bindings.BindingTo;
import la.alsocan.symbiot.api.to.Link;
import la.alsocan.symbiot.api.to.NextBindingTo;
import la.alsocan.symbiot.api.to.SourceNodeTo;
import la.alsocan.symbiot.api.to.StreamTo;
import la.alsocan.symbiot.api.to.bindings.ArrayConstantBindingTo;
import la.alsocan.symbiot.api.to.bindings.ArrayNodeBindingTo;
import la.alsocan.symbiot.api.to.bindings.BooleanConstantBindingTo;
import la.alsocan.symbiot.api.to.bindings.BooleanNodeBindingTo;
import la.alsocan.symbiot.api.to.bindings.IntegerConstantBindingTo;
import la.alsocan.symbiot.api.to.bindings.IntegerNodeBindingTo;
import la.alsocan.symbiot.api.to.bindings.NumberConstantBindingTo;
import la.alsocan.symbiot.api.to.bindings.NumberNodeBindingTo;
import la.alsocan.symbiot.api.to.bindings.StringConstantBindingTo;
import la.alsocan.symbiot.api.to.bindings.StringHandlebarsBindingTo;
import la.alsocan.symbiot.api.to.bindings.StringNodeBindingTo;
import la.alsocan.symbiot.core.streams.Stream;
import la.alsocan.symbiot.access.BindingDao;
import la.alsocan.symbiot.access.DriverDao;
import la.alsocan.symbiot.access.InputDao;
import la.alsocan.symbiot.access.OutputDao;
import la.alsocan.symbiot.access.StreamDao;
import la.alsocan.symbiot.api.to.ErrorResponseTo;
import la.alsocan.symbiot.api.to.inputs.InputTo;
import la.alsocan.symbiot.api.to.outputs.OutputTo;
import la.alsocan.symbiot.core.streams.StreamBuilder;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/streams")
public class StreamResource {
	
	private final BindingDao bindingDao;
	private final DriverDao driverDao;
	private final InputDao inputDao;
	private final OutputDao outputDao;
	private final StreamDao streamDao;

	public StreamResource(BindingDao bindingDao, DriverDao driverDao, InputDao inputDao, OutputDao outputDao, StreamDao streamDao) {
		this.bindingDao = bindingDao;
		this.driverDao = driverDao;
		this.inputDao = inputDao;
		this.outputDao = outputDao;
		this.streamDao = streamDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@Context UriInfo info, StreamTo to) {
		
		InputTo inputTo = inputDao.findById(to.getInputId());
		if (inputTo == null) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not find input '"+to.getInputId()+"'"))
				.build();
		}
		OutputTo outputTo = outputDao.findById(to.getOutputId());
		if (outputTo == null) {
			return Response.status(422)
				.entity(new ErrorResponseTo("Could not find output '"+to.getOutputId()+"'"))
				.build();
		}
		
		// count total bindings to be defined
		Stream s = StreamBuilder.build(to, driverDao, inputDao, outputDao, Collections.emptyList());
		Iterator<SchemaNode> it = s.getT().toBind();
		int count = 0;
		while(it.hasNext()) {
			it.next();
			count ++;
		}
		
		// store stream
		int id = streamDao.insert(to.getInputId(), to.getOutputId(), count);
		
		// build response
		URI absoluteUri = info.getBaseUriBuilder()
			.path(this.getClass())
			.path(this.getClass(), "get")
			.build(id);
		return Response.created(absoluteUri).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll(
			  @Context UriInfo info, 
			  @QueryParam(value = "inputId") Integer inputId,
			  @QueryParam(value = "outputId") Integer outputId) {
		
		List<StreamTo> tos;
		if (inputId != null && outputId != null) {
			tos = streamDao.findByInputAndOutput(inputId, outputId);
		} else if (inputId != null) {
			tos = streamDao.findByInput(inputId);
		} else if (outputId != null) {
			tos = streamDao.findByOutput(outputId);
		} else {
			tos = streamDao.findAll();
		}
		tos.stream().forEach((to) -> {
			resolveTo(info, to);
		});
		return Response.ok(tos).build();
	}
	
	@GET
	@Path(value = "{streamId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context UriInfo info, @PathParam("streamId") int streamId) {
		
		StreamTo streamTo = streamDao.findById(streamId);
		if (streamTo == null) {
			return Response.status(404)	.build();
		}
		return Response.ok(resolveTo(info, streamTo)).build();
	}
	
	@DELETE
	@Path(value = "{streamId}")
	public Response delete(@PathParam("streamId") int streamId) {
		
		StreamTo streamTo = streamDao.findById(streamId);
		if (streamTo == null) {
			return Response.status(404)	.build();
		}
		
		streamDao.delete(streamId);
		return Response.noContent().build();
	}
	
	private StreamTo resolveTo(UriInfo info, StreamTo to) {

		List<BindingTo> bindings = bindingDao.findAll(to.getId());
		Stream s = StreamBuilder.build(to, driverDao, inputDao, outputDao, bindings);
		
		// add current binding info
		bindings.stream().forEach((binding) -> {
			to.addBinding(binding);
		});
		
		// calculate next binding info
		Iterator<SchemaNode> it = s.getT().toBind();
		if (it.hasNext()) {
			SchemaNode node = it.next();
			NextBindingTo nextBindingTo = new NextBindingTo(node.getSchemaPointer(), node.getType().toString());
			legalBindingTypesFor(node.getType()).stream().forEach((type) -> {
				nextBindingTo.addLegalBindingType(type);
			});
			s.getT().legalNodesFor(node).stream().forEach((legalSourceNode) -> {
				nextBindingTo.addLegalSourceNode(new SourceNodeTo(
						legalSourceNode.getSchemaPointer(),
						legalSourceNode.getType().toString()));
			});
			to.setNextToBind(nextBindingTo);
			int remaining = 1;
			while(it.hasNext()) {
				it.next();
				remaining++;
			}
			to.setRemainingBindings(remaining);
		} else {
			to.setRemainingBindings(0);
		}
		
		// resolve hateoas links
		to.addLink(new Link("self",
			info.getBaseUriBuilder()
			.path(this.getClass())
			.path(this.getClass(), "get")
			.build(to.getId())
			.toString()))
		.addLink(new Link("input",
			info.getBaseUriBuilder()
			.path(InputResource.class)
			.path(InputResource.class, "get")
			.build(to.getInputId())
			.toString()))
		.addLink(new Link("output",
			info.getBaseUriBuilder()
			.path(OutputResource.class)
			.path(OutputResource.class, "get")
			.build(to.getOutputId())
			.toString()));
		if (to.getRemainingBindings() > 0) {
			to.addLink(new Link("nextToBind",
			info.getBaseUriBuilder()
			.path(BindingResource.class)
			.build(to.getId())
			.toString()));
		}
		
		return to;
	}
	
	private Set<String> legalBindingTypesFor(ENodeType targetNodeType) {
	
		Set<String> types = new TreeSet<>();
		switch(targetNodeType){
			case ARRAY:
				types.add(ArrayConstantBindingTo.TYPE);
				types.add(ArrayNodeBindingTo.TYPE);
				break;
			case BOOLEAN:
				types.add(BooleanConstantBindingTo.TYPE);
				types.add(BooleanNodeBindingTo.TYPE);
				break;
			case INTEGER:
				types.add(IntegerConstantBindingTo.TYPE);
				types.add(IntegerNodeBindingTo.TYPE);
				break;
			case NUMBER:
				types.add(IntegerConstantBindingTo.TYPE);
				types.add(IntegerNodeBindingTo.TYPE);
				types.add(NumberConstantBindingTo.TYPE);
				types.add(NumberNodeBindingTo.TYPE);
				break;
			case STRING:
				types.add(BooleanConstantBindingTo.TYPE);
				types.add(BooleanNodeBindingTo.TYPE);
				types.add(IntegerConstantBindingTo.TYPE);
				types.add(IntegerNodeBindingTo.TYPE);
				types.add(NumberConstantBindingTo.TYPE);
				types.add(NumberNodeBindingTo.TYPE);
				types.add(StringNodeBindingTo.TYPE);
				types.add(StringHandlebarsBindingTo.TYPE);
				types.add(StringConstantBindingTo.TYPE);
				break;
			case NULL:
			case OBJECT:
			default:
				break;
		}
		return types;
	}
}
