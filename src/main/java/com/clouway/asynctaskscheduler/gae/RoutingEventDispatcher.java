package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
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
   * Dispatches asyncevent. Basically fires separate task queue for the handler and for the listeners
   * @param eventClassAsString
   * @param eventAsJson
   * @throws ClassNotFoundException
   */
  public void dispatchAsyncEvent(String eventClassAsString, String eventAsJson) throws ClassNotFoundException {
    if (validParams(eventClassAsString, eventAsJson)) {

      AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClassAsString);

      Class<? extends AsyncEventHandler> evenHandlerClass = event.getAssociatedHandlerClass();

      dispatchHandlerAndListeners(event, evenHandlerClass);
    }
  }

  /**
   * Dispatches single event listener
   * @param eventClassAsString
   * @param eventAsJson
   * @param listenerClassName
   * @throws ClassNotFoundException
   */
  public void dispatchEventListener(String eventClassAsString, String eventAsJson, String listenerClassName) throws ClassNotFoundException {
    if (validParams(eventClassAsString, eventAsJson, listenerClassName)) {

      AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClassAsString);

      AsyncEventListener<AsyncEvent> listener  = listenersFactory.createListener(event.getClass(), listenerClassName);

      listener.onEvent(event);
    }
  }

  /**
   * Dispatches the event handler
   * @param eventClassAsString
   * @param eventAsJson
   * @param evenHandlerClassName
   * @throws ClassNotFoundException
   */
  public void dispatchEventHandler(String eventClassAsString, String eventAsJson, String evenHandlerClassName) throws ClassNotFoundException {
    if (validParams(eventClassAsString, eventAsJson, evenHandlerClassName)) {

      AsyncEvent<AsyncEventHandler> event = getAsyncEvent(eventAsJson, eventClassAsString);

      AsyncEventHandler handler = handlerFactory.create(event.getAssociatedHandlerClass());

      event.dispatch(handler);
    }
  }

  /**
   * Returns valid async event object if valid parameters are provided
   * @param eventAsJson
   * @param eventClassAsString
   * @return
   * @throws ClassNotFoundException
   */
  private AsyncEvent<AsyncEventHandler> getAsyncEvent(String eventAsJson, String eventClassAsString) throws ClassNotFoundException {
    Class<?> eventClass = Class.forName(eventClassAsString);

    AsyncEvent<AsyncEventHandler> event = null;

    if (validEvent(eventClass)) {
      ByteArrayInputStream inputStream = null;

      try {
        inputStream = new ByteArrayInputStream(eventAsJson.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      event = (AsyncEvent) eventTransport.in(eventClass, inputStream);

      try {

        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return event;
  }


  /**
   * Dispatches the given event handler and the listeners in separate task queue in datastore transaction if there are no listeners for the event
   * else dispatches the handler in the same task queue
   * @param event
   * @param evenHandlerClass
   */
  private void dispatchHandlerAndListeners(AsyncEvent<AsyncEventHandler> event, Class<? extends AsyncEventHandler> evenHandlerClass) {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Transaction txn = null;
    try {
      txn = ds.beginTransaction();

      List<Class<? extends AsyncEventListener>> listeners  = listenersFactory.getListenerClasses(event.getClass());

      if (listeners.isEmpty()) {
        AsyncEventHandler handler = handlerFactory.create(evenHandlerClass);
        event.dispatch(handler);
      } else {
        AsyncTaskScheduler asyncTaskScheduler = taskScheduler.get();

        asyncTaskScheduler.add(AsyncTaskOptions.eventWithHandler(event, evenHandlerClass));

        for (Class<? extends AsyncEventListener> listener : listeners) {
          asyncTaskScheduler.add(AsyncTaskOptions.eventWithListener(event, listener));
        }

        asyncTaskScheduler.now();
      }

      txn.commit();
    } catch (Exception e) {
      if (txn != null) {
        txn.rollback();
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Validates if event class is implementing AsyncEvent interface
   * @param eventClass
   * @return
   */
  private boolean validEvent(Class<?> eventClass){
    if (!Arrays.asList(eventClass.getInterfaces()).contains(AsyncEvent.class)) {
      throw new IllegalArgumentException("The Provided Class Is Not AsyncEvent.");
    }

    return true;
  }

  /**
   * Validates if provided parameters are null or empty
   * @param params
   * @return
   */
  private boolean validParams(String... params){

    for (String param : params){
      if(Strings.isNullOrEmpty(param)){
        throw new IllegalArgumentException("Illegal parameter provided.");
      }
    }

    return true;
  }


}
