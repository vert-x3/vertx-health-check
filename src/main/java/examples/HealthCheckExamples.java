package examples;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HealthCheckExamples {

  public void example1(Vertx vertx) {
    HealthChecks hc = HealthChecks.create(vertx);

    hc.register(
      "my-procedure",
      promise -> promise.complete(Status.OK()));

    // Register with a timeout. The check fails if it does not complete in time.
    // The timeout is given in ms.
    hc.register(
      "my-procedure",
      2000,
      promise -> promise.complete(Status.OK()));

  }

  public void example2(Vertx vertx) {
    HealthCheckHandler healthCheckHandler1 = HealthCheckHandler.create(vertx);

    HealthCheckHandler healthCheckHandler2 = HealthCheckHandler
      .createWithHealthChecks(HealthChecks.create(vertx));

    Router router = Router.router(vertx);
    // Populate the router with routes...
    // Register the health check handler
    router.get("/health*").handler(healthCheckHandler1);
    // Or
    router.get("/ping*").handler(healthCheckHandler2);
  }


  public void example2(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Register procedures
    // It can be done after the route registration, or even at runtime
    healthCheckHandler.register("my-procedure-name", promise -> {
      // Do the check ....
      // Upon success do
      promise.complete(Status.OK());
      // In case of failure do:
      promise.complete(Status.KO());
    });

    // Register another procedure with a timeout (2s). If the procedure does not complete in
    // the given time, the check fails.
    healthCheckHandler.register(
      "my-procedure-name-with-timeout",
      2000,
      promise -> {
        // Do the check ....
        // Upon success do
        promise.complete(Status.OK());
        // In case of failure do:
        promise.complete(Status.KO());
      });

    router.get("/health").handler(healthCheckHandler);
  }


  public void example3(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Register procedures
    // Procedure can be grouped. The group is deduced using a name with "/".
    // Groups can contains other group
    healthCheckHandler.register(
      "a-group/my-procedure-name",
      promise -> {
        //....
      });
    healthCheckHandler.register(
      "a-group/a-second-group/my-second-procedure-name",
      promise -> {
        //....
      });

    router.get("/health").handler(healthCheckHandler);
  }

  public void example4(Vertx vertx, Router router) {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    // Status can provide addition data provided as JSON
    healthCheckHandler.register("my-procedure-name", promise -> {
      promise.complete(Status.OK(new JsonObject().put("available-memory", "2mb")));
    });

    healthCheckHandler.register("my-second-procedure-name", promise -> {
      promise.complete(Status.KO(new JsonObject().put("load", 99)));
    });

    router.get("/health").handler(healthCheckHandler);
  }

  class SqlConnection {
    Future<Void> close() {
      return Future.succeededFuture();
    }
  }

  class Pool {
    Future<SqlConnection> getConnection() {
      return Future.succeededFuture();
    }
  }

  Pool pool;

  public void pool(HealthCheckHandler handler) {
    handler.register("database",
        promise ->
          pool.getConnection()
          .compose(SqlConnection::close)
          .<Status>mapEmpty()
          .onComplete(promise));
  }

  public void httpEndPoint(ServiceDiscovery discovery, HealthCheckHandler handler) {
    handler.register("my-service",
        promise ->
          HttpEndpoint.getClient(discovery, rec -> "my-service".equals(rec.getName()))
          .compose(HttpClient::close)
          .map(Status.OK())
          .onComplete(promise));
  }

  public void eventbus(Vertx vertx, HealthCheckHandler handler) {
    handler.register("receiver",
      promise ->
        vertx.eventBus().request("health", "ping")
          .onSuccess(msg -> {
            promise.complete(Status.OK());
          })
          .onFailure(err -> {
            promise.complete(Status.KO());
          }));
  }

  public void publishOnEventBus(Vertx vertx, HealthChecks healthChecks) {
    vertx.eventBus().consumer("health",
      message -> healthChecks.checkStatus()
        .onSuccess(message::reply)
        .onFailure(err -> message.fail(0, err.getMessage())));
  }
}
