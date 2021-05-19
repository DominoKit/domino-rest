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
package org.dominokit.rest.test;

import static org.assertj.core.api.Java6Assertions.assertThat;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.FileUpload;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.dominokit.rest.model.MultipartTestServiceFactory;
import org.dominokit.rest.model.SampleMultipartRequest;
import org.dominokit.rest.model.SampleObject;
import org.dominokit.rest.shared.request.ByteArrayProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MultipartTest extends BaseRestTest {

  @Test
  void multipart_request_with_form_data_as_arguments(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        MultipartTestServiceFactory.INSTANCE.textMultipart(
            "sample id",
            ByteArrayProvider.of("test file content".getBytes(StandardCharsets.UTF_8))),
        routingContext -> {
          testContext.verify(
              () -> {
                Set<FileUpload> fileUploads = routingContext.fileUploads();
                MultiMap formAttributes = routingContext.request().formAttributes();
                assertThat(formAttributes.get("id")).isEqualTo("sample id");
                Assertions.assertThat(fileUploads.size()).isOne();
                for (FileUpload fileUpload : fileUploads) {
                  Buffer buffer =
                      vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
                  assertThat(buffer.toString()).isEqualTo("test file content");
                }
              });
          routingContext.response().end();
          testContext.completeNow();
        });
  }

  @Test
  void multipart_request_with_object_form_data_as_arguments(
      Vertx vertx, VertxTestContext testContext) {
    SampleObject sampleObject = new SampleObject();
    sampleObject.setId("sample id");
    sampleObject.setName("sample name");
    sendRequest(
        MultipartTestServiceFactory.INSTANCE.objectMultipart(
            sampleObject,
            ByteArrayProvider.of("test file content".getBytes(StandardCharsets.UTF_8))),
        routingContext -> {
          testContext.verify(
              () -> {
                Set<FileUpload> fileUploads = routingContext.fileUploads();
                MultiMap formAttributes = routingContext.request().formAttributes();
                SampleObject resultObject =
                    Json.decodeValue(formAttributes.get("sampleObjectJson"), SampleObject.class);
                assertThat(resultObject.getId()).isEqualTo("sample id");
                assertThat(resultObject.getName()).isEqualTo("sample name");
                Assertions.assertThat(fileUploads.size()).isOne();
                for (FileUpload fileUpload : fileUploads) {
                  Buffer buffer =
                      vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
                  assertThat(buffer.toString()).isEqualTo("test file content");
                }
              });
          routingContext.response().end();
          testContext.completeNow();
        });
  }

  @Test
  void multipart_request_with_wrapper_form_data_as_argument(
      Vertx vertx, VertxTestContext testContext) {
    SampleObject sampleObject = new SampleObject();
    sampleObject.setId("sample id");
    sampleObject.setName("sample name");
    SampleMultipartRequest request = new SampleMultipartRequest();
    request.setSampleObject(sampleObject);
    request.setSize(3);
    request.setFileContent("test file content".getBytes(StandardCharsets.UTF_8));
    sendRequest(
        MultipartTestServiceFactory.INSTANCE.wrapperMultipart(request),
        routingContext -> {
          testContext.verify(
              () -> {
                Set<FileUpload> fileUploads = routingContext.fileUploads();
                MultiMap formAttributes = routingContext.request().formAttributes();
                SampleObject resultObject =
                    Json.decodeValue(formAttributes.get("sampleObjectJson"), SampleObject.class);
                assertThat(resultObject.getId()).isEqualTo("sample id");
                assertThat(resultObject.getName()).isEqualTo("sample name");
                assertThat(formAttributes.get("size")).isEqualTo("3");
                Assertions.assertThat(fileUploads.size()).isOne();
                for (FileUpload fileUpload : fileUploads) {
                  Buffer buffer =
                      vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
                  assertThat(buffer.toString()).isEqualTo("test file content");
                }
              });
          routingContext.response().end();
          testContext.completeNow();
        });
  }
}
