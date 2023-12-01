package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class StreamingService {
  public abstract Future<Void> getStream(WriteStream<Buffer> bufferWriteStream, JsonObject queryParams);

  protected Future<Void> streamProcessor(ReadStream<JsonObject> jsonObjectReadStream, WriteStream<Buffer> bufferWriteStream) {
    Promise<Void> endPromise = Promise.promise();
    AtomicBoolean firstObject = new AtomicBoolean(true);
    bufferWriteStream.write(Buffer.buffer("{ \"data\":["));
    jsonObjectReadStream.handler(bsonObject -> {
      if (!firstObject.getAndSet(false)) {
        bufferWriteStream.write(Buffer.buffer(","));
      }
      bufferWriteStream.write(DataType.fromBsonJson(bsonObject).toBuffer());
      if (bufferWriteStream.writeQueueFull()) {
        jsonObjectReadStream.pause();
        bufferWriteStream.drainHandler(done -> jsonObjectReadStream.resume());
      }
    });
    jsonObjectReadStream.endHandler(v -> {
      bufferWriteStream.write(Buffer.buffer("]}"));
      endPromise.complete();
    });
    jsonObjectReadStream.exceptionHandler(endPromise::fail);
    return endPromise.future();
  }
}
