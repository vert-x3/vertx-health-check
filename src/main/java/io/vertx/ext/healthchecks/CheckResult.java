package io.vertx.ext.healthchecks;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.impl.ProcedureException;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class CheckResult {

  public static CheckResult from(String name, Status status) {
    return new CheckResult()
      .setId(name)
      .setStatus(status);
  }

  public static CheckResult from(String name, Throwable e) {
    return new CheckResult()
      .setId(name)
      .setFailure(e);
  }

  public static boolean isUp(Future<CheckResult> json) {
    return !json.failed() && isUp(json.result());
  }

  public static boolean isUp(CheckResult json) {
    // In case of success
    // Case 1) no result -> UP
    // Case 2) result with "status" == "UP" -> UP
    // Case 3) result with "outcome" == "UP" -> UP
    return json == null || json.getUp();

  }

  private List<CheckResult> checks;
  private String id;
  private Status status;
  private Throwable failure;

  public CheckResult() {
  }

  public CheckResult setId(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public Status getStatus() {
    return status;
  }

  public CheckResult setStatus(Status status) {
    this.status = status;
    return this;
  }

  public Boolean getUp() {
    if (status != null) {
      return status.isOk();
    } else if (failure != null) {
      return false;
    }
    List<CheckResult> checks = getChecks();
    if (checks != null) {
      for (CheckResult check : checks) {
        if (!check.getUp()) {
          return false;
        }
      }
    }
    return true;
  }

  public JsonObject getData() {
    if (status != null) {
      if (status.getData().isEmpty()) {
        return null;
      }
      return status.getData();
    } else if (failure != null) {
      JsonObject data = new JsonObject();
      data.put("cause", failure.getMessage());
      if (failure instanceof ProcedureException) {
        data.put("procedure-execution-failure", true);
      }
      return data;
    }
    return null;
  }

  public Throwable getFailure() {
    return failure;
  }

  public CheckResult setFailure(Throwable failure) {
    this.failure = failure;
    return this;
  }

  public CheckResult setChecks(List<CheckResult> checks) {
    this.checks = checks;
    return this;
  }

  public List<CheckResult> getChecks() {
    return checks;
  }

  /**
   * Get a JSON version of this result, it computes the overall outcome.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (id != null) {
      json.put("id", id);
    }
    json.put("status", getUp() ? "UP" : "DOWN");
    JsonObject data = getData();
    if (data != null) {
      json.put("data", data.copy());
    }
    if (status != null && status.isProcedureInError()) {
      json.put("error", true);
    }
    if (checks != null) {
      JsonArray array = new JsonArray();
      for (CheckResult check : checks) {
        JsonObject nested = check.toJson();
        nested.remove("outcome"); // Only top level
        array.add(nested);
      }
      json.put("checks", array);
    }
    json.put("outcome", getUp() ? "UP" : "DOWN");
    return json;
  }

}
