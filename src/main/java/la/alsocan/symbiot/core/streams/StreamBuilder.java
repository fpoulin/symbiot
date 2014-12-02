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
package la.alsocan.symbiot.core.streams;

import java.util.List;
import la.alsocan.jsonshapeshifter.Transformation;
import la.alsocan.jsonshapeshifter.schemas.Schema;
import la.alsocan.symbiot.api.to.BindingTo;
import la.alsocan.symbiot.api.to.SchemaTo;
import la.alsocan.symbiot.api.to.StreamTo;
import la.alsocan.symbiot.jdbi.SchemaDao;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class StreamBuilder {
	
	public static Stream build(
			  StreamTo to,
			  SchemaDao schemaDao, 
			  List<BindingTo> bindings) {
	
		SchemaTo sourceSchemaTo = schemaDao.findById(to.getSourceSchemaId());
		Schema sourceSchema = Schema.buildSchema(sourceSchemaTo.getSchemaNode());
		
		SchemaTo targetSchemaTo = schemaDao.findById(to.getTargetSchemaId());
		Schema targetchema = Schema.buildSchema(targetSchemaTo.getSchemaNode());
		
		Transformation t  = new Transformation(sourceSchema, targetchema);
		Stream s = new Stream(t);
		
		bindings.stream().forEach((binding) -> {
			t.bind(t.getTarget().at(binding.getTargetNode()), binding.build(s));
		});
		
		return s;
	}
}
