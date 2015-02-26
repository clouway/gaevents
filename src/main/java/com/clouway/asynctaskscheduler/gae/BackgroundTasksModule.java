package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.servlet.ServletModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class BackgroundTasksModule extends AbstractModule {

  final Module servletsModule = new ServletModule() {
    @Override
    protected void configureServlets() {
      serve(TaskQueueAsyncTaskExecutorServlet.URL).with(TaskQueueAsyncTaskExecutorServlet.class);
      bind(TaskQueueAsyncTaskExecutorServlet.class).in(Singleton.class);
    }
  };

  @Override
  protected void configure() {
    install(servletsModule);
    bind(EventTransport.class).to(getEventTransport()).in(Singleton.class);
  }

  protected Class<? extends EventTransport> getEventTransport() {
    return GsonEventTransport.class;
  }

  @Provides
  public AsyncEventBus getAsyncEventBus(Provider<AsyncTaskScheduler> asyncTaskScheduler) {
    return new TaskQueueEventBus(asyncTaskScheduler);
  }

  @Provides
  public AsyncTaskScheduler getAsyncTaskScheduler(EventTransport eventTransport, Provider<CommonParamBinder> commonParamBinderProvider, TaskApplier taskApplier) {
    return new TaskQueueAsyncTaskScheduler(eventTransport, commonParamBinderProvider.get(), taskApplier);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof BackgroundTasksModule;
  }

  @Override
  public int hashCode() {
    return BackgroundTasksModule.class.hashCode();
  }

  @Provides
  public AsyncEventHandlerFactory getAsyncEventHandlerFactory(final Injector injector) {
    return new AsyncEventHandlerFactory() {
      @Override
      public AsyncEventHandler create(Class<? extends AsyncEventHandler> evenHandlerClass) {
        return injector.getInstance(evenHandlerClass);
      }
    };
  }

  @Provides
  public AsyncEventListenersFactory getAsyncEventListenerFactory(final Injector injector) {
    return new AsyncEventListenersFactory() {
      @Override
      public List<AsyncEventListener> create(Class<? extends AsyncEvent> eventClass) {

        TypeLiteral<Map<Class<? extends AsyncEvent>, List<Class<? extends AsyncEventListener>>>> mapOfEventListeners = new TypeLiteral<Map<Class<? extends AsyncEvent>, List<Class<? extends AsyncEventListener>>>>() {};
        Map<Class<? extends AsyncEvent>, List<Class<? extends AsyncEventListener>>> map;
        map = injector.getInstance(Key.get(mapOfEventListeners));

        ArrayList<AsyncEventListener> listeners = Lists.newArrayList();

        List<Class<? extends AsyncEventListener>> listenerClassList = map.get(eventClass);
        if (listenerClassList != null) {
          for (Class<? extends AsyncEventListener> listenerClass : listenerClassList) {
            AsyncEventListener listener = injector.getInstance(listenerClass);
            listeners.add(listener);
          }
        }
        return listeners;
      }
    };
  }

}