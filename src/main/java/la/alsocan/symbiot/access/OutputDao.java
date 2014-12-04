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
import la.alsocan.symbiot.api.to.outputs.FilesystemOutputTo;
import la.alsocan.symbiot.api.to.outputs.OutputTo;
import la.alsocan.symbiot.api.to.outputs.PollingOutputTo;
import la.alsocan.symbiot.api.to.outputs.WebhookOutputTo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.skife.jdbi.v2.BaseResultSetMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

/**
 * Dao for outputs, using a single table inheritance strategy (to keep it <i>simple</i>).
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class OutputDao {
	
	public static final String TABLE_NAME = "outputs";
	public static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "creationDate TIMESTAMP NOT NULL, "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "driverId VARCHAR(32) NOT NULL, "
			  + "outputDefinitionId VARCHAR(32) NOT NULL, "
			  + "type VARCHAR(32) NOT NULL, "
			  + "name VARCHAR(32) NOT NULL, "
			  + "description VARCHAR(512) NOT NULL, "
			  + "url VARCHAR(256), "
			  + "method VARCHAR(6), "
			  + "ttl INTEGER, "
			  + "folder VARCHAR(256), "
			  + "CONSTRAINT output_key PRIMARY KEY (id))";
	
			// FIXME: handle headers here
	
	private final DBI jdbi;
	
	public OutputDao(DBI jdbi) {
		this.jdbi = jdbi;
	}
	
	public int insert(OutputTo outputTo) {
		
		int id;
		try (Handle h = jdbi.open()) {
			id = -1;
			switch(outputTo.getType()) {
				case WebhookOutputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, outputDefinitionId, type, name, description, url, method) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :outputDefinitionId, :type, :name, :description, :url, :method)")
						.bind("driverId", outputTo.getDriverId())
						.bind("outputDefinitionId", outputTo.getOutputDefinitionId())
						.bind("type", WebhookOutputTo.TYPE)
						.bind("name", outputTo.getName())
						.bind("description", outputTo.getDescription())
						.bind("url", ((WebhookOutputTo)outputTo).getUrl())
						.bind("method", ((WebhookOutputTo)outputTo).getMethod())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					
					// FIXME: handle headers here
					
					break;
				case PollingOutputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, outputDefinitionId, type, name, description, ttl) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :outputDefinitionId, :type, :name, :description, :ttl)")
						.bind("driverId", outputTo.getDriverId())
						.bind("outputDefinitionId", outputTo.getOutputDefinitionId())
						.bind("type", PollingOutputTo.TYPE)
						.bind("name", outputTo.getName())
						.bind("description", outputTo.getDescription())
						.bind("ttl", ((PollingOutputTo)outputTo).getTtl())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
				case FilesystemOutputTo.TYPE:
					id = h.createStatement("INSERT INTO " + TABLE_NAME
						+ " (creationDate, lastModificationDate, driverId, outputDefinitionId, type, name, description, folder) "
						+ "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :driverId, :outputDefinitionId, :type, :name, :description, :folder)")
						.bind("driverId", outputTo.getDriverId())
						.bind("outputDefinitionId", outputTo.getOutputDefinitionId())
						.bind("type", FilesystemOutputTo.TYPE)
						.bind("name", outputTo.getName())
						.bind("description", outputTo.getDescription())
						.bind("folder", ((FilesystemOutputTo)outputTo).getFolder())
						.executeAndReturnGeneratedKeys(IntegerMapper.FIRST)
						.first();
					break;
			}
		}
		return id;
	}
	
	public List<OutputTo> findAll() {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME)
					  .map(new OutputMapper())
					  .list();
		}
	}
	
	public OutputTo findById(int id) {
		try(Handle h = jdbi.open()){
			return h.createQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = :id")
					  .bind("id", id)
					  .map(new OutputMapper())
					  .first();
		}
	}
	
	public void update(int id, OutputTo newTo) {
		
		try (Handle h = jdbi.open()) {
			switch(newTo.getType()) {
				case WebhookOutputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description, "
						+ "url = :url, "
						+ "method = :method "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.bind("url", ((WebhookOutputTo)newTo).getUrl())
						.bind("method", ((WebhookOutputTo)newTo).getMethod())
						.execute();
					
					// FIXME: handle headers here
					
					break;
				case PollingOutputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description, "
						+ "ttl = :ttl "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.bind("ttl", ((PollingOutputTo)newTo).getTtl())
						.execute();
					break;
				case FilesystemOutputTo.TYPE:
					h.createStatement("UPDATE " + TABLE_NAME + " SET "
						+ "lastModificationDate = CURRENT_TIMESTAMP, "
						+ "name = :name, "
						+ "description = :description, "
						+ "folder = :folder "
						+ "WHERE id = :id")
						.bind("id", id)
						.bind("name", newTo.getName())
						.bind("description", newTo.getDescription())
						.bind("folder", ((FilesystemOutputTo)newTo).getFolder())
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
	
	private class OutputMapper extends BaseResultSetMapper<OutputTo> {
		@Override
		protected OutputTo mapInternal(int index, Map<String, Object> row) {
			
			String type = (String)row.get("type");
			
			// specific fields
			OutputTo to = null;
			switch(type) {
				case WebhookOutputTo.TYPE:
					to = new WebhookOutputTo();
					((WebhookOutputTo)to).setUrl((String)row.get("url"));
					((WebhookOutputTo)to).setMethod((String)row.get("method"));
					
					// FIXME: handle headers here
					
					break;
				case PollingOutputTo.TYPE:
					to = new PollingOutputTo();
					((PollingOutputTo)to).setTtl((int)row.get("ttl"));
					break;
				case FilesystemOutputTo.TYPE:
					to = new FilesystemOutputTo();
					((FilesystemOutputTo)to).setFolder((String)row.get("folder"));
					break;
			}
			
			// common fields
			if (to != null) {
				to.setId((Integer)row.get("id"));
				to.setName((String)row.get("name"));
				to.setDescription((String)row.get("description"));
				to.setDriverId((String)row.get("driverId"));
				to.setOutputDefinitionId((String)row.get("outputDefinitionId"));
				to.setCreationDate(new DateTime(row.get("creationDate"), DateTimeZone.UTC));
				to.setLastModificationDate(new DateTime(row.get("lastModificationDate"), DateTimeZone.UTC));
			}
			
			return to;
		}
	}
}
