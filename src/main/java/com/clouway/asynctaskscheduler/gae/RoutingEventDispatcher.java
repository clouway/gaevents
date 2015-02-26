package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class RoutingEventDispatcher {


  private final EventTransport eventTransport;
  private final AsyncEventHandlerFactory handlerFactory;
  private final AsyncEventListenersFactory listenersFactory;
  private final Provider<AsyncTaskScheduler> taskScheduler;

  @Inject
  public RoutingEventDispatcher(EventTransport eventTransport,
                                AsyncEventHandlerFactory handlerFactory,
                                AsyncEventListenersFactory listenersFactory,
                                Provider<AsyncTaskScheduler> taskScheduler) {
    this.eventTransport = eventTransport;
    this.handlerFactory = handlerFactory;
    this.listenersFactory = listenersFactory;
    this.taskScheduler = taskScheduler;
  }

  /**
   * @param eventClassAsString
   * @param eventAsJson
   * @throws ClassNotFoundException
   */
  public void dispatchAsyncEvent(String eventClassAsString, String eventAsJson) throws ClassNotFoundException {

    if (Strings.isNullOrEmpty(eventClassAsString) || Strings.isNullOrEmpty(eventAsJson)) {
      throw new IllegalArgumentException("No AsyncEvent class as string or evnt as json provided.");
    }

    Class<?> eventClass = Class.forName(eventClassAsString);

    if (!Arrays.asList(eventClass.getInterfaces()).contains(AsyncEvent.class)) {
      throw new IllegalArgumentException("No AsyncEvent class provided.");
    }

    AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClass);

    Class<? extends AsyncEventHandler> evenHandlerClass = event.getAssociatedHandlerClass();

    //1.
    dispatchHandler(event, evenHandlerClass);
    //2.
    dispatchListeners(event);
  }

  /**
   * @param eventClassAsString
   * @param eventAsJson
   * @param listenerId
   * @throws ClassNotFoundException
   */
  public void dispatchEventListener(String eventClassAsString, String eventAsJson, int listenerId) throws ClassNotFoundException {
    if (Strings.isNullOrEmpty(eventClassAsString) || Strings.isNullOrEmpty(eventAsJson)) {
      throw new IllegalArgumentException("No AsyncEvent class as string or evnt as json provided.");
    }

    Class<?> eventClass = Class.forName(eventClassAsString);

    if (!Arrays.asList(eventClass.getInterfaces()).contains(AsyncEvent.class)) {
      throw new IllegalArgumentException("No AsyncEvent class provided.");
    }

    AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClass);

    List<? extends AsyncEventListener> listeners  = listenersFactory.create(event.getClass());
    AsyncEventListener listener = listeners.get(listenerId);

    listener.onEvent(event);
  }

  private AsyncEvent<AsyncEventHandler> getAsyncEvent(String eventAsJson, Class<?> eventClass) {

    ByteArrayInputStream inputStream = null;

    try {
      inputStream = new ByteArrayInputStream(eventAsJson.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    AsyncEvent<AsyncEventHandler> event = (AsyncEvent) eventTransport.in(eventClass, inputStream);

    try {

      if (inputStream != null) {
        inputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return event;
  }

  private void dispatchListeners(AsyncEvent<AsyncEventHandler> event) {
    List<? extends AsyncEventListener> listeners  = listenersFactory.create(event.getClass());
    int id = 0;
    for (AsyncEventListener listener : listeners) {
      taskScheduler.get().add(AsyncTaskOptions.event(event).eventListenerId(id)).now();
      id++;
    }
  }

  /**
   * Dsipatches the event to it's handler
   * @param event
   * @param evenHandlerClass
   */
  private void dispatchHandler(AsyncEvent<AsyncEventHandler> event, Class<? extends AsyncEventHandler> evenHandlerClass) {
    AsyncEventHandler handler = handlerFactory.create(evenHandlerClass);

    event.dispatch(handler);
  }




}
