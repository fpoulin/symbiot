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
package la.alsocan.jsonshapeshifterserver.jdbi;

import java.util.List;
import java.util.Map;
import la.alsocan.jsonshapeshifterserver.api.BindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.MissingBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.StringConstantBindingTo;
import la.alsocan.jsonshapeshifterserver.api.bindings.StringNodeBindingTo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.skife.jdbi.v2.BaseResultSetMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

/**
 * Dao for bindings, using a single table inheritance strategy (to keep it simple).
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class BindingDao {
	
	public static final String TABLE_NAME = "bindings";
	public static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "transformationId INTEGER NOT NULL, "
			  + "type VARCHAR(32) NOT NULL, "
			  + "targetNode VARCHAR(256) NOT NULL, "
			  + "sourceNode VARCHAR(256), "
			  + "stringConstant VARCHAR(512), "
			  + "CONSTRAINT binding_key PRIMARY KEY (id),"
			  + "CONSTRAINT transformation_fk FOREIGN KEY (transformationId) REFERENCES "+TransformationDao.TABLE_NAME+" (id))";
	
	private final DBI jdbi;
	
	public BindingDao(DBI jdbi) {
		this.jdbi = jdbi;
	}
	
	public int insert(BindingTo bindingTo, int transformationId) {
		int id;
		try (Handle h = jdbi.open()) {
			id = -1;
			// FIXME: improve this
			if (bindingTo instanceof MissingBindingTo) {
				id = h.createStatement("INSERT INTO " + TABLE_NAME
						  + " (lastModificationDate, transformationId, type, targetNode) "
						  + "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode)")
						  .bind("transformationId", transformationId)
						  .bind("type", "missing")
						  .bind("targetNode", bindingTo.getTargetNode())
						  .executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						  .first();
			} else if (bindingTo instanceof StringConstantBindingTo) {
				id = h.createStatement("INSERT INTO " + TABLE_NAME
						  + " (lastModificationDate, transformationId, type, targetNode, stringConstant) "
						  + "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :stringConstant)")
						  .bind("transformationId", transformationId)
						  .bind("type", "stringConstant")
						  .bind("targetNode", bindingTo.getTargetNode())
						  .bind("stringConstant", ((StringConstantBindingTo)bindingTo).getConstant())
						  .executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						  .first();
			} else if (bindingTo instanceof StringNodeBindingTo) {
				id = h.createStatement("INSERT INTO " + TABLE_NAME
						  + " (lastModificationDate, transformationId, type, targetNode, sourceNode) "
						  + "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :sourceNode)")
						  .bind("transformationId", transformationId)
						  .bind("type", "stringNode")
						  .bind("targetNode", bindingTo.getTargetNode())
						  .bind("sourceNode", ((StringNodeBindingTo)bindingTo).getSourceNode())
						  .executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						  .first();
			}
		}
		return id;
	}
	
	public List<BindingTo> findAll(int transformationId) {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME + " WHERE transformationId = :transformationId")
					  .bind("transformationId", transformationId)
					  .map(new BindingMapper())
					  .list();
		}
	}
	
	public BindingTo findById(int id, int transformationId) {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME + " WHERE transformationId = :transformationId AND id = :id")
					  .bind("transformationId", transformationId)
					  .bind("id", id)
					  .map(new BindingMapper())
					  .first();
		}
	}
	
	public void update(int id, int transformationId, BindingTo bindingTo) {
		try(Handle h = jdbi.open()){
			// do the job
		}
	}
	
	private class BindingMapper extends BaseResultSetMapper<BindingTo> {
		@Override
		protected BindingTo mapInternal(int index, Map<String, Object> row) {
			
			String type = (String)row.get("type");
			
			// specific fields
			BindingTo to;
			switch(type) {
				case "stringConstant":
					to = new StringConstantBindingTo();
					((StringConstantBindingTo)to).setConstant((String)row.get("stringConstant"));
					break;
				case "stringNode":
					to = new StringNodeBindingTo();
					((StringNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case "missing":
				default:
					to = new MissingBindingTo();
					break;
			}
			
			// common fields
			to.setId((Integer)row.get("id"));
			to.setLastModificationDate(new DateTime(row.get("lastModificationDate"), DateTimeZone.UTC));
			to.setTargetNode((String)row.get("targetNode"));
			
			return to;
		}
	}
}
