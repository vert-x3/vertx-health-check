package io.vertx.ext.healthchecks;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.ext.healthchecks.Assertions.assertThatCheck;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

@RunWith(VertxUnitRunner.class)
public class HealthChecksTest {

  Vertx vertx;
  HealthChecks healthChecks;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    healthChecks = HealthChecks.create(vertx);
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close().onComplete(tc.asyncAssertSuccess());
  }

  @Test
  public void testWithEmptySuccessfulCheck(TestContext tc) {
    healthChecks.register("foo", Promise::complete);

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeUp()
          .hasChildren(1)
          .hasAndGetCheck("foo").isUp().done();
      });
    }));
  }

  @Test
  public void testWithEmptyFailedCheck(TestContext tc) {
    healthChecks.register("foo", promise -> promise.fail("BOOM"));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown().hasData("cause", "BOOM").done();
      });
    }));
  }

  @Test
  public void testWithExplicitSuccessfulCheck(TestContext tc) {
    healthChecks.register("bar", promise -> promise.complete(Status.OK()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeUp()
          .hasChildren(1)
          .hasAndGetCheck("bar").isUp().done();
      });
    }));
  }

  @Test
  public void testWithExplicitFailedCheck(TestContext tc) {
    healthChecks.register("bar", promise -> promise.complete(Status.KO()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("bar").isDown().done();
      });
    }));
  }

  @Test
  public void testWithExplicitSuccessfulCheckAndData(TestContext tc) {
    healthChecks.register("bar", promise -> {
      promise.complete(Status.OK(new JsonObject().put("availableMemory", "2Mb")));
    });

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThat(json.getMap())
          .contains(entry("outcome", "UP"));

        JsonArray array = json.getJsonArray("checks");
        assertThat(array.getList()).hasSize(1);
        JsonObject check = array.getJsonObject(0);
        assertThat(check.getMap()).hasSize(3)
          .contains(
            entry("status", "UP"),
            entry("id", "bar"),
            entry("data", new JsonObject().put("availableMemory", "2Mb")));
      });
    }));
  }

  @Test
  public void testWithExplicitFailedCheckAndData(TestContext tc) {
    healthChecks.register("bar", promise -> {
      promise.complete(Status.KO(new JsonObject().put("availableMemory", "2Mb")));
    });

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThat(json.getMap())
          .contains(entry("outcome", "DOWN"));

        JsonArray array = json.getJsonArray("checks");
        assertThat(array.getList()).hasSize(1);
        JsonObject check = array.getJsonObject(0);
        assertThat(check.getMap()).hasSize(3)
          .contains(
            entry("status", "DOWN"),
            entry("id", "bar"),
            entry("data", new JsonObject().put("availableMemory", "2Mb")));
      });
    }));
  }

  @Test
  public void testWithOneSuccessfulAndOneFailedCheck(TestContext tc) {
    healthChecks
      .register("s", promise -> promise.complete(Status.OK()))
      .register("f", promise -> promise.complete(Status.KO()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown().hasChildren(2);
      });
    }));
  }

  @Test
  public void testWithTwoFailedChecks(TestContext tc) {
    healthChecks
      .register("f1", promise -> promise.complete(Status.KO()))
      .register("f2", promise -> promise.complete(Status.KO()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown().hasChildren(2);
      });
    }));
  }

  @Test
  public void testWithTwoSucceededChecks(TestContext tc) {
    healthChecks
      .register("s1", promise -> promise.complete(Status.OK()))
      .register("s2", promise -> promise.complete(Status.OK()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeUp().hasChildren(2);
      });
    }));
  }

  @Test
  public void testWithNestedCompositeThatSucceed(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.OK()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isUp()
          .hasOutcomeUp()
          .hasChildren(2)
          .hasAndGetCheck("sub").hasStatusUp()
          .hasAndGetCheck("B").hasStatusUp().done()
          .hasAndGetCheck("A").hasStatusUp().done()
          .done()

          .hasAndGetCheck("sub2").hasStatusUp()
          .hasAndGetCheck("c").hasStatusUp()
          .hasAndGetCheck("C1").hasStatusUp().done()
          .hasAndGetCheck("C2").hasStatusUp().done()
          .done()

          .done();
      });
    }));
  }

  @Test
  public void testWithNestedCompositeThatFailed(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isDown()
          .hasOutcomeDown()
          .hasChildren(2)
          .hasAndGetCheck("sub").hasStatusUp()
          .hasAndGetCheck("B").hasStatusUp().done()
          .hasAndGetCheck("A").hasStatusUp().done()
          .done()

          .hasAndGetCheck("sub2").hasStatusDown()
          .hasAndGetCheck("c").hasStatusDown()
          .hasAndGetCheck("C1").hasStatusUp().done()
          .hasAndGetCheck("C2").hasStatusDown().done()
          .done()

          .done();
      });
    }));
  }


  @Test
  public void testRetrievingAComposite(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    Async async = tc.async(3);

    healthChecks.checkStatus("sub").map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isUp()
          .hasOutcomeUp()
          .hasChildren(2)
          .hasAndGetCheck("B").hasStatusUp().done()
          .hasAndGetCheck("A").hasStatusUp().done()
          .done();
        async.countDown();
      });
    }));


    healthChecks.checkStatus("sub2").map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .hasAndGetCheck("c").hasStatusDown()
          .hasAndGetCheck("C1").hasStatusUp().done()
          .hasAndGetCheck("C2").hasStatusDown().done()
          .done();
        async.countDown();
      });
    }));

    healthChecks.checkStatus("sub2/c").map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .hasAndGetCheck("C1").hasStatusUp().done()
          .hasAndGetCheck("C2").hasStatusDown().done();
        async.countDown();
      });
    }));
  }

  @Test
  public void testRetrievingALeaf(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    Async async = tc.async(4);

    healthChecks.checkStatus("sub/A").map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isUp()
          .hasStatusUp()
          .hasOutcomeUp()
          .done();
        async.countDown();
      });
    }));


    healthChecks.checkStatus("sub2/c/C2").map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isDown()
          .hasOutcomeDown()
          .hasStatusDown()
          .done();
        async.countDown();
      });
    }));


    // Not found
    healthChecks.checkStatus("missing").map(CheckResult::toJson).onComplete(tc.asyncAssertFailure(t -> {
      tc.verify(v -> {
        assertThat(t.getMessage()).containsIgnoringCase("not found");
        async.countDown();
      });
    }));

    // Illegal
    healthChecks.checkStatus("sub2/c/C1/foo").map(CheckResult::toJson).onComplete(tc.asyncAssertFailure(t -> {
      async.countDown();
    }));
  }

  @Test
  public void testACheckThatTimeOut(TestContext tc) {
    healthChecks.register("foo", future -> {
      // Bad boy !
    });

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown()
          .hasData("procedure-execution-failure", true)
          .hasData("cause", "Timeout").done();
      });
    }));
  }

  @Test
  public void testACheckThatTimeOutFast(TestContext tc) {
    healthChecks.register("foo", 10, future ->
      vertx.setTimer(100, l -> {
        // Too late...
        future.complete();
      }));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown()
          .hasData("procedure-execution-failure", true)
          .hasData("cause", "Timeout").done();
      });
    }));
  }

  @Test
  public void testACheckThatFail(TestContext tc) {
    healthChecks.register("foo", future -> {
      throw new IllegalArgumentException("BOOM");
    });

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown()
          .hasData("cause", "BOOM")
          .hasData("procedure-execution-failure", true)
          .done();
      });
    }));
  }


  @Test
  public void testACheckThatTimeOutButSucceed(TestContext tc) {
    healthChecks.register("foo", future -> vertx.setTimer(2000, l -> future.complete()));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown().hasData("cause", "Timeout").done();
      });
    }));
  }

  @Test
  public void testACheckThatTimeOutButFailed(TestContext tc) {
    healthChecks.register("foo", future -> vertx.setTimer(2000, l -> future.fail("BOOM")));

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json).hasOutcomeDown()
          .hasChildren(1)
          .hasAndGetCheck("foo").isDown().hasData("cause", "Timeout").done();
      });
    }));
  }

  @Test
  public void testRemovingComposite(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    Async async = tc.async();

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isDown()
          .hasOutcomeDown();
        async.complete();
      });
    }));

    async.awaitSuccess();

    healthChecks.unregister("sub2/c");

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isUp()
          .hasOutcomeUp();
      });
    }));
  }

  @Test
  public void testRemovingLeaf(TestContext tc) {
    healthChecks
      .register("sub/A", promise -> promise.complete(Status.OK()))
      .register("sub/B", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C1", promise -> promise.complete(Status.OK()))
      .register("sub2/c/C2", promise -> promise.complete(Status.KO()));

    Async async = tc.async();

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isDown()
          .hasOutcomeDown();
        async.complete();
      });
    }));

    async.awaitSuccess();

    healthChecks.unregister("sub2/c/C2");

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      tc.verify(v -> {
        assertThatCheck(json)
          .isUp()
          .hasOutcomeUp();
      });
    }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidRegistrationOfAProcedure() {
    healthChecks.register("foo", promise -> promise.complete(Status.OK()));
    healthChecks.register("foo/bar", promise -> promise.complete(Status.OK()));
  }

  @Test(expected = NullPointerException.class)
  public void testRegistrationWithNoName() {
    healthChecks.register(null, promise -> promise.complete(Status.OK()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRegistrationWithEmptyName() {
    healthChecks.register("", promise -> promise.complete(Status.OK()));
  }

  @Test(expected = NullPointerException.class)
  public void testRegistrationWithNoProcedure() {
    healthChecks.register("bad", null);
  }

  @Test
  public void testOnEventBus_OK(TestContext tc) {
    vertx.eventBus().consumer("health", ar -> {
      ar.reply("pong");
    });

    registerEventBusProcedure();

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      assertThatCheck(json)
        .isUp()
        .hasOutcomeUp();
    }));
  }

  private void registerEventBusProcedure() {
    healthChecks.register("receiver", promise ->
      vertx.eventBus().request("health", "ping").onComplete(response -> {
        if (response.succeeded()) {
          promise.complete(Status.OK());
        } else {
          promise.complete(Status.KO());
        }
      })
    );
  }

  @Test
  public void testOnEventBus_KO(TestContext tc) {
    vertx.eventBus().consumer("health", ar -> {
      ar.fail(500, "BOOM !");
    });

    registerEventBusProcedure();

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      assertThatCheck(json)
        .isDown()
        .hasOutcomeDown();
    }));
  }

  @Test
  public void testOnEventBus_KO_no_receiver(TestContext tc) {
    registerEventBusProcedure();

    healthChecks.checkStatus().map(CheckResult::toJson).onComplete(tc.asyncAssertSuccess(json -> {
      assertThatCheck(json)
        .isDown()
        .hasOutcomeDown();
    }));
  }
}
