package io.vertx.ext.healthchecks.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.ext.healthchecks.Status;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DefaultProcedure implements Procedure {

  private final Handler<Promise<Status>> handler;
  private final String name;

  private final Vertx vertx;
  private final long timeout;

  public DefaultProcedure(Vertx vertx, String name, long timeout,
                   Handler<Promise<Status>> handler) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(name);
    Objects.requireNonNull(handler);
    this.timeout = timeout;
    this.name = name;
    this.handler = handler;
    this.vertx = vertx;
  }

  @Override
  public void check(Handler<CheckResult> resultHandler) {
    try {
      Promise<Status> promise = Promise.promise();
      promise.future().onComplete(ar -> resultHandler.handle(from(name, ar)));

      if (timeout >= 0) {
        vertx.setTimer(timeout, l -> promise.tryFail(new ProcedureException("Timeout")));
      }

      try {
        handler.handle(promise);
      } catch (Exception e) {
        promise.tryFail(new ProcedureException(e));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static CheckResult from(String name, AsyncResult<?> ar) {
    if (ar.succeeded()) {
      // We may get a JSON Object, if completed using:
      // future.complete({ok: true});
      Status res = null;
      if (ar.result() instanceof Status) {
        res = (Status) ar.result();
      } else if (ar.result() instanceof JsonObject) {
        res = new Status((JsonObject) ar.result());
      }
      return CheckResult.from(name, res);
    } else {
      return CheckResult.from(name, ar.cause());
    }
  }
}
