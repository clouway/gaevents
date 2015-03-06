package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
import com.clouway.asynctaskscheduler.spi.AsyncEventBusBinder.Listener;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.servlet.ServletModule;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;


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
  public AsyncEventListenersFactory getAsyncEventListenersFactory(final Injector injector) {
    return new AsyncEventListenersFactory() {
      @Override
      public AsyncEventListener createListener(Class<? extends AsyncEvent> eventClass, String eventListenerClassName) {
        List<Class<? extends AsyncEventListener>> listeners = getListenerClasses(eventClass);
        for (Class<? extends AsyncEventListener> listener : listeners){
          if(eventListenerClassName.equals(listener.getSimpleName())){
            return injector.getInstance(listener);
          }
        }
        return null;
      }

      @Override
      public List<Class<? extends AsyncEventListener>> getListenerClasses(Class<? extends AsyncEvent> eventClass) {
        TypeLiteral<Set<Listener>> listenersKey = new TypeLiteral<Set<Listener>>() {};
        Set<Listener> listeners = injector.getInstance(Key.get(listenersKey));

        List<Class<? extends AsyncEventListener>> listenerClasses = Lists.newArrayList();

        for (Listener listener : listeners){
          Class<? extends AsyncEvent> genericEventClass = getListenerEventClass(listener);

          if (genericEventClass.equals(eventClass)){
            if (!listenerClasses.contains(listener.getListenerClass())){
              listenerClasses.add(listener.getListenerClass());
            }
          }
        }

        return  listenerClasses;
      }

      private Class<? extends AsyncEvent> getListenerEventClass(Listener listener) {
        Type[] paramTypes = listener.getListenerClass().getGenericInterfaces();

        ParameterizedType listenerType = null;
        for(Type type : paramTypes){
          if(((ParameterizedType) type).getRawType().equals(AsyncEventListener.class)){
            listenerType = (ParameterizedType) type;
          }
        }
        return (Class<? extends AsyncEvent>) listenerType.getActualTypeArguments()[0]; //0 because we have only 1 generic type for listeners
      }
    };
  }

}