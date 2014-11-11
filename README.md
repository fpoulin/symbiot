# json-shapeshifter-server

An HTTP server to expose json-shapeshifter through a RESTful API.

# Try it

* Checkout out the sources
* `cd` to the project folder
* Run `mvn clean package`
* Run `java -jar target/json-shapeshifter-server-1.0-SNAPSHOT.jar server config.yml`

A Jetty server is started (listens on port 8080).

You can try it (from browser also can):
* `curl http://localhost:8080/ping`
* `curl http://localhost:8080/ping?echo=alsocan`

...and be amazed by the uselessness of this API at this stage.

# Credits

This project relies on the fabulous [Drop Wizard](http://dropwizard.io/).

# License

Under [MIT License](http://opensource.org/licenses/MIT).