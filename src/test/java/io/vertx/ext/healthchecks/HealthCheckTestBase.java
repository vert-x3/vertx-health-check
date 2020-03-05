package io.vertx.ext.healthchecks;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HealthCheckTestBase {

  Vertx vertx;
  HealthCheckHandler handler;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    handler = HealthCheckHandler.create(vertx, getAuthProvider());
    router.get("/health*").handler(handler);

    // Only for authentication tests
    router.post("/post-health").handler(BodyHandler.create());
    router.post("/post-health").handler(handler);

    Router sub = Router.router(vertx);
    sub.get("/ping*").handler(handler);
    router.mountSubRouter("/prefix", sub);


    // Reproducer for https://github.com/vert-x3/vertx-health-check/issues/13
    // This sub-router does not pass a path to the route but handle all GET requests.
    Router subRouter = Router.router(vertx);
    subRouter.get().handler(handler);
    router.mountSubRouter("/no-route", subRouter);

    AtomicBoolean done = new AtomicBoolean();
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080, ar -> done.set(ar.succeeded()));
    await().untilAtomic(done, is(true));

    Restafari.baseURI = "http://localhost";
    Restafari.port = 8080;
  }

  AuthenticationProvider getAuthProvider() {
    return null;
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    vertx.close(v -> done.set(v.succeeded()));
    await().untilAtomic(done, is(true));
  }

  protected String prefix() {
    return "/prefix";
  }

  protected String route() {
    return "/ping";
  }

  JsonObject get(int status) {
    String json = Restafari.get("/health")
      .then()
      .statusCode(status)
      .header("content-type", "application/json")
      .extract().asString();
    return new JsonObject(json);
  }

  JsonObject getWithPrefix(int status) {
    String json = Restafari.get(prefix() + route())
      .then()
      .statusCode(status)
      .header("content-type", "application/json")
      .extract().asString();
    return new JsonObject(json);
  }

  JsonObject get(String path, int status) {
    String json = Restafari.get("/health/" + path)
      .then()
      .statusCode(status)
      .header("content-type", "application/json")
      .extract().asString();
    return new JsonObject(json);
  }

  JsonObject getWithPrefix(String path, int status) {
    String json = Restafari.get(prefix() + route() + "/" + path)
      .then()
      .statusCode(status)
      .header("content-type", "application/json")
      .extract().asString();
    return new JsonObject(json);
  }
}
