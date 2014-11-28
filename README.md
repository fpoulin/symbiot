# symbiot

A simple Internet of Things service that runs on your machine.

# Try it

* Checkout out the sources
* `cd` to the project folder
* Run `mvn clean package`
* Run `java -jar target/symbiot-{version}.jar drop-and-create-db config.yml`
* Run `java -jar target/symbiot-{version}.jar server config.yml`

A Jetty server is started (listens on port 8080).

You can try it (from browser also can):
* `curl http://localhost:8080/ping`
* `curl http://localhost:8080/ping?echo=alsocan`

...and be amazed by the uselessness of this API at this stage.

# Credits

This project relies on the fabulous [DropWizard](http://dropwizard.io/).

# License

Under [MIT License](http://opensource.org/licenses/MIT).
