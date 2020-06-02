package io.vertx.ext.healthchecks.impl;

import io.vertx.core.Handler;
import io.vertx.ext.healthchecks.CheckResult;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface Procedure {

  void check(Handler<CheckResult> resultHandler);

}
