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
import la.alsocan.jsonshapeshifterserver.api.TransformationTo;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@RegisterMapper(TransformationMapper.class)
public interface TransformationDao {
	
	static final String TABLE_NAME = "transformations";
	static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "creationDate TIMESTAMP NOT NULL, "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "sourceSchemaId INTEGER NOT NULL, "
			  + "targetSchemaId INTEGER NOT NULL, "
			  + "CONSTRAINT transformations_key PRIMARY KEY (id),"
			  + "CONSTRAINT source_fk FOREIGN KEY (sourceSchemaId) REFERENCES "+SchemaDao.TABLE_NAME+" (id),"
			  + "CONSTRAINT target_fk FOREIGN KEY (targetSchemaId) REFERENCES "+SchemaDao.TABLE_NAME+" (id))";
	
	@SqlUpdate("INSERT INTO " + TABLE_NAME
			  + " (creationDate, lastModificationDate, sourceSchemaId, targetSchemaId) "
			  + "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :sourceSchemaId, :targetSchemaId)")
	@GetGeneratedKeys
	int insert(@Bind("sourceSchemaId") int sourceSchemaId, @Bind("targetSchemaId") int targetSchemaId);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME)
	List<TransformationTo> findAll();
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME 
			  + " WHERE id = :id")
	TransformationTo findById(@Bind("id") int id);
	
	@SqlUpdate("DELETE FROM " + TABLE_NAME 
			  + " WHERE id = :id")
	void delete(@Bind("id") int id);
}
