package io.vertx.ext.healthchecks.impl;

import io.vertx.core.Handler;
import io.vertx.ext.healthchecks.CheckResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DefaultCompositeProcedure implements CompositeProcedure {

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
  public void check(Handler<CheckResult> resultHandler) {
    List<Map.Entry<String, Procedure>> copy;
    synchronized (this) {
      copy = new ArrayList<>(children.entrySet());
    }

    List<CheckResult> checks = new ArrayList<>();
    int size = copy.size();
    CheckResult[] completed = new CheckResult[size];

    Runnable task = () -> {
      for (int j = 0;j < size;j++) {
        CheckResult json = completed[j];
        if (json.getId() == null) {
          json.setId(copy.get(j).getKey());
        }
        checks.add(json);
      }

      CheckResult result = new CheckResult();
      result.setChecks(checks);

      resultHandler.handle(result);
    };

    if (size == 0) {
      task.run();
      return;
    }

    AtomicInteger count = new AtomicInteger(size);


    for (int i = 0; i < size; i++) {
      int idx = i;
      Map.Entry<String, Procedure> entry = copy.get(idx);
      entry.getValue().check(res -> {
        completed[idx] = res;
        if (count.decrementAndGet() == 0) {
          task.run();
        }
      });
    }
  }
}
