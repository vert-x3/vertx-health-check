package io.vertx.ext.healthchecks;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.ext.healthchecks.Status}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.healthchecks.Status} original class using Vert.x codegen.
 */
public class StatusConverter implements JsonCodec<Status, JsonObject> {

  public static final StatusConverter INSTANCE = new StatusConverter();

  @Override public JsonObject encode(Status value) { return (value != null) ? value.toJson() : null; }

  @Override public Status decode(JsonObject value) { return (value != null) ? new Status(value) : null; }

  @Override public Class<Status> getTargetClass() { return Status.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Status obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "data":
          if (member.getValue() instanceof JsonObject) {
            obj.setData(((JsonObject)member.getValue()).copy());
          }
          break;
        case "ok":
          if (member.getValue() instanceof Boolean) {
            obj.setOk((Boolean)member.getValue());
          }
          break;
        case "procedureInError":
          if (member.getValue() instanceof Boolean) {
            obj.setProcedureInError((Boolean)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(Status obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Status obj, java.util.Map<String, Object> json) {
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    json.put("ok", obj.isOk());
    json.put("procedureInError", obj.isProcedureInError());
  }
}
