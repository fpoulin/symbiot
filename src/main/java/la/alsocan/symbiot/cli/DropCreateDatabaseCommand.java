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
package la.alsocan.symbiot.cli;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import la.alsocan.symbiot.ServerConfiguration;
import la.alsocan.symbiot.access.BindingDao;
import la.alsocan.symbiot.access.InputDao;
import la.alsocan.symbiot.access.OutputDao;
import la.alsocan.symbiot.access.StreamDao;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class DropCreateDatabaseCommand extends ConfiguredCommand<ServerConfiguration> {

	private static final String EXPECTED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String EXPECTED_URL = "jdbc:derby:symbiotDB";
	private static final String DB_NAME = "symbiotDB";
	
	public DropCreateDatabaseCommand(String name) {
		super(name, "This command drops the database and create a new one");
	}
	
	@Override
	protected void run(Bootstrap bootstrap, Namespace namespace, ServerConfiguration configuration) throws Exception {
		
		// check that the config uses Derby with the expected name (KISS!)
		if (!EXPECTED_DRIVER.equals(configuration.getDataSourceFactory().getDriverClass()) 
			||!EXPECTED_URL.equals(configuration.getDataSourceFactory().getUrl())) {
			throw new IllegalStateException("Expected driver '" + EXPECTED_DRIVER 
				+ "' and url '" + EXPECTED_URL + "' in the config (yeah, that's life).");
		}
		
		// drop existing database
		File f = new File(DB_NAME);
		if (f.exists() && f.isDirectory()) {
			FileUtils.deleteDirectory(f);
		}
		
		// create new database (with DDL)
		DriverManager.registerDriver((Driver)Class.forName(EXPECTED_DRIVER).newInstance());
		try (Connection c = DriverManager.getConnection(EXPECTED_URL
			+ ";create=true"
			+ ";user=" + configuration.getDataSourceFactory().getUser()
			+ ";password=" + configuration.getDataSourceFactory().getPassword())) {
			
			c.createStatement().executeUpdate(InputDao.DDL);
			c.createStatement().executeUpdate(OutputDao.DDL);
			c.createStatement().executeUpdate(StreamDao.DDL);
			c.createStatement().executeUpdate(BindingDao.DDL);
		}
	}
}
