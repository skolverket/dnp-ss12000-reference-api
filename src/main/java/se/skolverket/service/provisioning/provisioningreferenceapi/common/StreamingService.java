package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.buildPageToken;

@Slf4j
public abstract class StreamingService {
  public abstract Future<Void> getStream(WriteStream<Buffer> bufferWriteStream, JsonObject queryParams);

  protected Future<Void> streamProcessor(ReadStream<JsonObject> jsonObjectReadStream, WriteStream<Buffer> bufferWriteStream, JsonObject queryParams) {
    Promise<Void> endPromise = Promise.promise();
    AtomicBoolean firstObject = new AtomicBoolean(true);
    AtomicInteger objectCount = new AtomicInteger(0);
    AtomicReference<JsonObject> object = new AtomicReference<>();
    bufferWriteStream.write(Buffer.buffer("{ \"data\":["));
    jsonObjectReadStream.handler(bsonObject -> {
      objectCount.incrementAndGet();
      if (!firstObject.getAndSet(false)) {
        bufferWriteStream.write(Buffer.buffer(","));
      }
      object.set(DataType.fromBsonJson(bsonObject));
      bufferWriteStream.write(object.get().toBuffer());
      if (bufferWriteStream.writeQueueFull()) {
        jsonObjectReadStream.pause();
        bufferWriteStream.drainHandler(done -> jsonObjectReadStream.resume());
      }
    });
    jsonObjectReadStream.endHandler(v -> {
      String pageToken = buildPageToken(queryParams, objectCount.get(), object.get());
      if (pageToken == null || pageToken.isEmpty()) {
        bufferWriteStream.write(Buffer.buffer("]}"));
      } else {
        bufferWriteStream.write(Buffer.buffer("],\"pageToken\":\"%s\"}".formatted(pageToken)));
      }
      endPromise.complete();
    });
    jsonObjectReadStream.exceptionHandler(endPromise::fail);
    return endPromise.future();
  }
}
