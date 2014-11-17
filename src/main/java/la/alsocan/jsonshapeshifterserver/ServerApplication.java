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
package la.alsocan.jsonshapeshifterserver;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import la.alsocan.jsonshapeshifterserver.cli.DropCreateDatabaseCommand;
import la.alsocan.jsonshapeshifterserver.health.PingHealthCheck;
import la.alsocan.jsonshapeshifterserver.jdbi.SchemaDao;
import la.alsocan.jsonshapeshifterserver.resources.PingResource;
import la.alsocan.jsonshapeshifterserver.resources.SchemaResource;
import org.skife.jdbi.v2.DBI;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class ServerApplication extends Application<ServerConfiguration> {

	public static void main(String[] args) throws Exception {
		new ServerApplication().run(args);
	}

	@Override
	public String getName() {
		return "shapeshifter-server";
	}
	
	@Override
	public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
		bootstrap.addCommand(new DropCreateDatabaseCommand("drop-and-create-db"));
	}

	@Override
	public void run(ServerConfiguration conf, Environment env) throws ClassNotFoundException {

		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(env, conf.getDataSourceFactory(), "derby");
		final SchemaDao schemaDao = jdbi.onDemand(SchemaDao.class);
		
		// health checks
		env.healthChecks().register("ping", new PingHealthCheck());
		
		// configure object mapper
		env.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		// resources
		env.jersey().register(new PingResource(conf.getEcho()));
		env.jersey().register(new SchemaResource(schemaDao));
	}
}
