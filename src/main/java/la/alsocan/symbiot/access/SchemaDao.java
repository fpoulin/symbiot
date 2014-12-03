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
import la.alsocan.symbiot.api.to.SchemaTo;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@RegisterMapper(SchemaMapper.class)
public interface SchemaDao {
	
	static final String TABLE_NAME = "schemas";
	static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "creationDate TIMESTAMP NOT NULL, "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "schemaStr CLOB(5000) NOT NULL, "
			  + "CONSTRAINT schemas_key PRIMARY KEY (id))";
	
	@SqlUpdate("INSERT INTO " + TABLE_NAME
			  + " (creationDate, lastModificationDate, schemaStr)"
			  + " VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :schemaStr)")
	@GetGeneratedKeys
	int insert(@Bind("schemaStr") final String schemaStr);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME)
	List<SchemaTo> findAll();
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = :id")
	SchemaTo findById(@Bind("id") int id);
	
	@SqlUpdate("UPDATE " + TABLE_NAME
			  + " SET lastModificationDate = CURRENT_TIMESTAMP, schemaStr = :schemaStr"
			  + " WHERE id = :id")
	void update(@Bind("id") int id, @Bind("schemaStr") String schemaStr);
	
	@SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE id = :id")
	void delete(@Bind("id") int id);
}
