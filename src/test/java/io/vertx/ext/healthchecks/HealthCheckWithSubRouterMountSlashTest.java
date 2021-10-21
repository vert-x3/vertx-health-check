/*
 *  Copyright (c) 2011-2021 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.healthchecks;

/**
 * Same as {@link HealthCheckWithSubRouterTest} but using a sub router mount point as: '/'.
 *
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
public class HealthCheckWithSubRouterMountSlashTest extends HealthCheckWithSubRouterTest {

  @Override
  protected String prefix() {
    return "/";
  }

  @Override
  public void testWithEmptySuccessfulCheck() {
    super.testWithEmptySuccessfulCheck();
  }

  @Override
  public void testWithExplicitSuccessfulCheckAndData() {
    super.testWithExplicitSuccessfulCheckAndData();
  }

  @Override
  public void testWithExplicitFailedCheckAndData() {
    super.testWithExplicitFailedCheckAndData();
  }

  @Override
  public void testWithOneSuccessfulAndOneFailedCheck() {
    super.testWithOneSuccessfulAndOneFailedCheck();
  }

  @Override
  public void testWithTwoFailedChecks() {
    super.testWithTwoFailedChecks();
  }

  @Override
  public void testWithTwoSucceededChecks() {
    super.testWithTwoSucceededChecks();
  }

  @Override
  public void testWithNestedCompositeThatSucceed() {
    super.testWithNestedCompositeThatSucceed();
  }

  @Override
  public void testWithNestedCompositeThatFailed() {
    super.testWithNestedCompositeThatFailed();
  }

  @Override
  public void testRetrievingAComposite() {
    super.testRetrievingAComposite();
  }

  @Override
  public void testRetrievingALeaf() {
    super.testRetrievingALeaf();
  }

  @Override
  public void testACheckThatTimeOut() {
    super.testACheckThatTimeOut();
  }

  @Override
  public void testACheckThatFail() {
    super.testACheckThatFail();
  }

  @Override
  public void testACheckThatTimeOutButSucceed() {
    super.testACheckThatTimeOutButSucceed();
  }

  @Override
  public void testACheckThatTimeOutButFailed() {
    super.testACheckThatTimeOutButFailed();
  }

  @Override
  public void testRemovingComposite() {
    super.testRemovingComposite();
  }

  @Override
  public void testRemovingLeaf() {
    super.testRemovingLeaf();
  }
}
