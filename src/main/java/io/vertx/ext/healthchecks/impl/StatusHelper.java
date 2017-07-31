package io.vertx.ext.healthchecks.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StatusHelper {

  public static JsonObject from(String name, AsyncResult<?> ar) {
    // We may get a JSON Object, if completed using:
    // future.complete({ok: true});
    Status res = null;
    if (ar.result() instanceof Status) {
      res = (Status) ar.result();
    } else if (ar.result() instanceof JsonObject) {
      res = new Status((JsonObject) ar.result());
    }

    JsonObject json = new JsonObject();
    if (name != null) {
      json.put("id", name);
    }
    if (ar.succeeded()) {
      if (res != null && !res.isOk()) {
        json.put("status", "DOWN");
      } else {
        json.put("status", "UP");
      }

      if (res != null && res.getData() != null && !res.getData().isEmpty()) {
        json.put("data", res.getData());
      }

      if (res != null && res.isProcedureInError()) {
        json.put("error", true);
      }
    } else {
      json
        .put("status", "DOWN")
        .put("data", new JsonObject().put("cause", ar.cause().getMessage()));
    }

    return json;
  }

  public static JsonObject onError(String name, ProcedureException e) {
    JsonObject json = new JsonObject();
    if (name != null) {
      json.put("id", name);
    }
    return json
      .put("status", "DOWN")
      .put("data", new JsonObject()
        .put("procedure-execution-failure", true)
        .put("cause", e.getMessage()));
  }

  public static boolean isUp(Future<JsonObject> json) {
    return !json.failed() && isUp(json.result());

  }

  public static boolean isUp(JsonObject json) {
    // In case of success
    // Case 1) no result -> UP
    // Case 2) result with "status" == "UP" -> UP
    // Case 3) result with "outcome" == "UP" -> UP
    return json == null || "UP".equals(json.getString("status")) || "UP".equals(json.getString("outcome"));

  }
}
