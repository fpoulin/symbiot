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
package la.alsocan.symbiot.access;

import java.util.List;
import java.util.Map;
import la.alsocan.symbiot.api.to.inputs.ApiPullInputTo;
import la.alsocan.symbiot.api.to.inputs.ApiPushInputTo;
import la.alsocan.symbiot.api.to.inputs.FilesystemInputTo;
import la.alsocan.symbiot.api.to.inputs.InputTo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.skife.jdbi.v2.BaseResultSetMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

/**
 * Dao for inputs, using a single table inheritance strategy (to keep it <i>simple</i>).
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class InputDao {
	
	public static final String TABLE_NAME = "inputs";
	public static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "creationDate TIMESTAMP NOT NULL, "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "driverId VARCHAR(32) NOT NULL, "
			  + "inputDefinitionId VARCHAR(32) NOT NULL, "
			  + "type VARCHAR(32) NOT NULL, "
			  + "name VARCHAR(32) NOT NULL, "
			  + "description VARCHAR(512) NOT NULL, "
			  + "frequency INTEGER, "
			  + "url VARCHAR(256), "
			  + "method VARCHAR(6), "
			  + "folder VARCHAR(256), "
			  + "regex VARCHAR(64), "
			  + "deleteAfterRead BOOLEAN, "
			  + "CONSTRAINT input_key PRIMARY KEY (id))";
	
			// FIXME: handle headers here
	
	private final DBI jdbi;
	
	public InputDao(DBI jdbi) {
		this.jdbi = jdbi;
	}
	
	public int insert(InputTo inputTo) {
		
		int id;
		try (Handle h = jdbi.open()) {
			id = -1;
			switch(inputTo.getType()) {
				case ApiPushInputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, inputDefinitionId, type, name, description) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :inputDefinitionId, :type, :name, :description)")
						.bind("driverId", inputTo.getDriverId())
						.bind("inputDefinitionId", inputTo.getInputDefinitionId())
						.bind("type", ApiPushInputTo.TYPE)
						.bind("name", inputTo.getName())
						.bind("description", inputTo.getDescription())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case ApiPullInputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, inputDefinitionId, type, name, description, frequency, url, method) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :inputDefinitionId, :type, :name, :description, :frequency, :url, :method)")
						.bind("driverId", inputTo.getDriverId())
						.bind("inputDefinitionId", inputTo.getInputDefinitionId())
						.bind("type", ApiPullInputTo.TYPE)
						.bind("name", inputTo.getName())
						.bind("description", inputTo.getDescription())
						.bind("frequency", ((ApiPullInputTo)inputTo).getFrequency())
						.bind("url", ((ApiPullInputTo)inputTo).getUrl())
						.bind("method", ((ApiPullInputTo)inputTo).getMethod())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					
						// FIXME: handle headers here
					
					break;
				case FilesystemInputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, inputDefinitionId, type, name, description, folder, regex, deleteAfterRead) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :inputDefinitionId, :type, :name, :description, :folder, :regex, :deleteAfterRead)")
						.bind("driverId", inputTo.getDriverId())
						.bind("inputDefinitionId", inputTo.getInputDefinitionId())
						.bind("type", FilesystemInputTo.TYPE)
						.bind("name", inputTo.getName())
						.bind("description", inputTo.getDescription())
						.bind("folder", ((FilesystemInputTo)inputTo).getFolder())
						.bind("regex", ((FilesystemInputTo)inputTo).getRegex())
						.bind("deleteAfterRead", ((FilesystemInputTo)inputTo).getDeleteAfterRead())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
			}
		}
		return id;
	}
	
	public List<InputTo> findAll() {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME)
					  .map(new InputMapper())
					  .list();
		}
	}
	
	public InputTo findById(int id) {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = :id")
					  .bind("id", id)
					  .map(new InputMapper())
					  .first();
		}
	}
	
	public void update(int id, InputTo newTo) {
		
		try (Handle h = jdbi.open()) {
			switch(newTo.getType()) {
				case ApiPushInputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.execute();
					break;
				case ApiPullInputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description, "
						+ "frequency = :frequency, "
						+ "url = :url, "
						+ "method = :method "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.bind("frequency", ((ApiPullInputTo)newTo).getFrequency())
						.bind("url", ((ApiPullInputTo)newTo).getUrl())
						.bind("method", ((ApiPullInputTo)newTo).getMethod())
						.execute();
					
						// FIXME: handle headers here
					
					break;
				case FilesystemInputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description, "
						+ "folder = :folder, "
						+ "regex = :regex, "
						+ "deleteAfterRead = :deleteAfterRead "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.bind("folder", ((FilesystemInputTo)newTo).getFolder())
						.bind("regex", ((FilesystemInputTo)newTo).getRegex())
						.bind("deleteAfterRead", ((FilesystemInputTo)newTo).getDeleteAfterRead())
						.execute();
					break;
			}
		}
	}
	
	public void delete(int id) {
		try(Handle h = jdbi.open()){
			h.createStatement("DELETE FROM " + TABLE_NAME + " WHERE id = :id")
					  .bind("id", id)
					  .execute();
		}
	}
	
	private class InputMapper extends BaseResultSetMapper<InputTo> {
		@Override
		protected InputTo mapInternal(int index, Map<String, Object> row) {
			
			String type = (String)row.get("type");
			
			// specific fields
			InputTo to = null;
			switch(type) {
				case ApiPushInputTo.TYPE:
					to = new ApiPushInputTo();
					break;
				case ApiPullInputTo.TYPE:
					to = new ApiPullInputTo();
					((ApiPullInputTo)to).setFrequency((int)row.get("frequency"));
					((ApiPullInputTo)to).setUrl((String)row.get("url"));
					((ApiPullInputTo)to).setMethod((String)row.get("method"));
					
					// FIXME: handle headers here
					
					break;
				case FilesystemInputTo.TYPE:
					to = new FilesystemInputTo();
					((FilesystemInputTo)to).setFolder((String)row.get("folder"));
					((FilesystemInputTo)to).setRegex((String)row.get("regex"));
					((FilesystemInputTo)to).setDeleteAfterRead((Boolean)row.get("deleteAfterRead"));
					break;
			}
			
			// common fields
			if (to != null) {
				to.setId((Integer)row.get("id"));
				to.setName((String)row.get("name"));
				to.setDescription((String)row.get("description"));
				to.setDriverId((String)row.get("driverId"));
				to.setInputDefinitionId((String)row.get("inputDefinitionId"));
				to.setCreationDate(new DateTime(row.get("creationDate"), DateTimeZone.UTC));
				to.setLastModificationDate(new DateTime(row.get("lastModificationDate"), DateTimeZone.UTC));
			}
			
			return to;
		}
	}
}
