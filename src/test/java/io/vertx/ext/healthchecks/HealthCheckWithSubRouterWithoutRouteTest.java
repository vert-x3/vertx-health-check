package io.vertx.ext.healthchecks;

/**
 * Same as {@link HealthCheckTest} but using a health check handler mounter in a sub-router.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HealthCheckWithSubRouterWithoutRouteTest extends HealthCheckWithSubRouterTest {

  @Override
  protected String prefix() {
    return "/no-route";
  }

  @Override
  protected String route() {
    return "";
  }

  @Override
  public void testRetrievingALeaf() {
    super.testRetrievingALeaf();
  }
}
