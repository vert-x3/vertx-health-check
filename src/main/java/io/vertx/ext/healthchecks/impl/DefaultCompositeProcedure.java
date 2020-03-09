package io.vertx.ext.healthchecks.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

import static io.vertx.ext.healthchecks.impl.StatusHelper.isUp;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
class DefaultCompositeProcedure implements CompositeProcedure {

  private Map<String, Procedure> children = new HashMap<>();

  @Override
  public DefaultCompositeProcedure add(String name, Procedure check) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(check);

    synchronized (this) {
      children.put(name, check);
    }

    return this;
  }

  @Override
  public synchronized boolean remove(String name) {
    Objects.requireNonNull(name);
    return children.remove(name) != null;
  }

  @Override
  public synchronized Procedure get(String name) {
    return children.get(name);
  }

  @Override
  public void check(Handler<JsonObject> resultHandler) {
    Map<String, Procedure> copy = new HashMap<>();
    synchronized (this) {
      copy.putAll(children);
    }

    JsonObject result = new JsonObject();
    JsonArray checks = new JsonArray();
    result.put("checks", checks);

    Map<String, Promise<JsonObject>> tasks = new HashMap<>();
    List<Future> completed = new ArrayList<>();
    for (Map.Entry<String, Procedure> entry : copy.entrySet()) {
      Promise<JsonObject> promise = Promise.promise();
      completed.add(promise.future());
      tasks.put(entry.getKey(), promise);
      entry.getValue().check(promise::complete);
    }

    CompositeFuture.join(completed)
      .onComplete(ar -> {
        boolean success = true;
        for (Map.Entry<String, Promise<JsonObject>> entry : tasks.entrySet()) {
          Future<JsonObject> json = entry.getValue().future();
          boolean up = isUp(json);
          success = success && up;

          JsonObject r = new JsonObject()
            .put("id", json.result().getString("id", entry.getKey()))
            .put("status", up ? "UP" : "DOWN");

          if (json.result() != null) {
            JsonObject data = json.result().getJsonObject("data");
            JsonArray children = json.result().getJsonArray("checks");
            if (data != null) {
              data.remove("result");
              r.put("data", data);
            } else if (children != null) {
              r.put("checks", children);
            }
          }

          checks.add(r);
        }

        if (success) {
          result.put("outcome", "UP");
        } else {
          result.put("outcome", "DOWN");
        }

        resultHandler.handle(result);

      });
  }


}
