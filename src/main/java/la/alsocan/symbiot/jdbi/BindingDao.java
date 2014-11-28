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
package la.alsocan.symbiot.jdbi;

import java.util.List;
import java.util.Map;
import la.alsocan.symbiot.api.BindingTo;
import la.alsocan.symbiot.api.bindings.AbstractNodeBindingTo;
import la.alsocan.symbiot.api.bindings.ArrayConstantBindingTo;
import la.alsocan.symbiot.api.bindings.ArrayNodeBindingTo;
import la.alsocan.symbiot.api.bindings.BooleanConstantBindingTo;
import la.alsocan.symbiot.api.bindings.BooleanNodeBindingTo;
import la.alsocan.symbiot.api.bindings.IntegerConstantBindingTo;
import la.alsocan.symbiot.api.bindings.IntegerNodeBindingTo;
import la.alsocan.symbiot.api.bindings.NumberConstantBindingTo;
import la.alsocan.symbiot.api.bindings.NumberNodeBindingTo;
import la.alsocan.symbiot.api.bindings.StringConstantBindingTo;
import la.alsocan.symbiot.api.bindings.StringNodeBindingTo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.skife.jdbi.v2.BaseResultSetMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

/**
 * Dao for bindings, using a single table inheritance strategy (to keep it <i>simple</i>).
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
			  + "arrayConstant INTEGER, "
			  + "booleanConstant BOOLEAN, "
			  + "integerConstant INTEGER, "
			  + "numberConstant DOUBLE PRECISION, "
			  + "stringConstant VARCHAR(512), "
			  + "CONSTRAINT binding_key PRIMARY KEY (id),"
			  + "CONSTRAINT transformation_fk FOREIGN KEY (transformationId) "
			  + " REFERENCES " + TransformationDao.TABLE_NAME + " (id) ON DELETE CASCADE)";
	
	private final DBI jdbi;
	
	public BindingDao(DBI jdbi) {
		this.jdbi = jdbi;
	}
	
	// FIXME: improve this ugly method (ex: using one über-TO which is always inserte the same way)
	public int insert(BindingTo bindingTo, int transformationId) {
		
		int id;
		try (Handle h = jdbi.open()) {
			id = -1;
			switch(bindingTo.getType()) {
				case ArrayNodeBindingTo.TYPE:
				case BooleanNodeBindingTo.TYPE:
				case IntegerNodeBindingTo.TYPE:
				case NumberNodeBindingTo.TYPE:
				case StringNodeBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, sourceNode) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :sourceNode)")
						.bind("transformationId", transformationId)
						.bind("type", bindingTo.getType())
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("sourceNode", ((AbstractNodeBindingTo)bindingTo).getSourceNode())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case ArrayConstantBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, arrayConstant) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :arrayConstant)")
						.bind("transformationId", transformationId)
						.bind("type", "arrayConstant")
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("arrayConstant", ((ArrayConstantBindingTo)bindingTo).getNbIterations())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case BooleanConstantBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, booleanConstant) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :booleanConstant)")
						.bind("transformationId", transformationId)
						.bind("type", "booleanConstant")
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("booleanConstant", ((BooleanConstantBindingTo)bindingTo).getConstant())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case IntegerConstantBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, integerConstant) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :integerConstant)")
						.bind("transformationId", transformationId)
						.bind("type", "integerConstant")
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("integerConstant", ((IntegerConstantBindingTo)bindingTo).getConstant())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case NumberConstantBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, numberConstant) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :numberConstant)")
						.bind("transformationId", transformationId)
						.bind("type", "numberConstant")
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("numberConstant", ((NumberConstantBindingTo)bindingTo).getConstant())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case StringConstantBindingTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (lastModificationDate, transformationId, type, targetNode, stringConstant) "
						+ "VALUES (CURRENT_TIMESTAMP, :transformationId, :type, :targetNode, :stringConstant)")
						.bind("transformationId", transformationId)
						.bind("type", "stringConstant")
						.bind("targetNode", bindingTo.getTargetNode())
						.bind("stringConstant", ((StringConstantBindingTo)bindingTo).getConstant())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
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
	
	public int countByTargetNode( String targetNode, int transformationId) {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT COUNT(*) FROM " + TABLE_NAME 
					  + " WHERE transformationId = :transformationId "
					  + "AND targetNode = :targetNode")
					  .bind("transformationId", transformationId)
					  .bind("targetNode", targetNode)
					  .map(IntegerMapper.FIRST)
					  .first();
		}
	}
	
	public void update(int id, int transformationId, BindingTo newTo) {
		
		try (Handle h = jdbi.open()) {
			switch(newTo.getType()) {
				case ArrayNodeBindingTo.TYPE:
				case BooleanNodeBindingTo.TYPE:
				case IntegerNodeBindingTo.TYPE:
				case NumberNodeBindingTo.TYPE:
				case StringNodeBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = :sourceNode, "
						+ "arrayConstant = NULL, "
						+ "booleanConstant = NULL, "
						+ "integerConstant = NULL, "
						+ "numberConstant = NULL, "
						+ "stringConstant = NULL "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", newTo.getType())
						.bind("sourceNode", ((AbstractNodeBindingTo)newTo).getSourceNode())
						.execute();
					break;
				case ArrayConstantBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = NULL, "
						+ "arrayConstant = :arrayConstant, "
						+ "booleanConstant = NULL, "
						+ "integerConstant = NULL, "
						+ "numberConstant = NULL, "
						+ "stringConstant = NULL "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", ArrayConstantBindingTo.TYPE)
						.bind("arrayConstant", ((ArrayConstantBindingTo)newTo).getNbIterations())
						.execute();
					break;
				case BooleanConstantBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = NULL, "
						+ "arrayConstant = NULL, "
						+ "booleanConstant = :booleanConstant, "
						+ "integerConstant = NULL, "
						+ "numberConstant = NULL, "
						+ "stringConstant = NULL "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", BooleanConstantBindingTo.TYPE)
						.bind("booleanConstant", ((BooleanConstantBindingTo)newTo).getConstant())
						.execute();
					break;
				case IntegerConstantBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = NULL, "
						+ "arrayConstant = NULL, "
						+ "booleanConstant = NULL, "
						+ "integerConstant = :integerConstant, "
						+ "numberConstant = NULL, "
						+ "stringConstant = NULL "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", IntegerConstantBindingTo.TYPE)
						.bind("integerConstant", ((IntegerConstantBindingTo)newTo).getConstant())
						.execute();
					break;
				case NumberConstantBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = NULL, "
						+ "arrayConstant = NULL, "
						+ "booleanConstant = NULL, "
						+ "integerConstant = NULL, "
						+ "numberConstant = :numberConstant, "
						+ "stringConstant = NULL "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", NumberConstantBindingTo.TYPE)
						.bind("numberConstant", ((NumberConstantBindingTo)newTo).getConstant())
						.execute();
					break;
				case StringConstantBindingTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "type = :type, "
						+ "sourceNode = NULL, "
						+ "arrayConstant = NULL, "
						+ "booleanConstant = NULL, "
						+ "integerConstant = NULL, "
						+ "numberConstant = NULL, "
						+ "stringConstant = :stringConstant "
						+ "WHERE transformationId = :transformationId AND id = :id")
						.bind("transformationId", transformationId)
						.bind("id", id)
						.bind("type", StringConstantBindingTo.TYPE)
						.bind("stringConstant", ((StringConstantBindingTo)newTo).getConstant())
						.execute();
					break;
			}
		}
	}
	
	public void delete(int id, int transformationId) {
		try(Handle h = jdbi.open()){
			h.createStatement("DELETE FROM " + TABLE_NAME + " WHERE transformationId = :transformationId AND id = :id")
					  .bind("transformationId", transformationId)
					  .bind("id", id)
					  .execute();
		}
	}
	
	private class BindingMapper extends BaseResultSetMapper<BindingTo> {
		@Override
		protected BindingTo mapInternal(int index, Map<String, Object> row) {
			
			String type = (String)row.get("type");
			
			// specific fields
			BindingTo to = null;
			switch(type) {
				case ArrayNodeBindingTo.TYPE:
					to = new ArrayNodeBindingTo();
					((ArrayNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case ArrayConstantBindingTo.TYPE:
					to = new ArrayConstantBindingTo();
					((ArrayConstantBindingTo)to).setNbIterations((int)row.get("arrayConstant"));
					break;
				case BooleanNodeBindingTo.TYPE:
					to = new BooleanNodeBindingTo();
					((BooleanNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case BooleanConstantBindingTo.TYPE:
					to = new BooleanConstantBindingTo();
					((BooleanConstantBindingTo)to).setConstant((Boolean)row.get("booleanConstant"));
					break;
				case IntegerNodeBindingTo.TYPE:
					to = new IntegerNodeBindingTo();
					((IntegerNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case IntegerConstantBindingTo.TYPE:
					to = new IntegerConstantBindingTo();
					((IntegerConstantBindingTo)to).setConstant((Integer)row.get("integerConstant"));
					break;
				case NumberNodeBindingTo.TYPE:
					to = new NumberNodeBindingTo();
					((NumberNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case NumberConstantBindingTo.TYPE:
					to = new NumberConstantBindingTo();
					((NumberConstantBindingTo)to).setConstant((Double)row.get("numberConstant"));
					break;
				case StringNodeBindingTo.TYPE:
					to = new StringNodeBindingTo();
					((StringNodeBindingTo)to).setSourceNode((String)row.get("sourceNode"));
					break;
				case StringConstantBindingTo.TYPE:
					to = new StringConstantBindingTo();
					((StringConstantBindingTo)to).setConstant((String)row.get("stringConstant"));
					break;
			}
			
			// common fields
			if (to != null) {
				to.setId((Integer)row.get("id"));
				to.setLastModificationDate(new DateTime(row.get("lastModificationDate"), DateTimeZone.UTC));
				to.setTargetNode((String)row.get("targetNode"));
			}
			
			return to;
		}
	}
}
