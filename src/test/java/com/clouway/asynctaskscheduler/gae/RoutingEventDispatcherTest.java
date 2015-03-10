package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.common.ActionEvent;
import com.clouway.asynctaskscheduler.common.ActionEventHandler;
import com.clouway.asynctaskscheduler.common.IndexingListener;
import com.clouway.asynctaskscheduler.common.TaskQueueParamParser;
import com.clouway.asynctaskscheduler.common.TestEventListener;
import com.clouway.asynctaskscheduler.spi.AsyncEvent;
import com.clouway.asynctaskscheduler.spi.AsyncEventHandler;
import com.clouway.asynctaskscheduler.spi.AsyncEventHandlerFactory;
import com.clouway.asynctaskscheduler.spi.AsyncEventListener;
import com.clouway.asynctaskscheduler.spi.AsyncEventListenersFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class RoutingEventDispatcherTest {
  @Inject
  private RoutingEventDispatcher dispatcher;

  @Inject
  private Gson gson;

  private ActionEventHandler handler = new ActionEventHandler();
  private AsyncEventHandlerFactory handlerFactory = new AsyncEventHandlerFactory() {
    @Override
    public AsyncEventHandler create(Class<? extends AsyncEventHandler> evenHandlerClass) {
      return handler;
    }
  };

  IndexingListener indexingListener = new IndexingListener();
  TestEventListener testEventListener = new TestEventListener();
  private AsyncEventListenersFactory listenersFactory = new AsyncEventListenersFactory() {
    @Override
    public AsyncEventListener createListener(Class<? extends AsyncEvent> eventClass, String eventListenerClassName) {
      return testEventListener;
    }

    @Override
    public List<Class<? extends AsyncEventListener>> getListenerClasses(Class<? extends AsyncEvent> eventClass) {
      List<Class<? extends AsyncEventListener>> listeners = new ArrayList<Class<? extends AsyncEventListener>>();
      listeners.add(IndexingListener.class);
      listeners.add(TestEventListener.class);
      return listeners;
    }
  };

  private String eventClassAsString = ActionEvent.class.getName();
  private ActionEvent event = new ActionEvent("test message");
  private String eventAsJson;

  private LocalServiceTestHelper helper;

  @Before
  public void setUp() throws Exception {
    LocalTaskQueueTestConfig localTaskQueueTestConfig = new LocalTaskQueueTestConfig();
    localTaskQueueTestConfig.setQueueXmlPath("src/test/java/queue.xml");
    //todo make datastoreRule
    helper = new LocalServiceTestHelper(localTaskQueueTestConfig,new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy().setStoreDelayMs(0));
    helper.setUp();
    Injector injector = Guice.createInjector(new BackgroundTasksModule() {
      @Override
      public AsyncEventHandlerFactory getAsyncEventHandlerFactory(Injector injector) {
        return handlerFactory;
      }

      @Override
      public AsyncEventListenersFactory getAsyncEventListenersFactory(Injector injector) {
        return listenersFactory;
      }
    });
    injector.injectMembers(this);

    eventAsJson = gson.toJson(event);

  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  @Test
  public void shouldDispatchAsyncEvent() throws Exception {
    dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);

    QueueStateInfo qsi = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());

    //three task queues fired because the event has 2 listeners and 1 handler
    assertThat(qsi.getCountTasks(), is(3));
  }

  @Test
  public void dispatchAsyncEventInSameTaskQueueIfNoListeners() throws Exception {
    listenersFactory = new AsyncEventListenersFactory() {
      @Override
      public AsyncEventListener createListener(Class<? extends AsyncEvent> eventClass, String eventListenerClassName) {
        return null;
      }

      @Override
      public List<Class<? extends AsyncEventListener>> getListenerClasses(Class<? extends AsyncEvent> eventClass) {
        return Lists.newArrayList();
      }
    };

    //new injector so the listenersFactory can be initiated without the listeners
    Injector injector = Guice.createInjector(new BackgroundTasksModule() {
      @Override
      public AsyncEventHandlerFactory getAsyncEventHandlerFactory(Injector injector) {
        return handlerFactory;
      }

      @Override
      public AsyncEventListenersFactory getAsyncEventListenersFactory(Injector injector) {
        return listenersFactory;
      }
    });
    injector.injectMembers(this);

    dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);

    assertEquals(event.getMessage(), handler.message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotDispatchAsyncEventWhenEventClassNull() throws Exception {
    dispatcher.dispatchAsyncEvent(null, eventAsJson);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotDispatchAsyncEventWhenEventAsJsonIsNull() throws Exception {
    dispatcher.dispatchAsyncEvent(eventClassAsString, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotDispatchAsyncEventWhenEventClassDoesNotRepresentAsyncEvent() throws Exception {
    eventClassAsString = RoutingEventDispatcherTest.class.getName();
    dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);
  }

  @Test(expected = ClassNotFoundException.class)
  public void shouldNotDispatchAsyncEventWhenEventClassDoesNotRepresentAClass() throws Exception {
    eventClassAsString = "blabla";
    dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);
  }

  @Test
  public void shouldExecuteAllConfiguredListernerWhenEventWasReceived() throws Exception {

    dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);

    QueueStateInfo qsi = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());

    //two task queues fired because the event has 2 listeners
    assertThat(qsi.getCountTasks(), is(3));
    //each fired task queue contains the event class and the id of the listener to be executed
    //this test may fail from time to time due to tasks in the queue not executed in the given order, soo only the ids are wrong, happens very rarely
    assertParams(qsi.getTaskInfo().get(0).getBody(), TaskQueueAsyncTaskScheduler.EVENT, event.getClass().getCanonicalName());//get canonical name because the event class can be only retrieved with the full packaging
    assertParams(qsi.getTaskInfo().get(1).getBody(), TaskQueueAsyncTaskScheduler.LISTENER, indexingListener.getClass().getSimpleName());
    assertParams(qsi.getTaskInfo().get(2).getBody(), TaskQueueAsyncTaskScheduler.LISTENER, testEventListener.getClass().getSimpleName());
  }

  @Test
  public void shouldDispatchEventListener() throws Exception {
    //execute event listeners by their id, the id is received as task queue parameter
    dispatcher.dispatchEventListener(eventClassAsString, eventAsJson, testEventListener.getClass().getSimpleName());

    assertEquals(event.getMessage(), ((ActionEvent) testEventListener.event).getMessage());
  }

  @Test
  public void shouldDispatchEventHandler() throws Exception {
    //execute event listeners by their id, the id is received as task queue parameter
    dispatcher.dispatchEventHandler(eventClassAsString, eventAsJson, event.getAssociatedHandlerClass().getSimpleName());

    assertEquals(event.getMessage(), handler.message);
  }

  private QueueStateInfo getQueueStateInfo(String queueName) {
    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    return ltq.getQueueStateInfo().get(queueName);
  }

  private void assertParams(String taskQueueBody, String paramName, String paramValue) throws UnsupportedEncodingException {
    Map<String, String> params = TaskQueueParamParser.parse(taskQueueBody);
    assertEquals(params.get(paramName), paramValue);
  }
}
