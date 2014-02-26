package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.EventTransport;
import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public class GsonEventTransport implements EventTransport {

  private final Gson gson;

  @Inject
  public GsonEventTransport(Gson gson) {
    this.gson = gson;
  }

  @Override
  public <T> T in(Class<T> eventClass, String event) {
    return gson.fromJson(event, eventClass);
  }

  @Override
  public String out(Class<?> eventClass, Object event) {
    return gson.toJson(event, eventClass);
  }
}
