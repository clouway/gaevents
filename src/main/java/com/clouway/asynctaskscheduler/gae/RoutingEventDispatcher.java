package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
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

    dispatchHandlerAndListeners(event, evenHandlerClass);
  }

  /**
   * @param eventClassAsString
   * @param eventAsJson
   * @param listenerClassAsString
   * @throws ClassNotFoundException
   */
  public void dispatchEventListener(String eventClassAsString, String eventAsJson, String listenerClassAsString) throws ClassNotFoundException {
    if (Strings.isNullOrEmpty(eventClassAsString) || Strings.isNullOrEmpty(eventAsJson)) {
      throw new IllegalArgumentException("No AsyncEvent class as string or evnt as json provided.");
    }

    Class<?> eventClass = Class.forName(eventClassAsString);

    if (!Arrays.asList(eventClass.getInterfaces()).contains(AsyncEvent.class)) {
      throw new IllegalArgumentException("No AsyncEvent class provided.");
    }

    AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClass);

    AsyncEventListener listener  = listenersFactory.createListener((Class<? extends AsyncEventListener>) Class.forName(listenerClassAsString));

    listener.onEvent(event);
  }

  public void dispatchEventHandler(String eventClassAsString, String eventAsJson, String evenHandlerClassAsString) throws ClassNotFoundException {
    if (Strings.isNullOrEmpty(eventClassAsString) || Strings.isNullOrEmpty(eventAsJson)) {
      throw new IllegalArgumentException("No AsyncEvent class as string or evnt as json provided.");
    }

    Class<?> eventClass = Class.forName(eventClassAsString);

    if (!Arrays.asList(eventClass.getInterfaces()).contains(AsyncEvent.class)) {
      throw new IllegalArgumentException("No AsyncEvent class provided.");
    }

    AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClass);

    AsyncEventHandler handler = handlerFactory.create((Class<? extends AsyncEventHandler>) Class.forName(evenHandlerClassAsString));

    event.dispatch(handler);
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


  /**
   * Dsipatches the event to it's handler
   * @param event
   * @param evenHandlerClass
   */
  private void dispatchHandlerAndListeners(AsyncEvent<AsyncEventHandler> event, Class<? extends AsyncEventHandler> evenHandlerClass) {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Transaction txn = null;
    try {
      txn = ds.beginTransaction();

      List<? extends AsyncEventListener> listeners  = listenersFactory.create(event.getClass());
      AsyncTaskScheduler asyncTaskScheduler = taskScheduler.get();

      asyncTaskScheduler.add(AsyncTaskOptions.event(event).eventHandler(evenHandlerClass.getCanonicalName()));

      for (AsyncEventListener listener : listeners) {
        asyncTaskScheduler.add(AsyncTaskOptions.event(event).eventListener(listener.getClass().getCanonicalName()));
      }

      asyncTaskScheduler.now();

      txn.commit();
    } catch (DatastoreFailureException e) {
      if (txn != null) {
        txn.rollback();
      }
    }
  }




}
