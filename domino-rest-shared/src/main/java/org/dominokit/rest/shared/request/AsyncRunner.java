/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.shared.request;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The runner that runs the async operation according to the environment, this runner runs {@link
 * AsyncTask} and call its {@link AsyncTask#onSuccess()} if the async operation went successfully,
 * calling {@link AsyncTask#onFailed(Throwable)} otherwise.
 */
@FunctionalInterface
public interface AsyncRunner {

  /** The logger for {@link AsyncRunner}. */
  Logger LOGGER = Logger.getLogger(AsyncRunner.class.getName());

  /**
   * This represents the async task, the {@link AsyncTask#onSuccess()} will be called if the async
   * task ran successfully, {@link AsyncTask#onFailed(Throwable)} will be called if something went
   * wrong
   */
  interface AsyncTask {
    /** Called when the async task completes successfully. */
    void onSuccess();

    /**
     * Called when the async task fails.
     *
     * @param error the error that occurred
     */
    default void onFailed(Throwable error) {
      LOGGER.log(Level.SEVERE, "Failed to run async task : ", error);
    }
  }

  /**
   * Runs this task in an async mode
   *
   * @param asyncTask the task to run
   */
  void runAsync(AsyncTask asyncTask);
}
