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
import la.alsocan.jsonshapeshifter.Transformation;
import la.alsocan.jsonshapeshifter.schemas.ENodeType;
import la.alsocan.jsonshapeshifter.schemas.SchemaNode;
import la.alsocan.jsonshapeshifterserver.api.BindingTo;
import la.alsocan.jsonshapeshifterserver.api.Link;
import la.alsocan.jsonshapeshifterserver.api.NextBindingTo;
import la.alsocan.jsonshapeshifterserver.api.SourceNodeTo;
import la.alsocan.jsonshapeshifterserver.api.TransformationTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.ArrayConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.ArrayNodeBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.BooleanConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.BooleanNodeBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.IntegerConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.IntegerNodeBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.NumberConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.NumberNodeBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.StringConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.StringHandlebarsBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.StringNodeBindingTo;
import la.alsocan.jsonshapeshifterserver.core.TransformationBuilder;
import la.alsocan.jsonshapeshifterserver.jdbi.BindingDao;
import la.alsocan.jsonshapeshifterserver.jdbi.SchemaDao;
import la.alsocan.jsonshapeshifterserver.jdbi.TransformationDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@Path("/transformations")
public class TransformationResource {
	
	private final BindingDao bindingDao;
	private final SchemaDao schemaDao;
	private final TransformationDao transformationDao;

	public TransformationResource(
			  BindingDao bindingDao,
			  SchemaDao schemaDao,
			  TransformationDao transformationDao) {
		
		this.bindingDao = bindingDao;
		this.schemaDao = schemaDao;
		this.transformationDao = transformationDao;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(@Context UriInfo info, TransformationTo to) {
		
		// count total bindings to be defined
		List<BindingTo> bindings = bindingDao.findAll(to.getId());
		Transformation t = TransformationBuilder.build(to, schemaDao, bindings);
		Iterator<SchemaNode> it = t.toBind();
		int count = 0;
		while(it.hasNext()) {
			it.next();
			count ++;
		}
		
		// store transformation
		int id = transformationDao.insert(to.getSourceSchemaId(), to.getTargetSchemaId(), count);
		
		// build response
		URI absoluteUri = info.getBaseUriBuilder()
			.path(this.getClass())
			.path(this.getClass(), "get")
			.build(id);
		return Response.created(absoluteUri).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll(@Context UriInfo info, @QueryParam(value = "schemaId") Integer schemaId) {
		List<TransformationTo> tos;
		if (schemaId != null) {
			tos = transformationDao.findBySchema(schemaId);
		} else {
			tos = transformationDao.findAll();
		}
		tos.stream().forEach((to) -> {
			resolveTo(info, to);
		});
		return Response.ok(tos).build();
	}
	
	@GET
	@Path(value = "{transformationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context UriInfo info, @PathParam("transformationId") int transformationId) {
		
		TransformationTo transformationTo = transformationDao.findById(transformationId);
		if (transformationTo == null) {
			return Response.status(404)	.build();
		}
		return Response.ok(resolveTo(info, transformationTo)).build();
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
	
	private TransformationTo resolveTo(UriInfo info, TransformationTo to) {

		List<BindingTo> bindings = bindingDao.findAll(to.getId());
		Transformation t = TransformationBuilder.build(to, schemaDao, bindings);
		
		// add current binding info
		bindings.stream().forEach((binding) -> {
			to.addBinding(binding);
		});
		
		// calculate next binding info
		Iterator<SchemaNode> it = t.toBind();
		if (it.hasNext()) {
			SchemaNode node = it.next();
			NextBindingTo nextBindingTo = new NextBindingTo(node.getSchemaPointer(), node.getType().toString());
			legalBindingTypesFor(node.getType()).stream().forEach((type) -> {
				nextBindingTo.addLegalBindingType(type);
			});
			t.legalNodesFor(node).stream().forEach((legalSourceNode) -> {
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
		.addLink(new Link("sourceSchema",
			info.getBaseUriBuilder()
			.path(SchemaResource.class)
			.path(SchemaResource.class, "get")
			.build(to.getSourceSchemaId())
			.toString()))
		.addLink(new Link("targetSchema",
			info.getBaseUriBuilder()
			.path(SchemaResource.class)
			.path(SchemaResource.class, "get")
			.build(to.getTargetSchemaId())
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
		}
		return types;
	}
}
