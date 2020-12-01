package io.vertx.ext.healthchecks;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.impl.HealthChecksImpl;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface HealthChecks {

  /**
   * Creates a new instance of the default implementation of {@link HealthChecks}.
   *
   * @param vertx the instance of Vert.x, must not be {@code null}
   * @return the created instance
   */
  static HealthChecks create(Vertx vertx) {
    return new HealthChecksImpl(vertx);
  }

  /**
   * Registers a health check procedure.
   * <p>
   * The procedure is a {@link Handler} taking a {@link Promise} of {@link Status} as parameter.
   * Procedures are asynchronous, and <strong>must</strong> complete or fail the given {@link Promise}.
   * If the future object is failed, the procedure outcome is considered as `DOWN`. If the future is
   * completed without any object, the procedure outcome is considered as `UP`. If the future is completed
   * with a (not-null) {@link Status}, the procedure outcome is the received status.
   * <p>
   * This method uses a 1s timeout. Use {@link #register(String, long, Handler)} to configure the timeout.
   *
   * @param name      the name of the procedure, must not be {@code null} or empty
   * @param procedure the procedure, must not be {@code null}
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks register(String name, Handler<Promise<Status>> procedure);

  /**
   * Registers a health check procedure.
   * <p>
   * The procedure is a {@link Handler} taking a {@link Promise} of {@link Status} as parameter.
   * Procedures are asynchronous, and <strong>must</strong> complete or fail the given {@link Promise}.
   * If the future object is failed, the procedure outcome is considered as `DOWN`. If the future is
   * completed without any object, the procedure outcome is considered as `UP`. If the future is completed
   * with a (not-null) {@link Status}, the procedure outcome is the received status.
   *
   * @param name      the name of the procedure, must not be {@code null} or empty
   * @param timeout   the procedure timeout in milliseconds
   * @param procedure the procedure, must not be {@code null}
   * @return the current {@link HealthChecks}
   */
  HealthChecks register(String name, long timeout, Handler<Promise<Status>> procedure);

  /**
   * Unregisters a procedure.
   *
   * @param name the name of the procedure
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks unregister(String name);


  /**
   * Invokes the registered procedures and computes the outcome.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received the computed
   *                      {@link JsonObject}.
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks invoke(Handler<JsonObject> resultHandler);


  /**
   * Invokes the registered procedure with the given name and sub-procedures. It computes the overall
   * outcome.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received an
   *                      {@link AsyncResult} marked as failed if the procedure with the given name cannot
   *                      be found or invoked.
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks invoke(String name, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Like {@link #invoke(String, Handler)} but with a future of the result.
   */
  Future<JsonObject> invoke(String name);

  /**
   * Invokes the registered procedures.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received the computed
   *                      {@link CheckResult}.
   */
  void checkStatus(Handler<AsyncResult<CheckResult>> resultHandler);

  /**
   * Like {@link #checkStatus(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<CheckResult> checkStatus();

  /**
   * Invokes the registered procedure with the given name and sub-procedures.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received an
   *                      {@link AsyncResult} marked as failed if the procedure with the given name cannot
   *                      be found or invoked.
   */
  void checkStatus(String name, Handler<AsyncResult<CheckResult>> resultHandler);

  /**
   * Like {@link #checkStatus(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<CheckResult> checkStatus(String name);

}
