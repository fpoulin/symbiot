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
import la.alsocan.symbiot.api.to.StreamTo;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
@RegisterMapper(StreamMapper.class)
public interface StreamDao {
	
	static final String TABLE_NAME = "streams";
	static final String DDL = 
			  "CREATE TABLE " + TABLE_NAME + "("
			  + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
			  + "creationDate TIMESTAMP NOT NULL, "
			  + "lastModificationDate TIMESTAMP NOT NULL, "
			  + "inputId INTEGER NOT NULL, "
			  + "outputId INTEGER NOT NULL, "
			  + "totalBindings INTEGER NOT NULL, "
			  + "CONSTRAINT streams_key PRIMARY KEY (id),"
			  + "CONSTRAINT input_fk FOREIGN KEY (inputId) REFERENCES "+InputDao.TABLE_NAME+" (id) ON DELETE RESTRICT,"
			  + "CONSTRAINT output_fk FOREIGN KEY (outputId) REFERENCES "+OutputDao.TABLE_NAME+" (id) ON DELETE RESTRICT)";
	
	@SqlUpdate("INSERT INTO " + TABLE_NAME
			  + " (creationDate, lastModificationDate, inputId, outputId, totalBindings) "
			  + "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :inputId, :outputId, :totalBindings)")
	@GetGeneratedKeys
	int insert(
			  @Bind("inputId") int inputId, 
			  @Bind("outputId") int outputId,
			  @Bind("totalBindings") int totalBindings);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME)
	List<StreamTo> findAll();
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = :id")
	StreamTo findById(@Bind("id") int id);
	
	@SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE inputId = :inputId")
	int countByInput(@Bind("inputId") int inputId);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE inputId = :inputId")
	List<StreamTo> findByInput(@Bind("inputId") int inputId);
	
	@SqlQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE outputId = :outputId")
	int countByOutput(@Bind("outputId") int outputId);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE outputId = :outputId")
	List<StreamTo> findByOutput(@Bind("outputId") int outputId);
	
	@SqlQuery("SELECT * FROM " + TABLE_NAME + " WHERE inputId = :inputId AND outputId = :outputId")
	List<StreamTo> findByInputAndOutput(@Bind("inputId") int inputId, @Bind("outputId") int outputId);
	
	@SqlUpdate("DELETE FROM " + TABLE_NAME + " WHERE id = :id")
	void delete(@Bind("id") int id);
}
