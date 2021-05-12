package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.*;
import com.clouway.asynctaskscheduler.spi.AsyncEventBusBinder.ListenerClazz;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;


/**
 * Listener can be registered in modules as it follows:
 * new AsyncEventBusBinder(binder())
 *       .registerListener(ContractSignedEventListener.class)
 *       .registerListener(ContractChangedEventListener.class);
 * These bindings can be added in different modules for different events or
 * for already added events. Single event can have more than one listeners
 * Same listener can be bound only once to event, even if tried to be added
 * multiple times exception wont be thrown.
 *
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
    bind(HeadersProvider.class).to(getHeadersProvider()).in(Singleton.class);
  }

  protected Class<? extends EventTransport> getEventTransport() {
    return GsonEventTransport.class;
  }

  protected Class<? extends HeadersProvider> getHeadersProvider() {
    return DefaultHeadersProvider.class;
  }

  @Provides
  public AsyncEventBus getAsyncEventBus(Provider<AsyncTaskScheduler> asyncTaskScheduler) {
    return new TaskQueueEventBus(asyncTaskScheduler);
  }

  @Provides
  public AsyncTaskScheduler getAsyncTaskScheduler(EventTransport eventTransport, Provider<CommonParamBinder> commonParamBinderProvider, TaskApplier taskApplier, HeadersProvider headersProvider) {
    return new TaskQueueAsyncTaskScheduler(eventTransport, commonParamBinderProvider.get(), taskApplier, headersProvider);
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
        TypeLiteral<Set<ListenerClazz>> listenersKey = new TypeLiteral<Set<ListenerClazz>>() {};
        Set<ListenerClazz> listenerClazzs = injector.getInstance(Key.get(listenersKey));

        List<Class<? extends AsyncEventListener>> listenerClasses = Lists.newArrayList();

        for (ListenerClazz listenerClazz : listenerClazzs){
          Class<? extends AsyncEvent> genericEventClass = getListenerEventClass(listenerClazz);

          if (genericEventClass != null && genericEventClass.equals(eventClass)){
            if (!listenerClasses.contains(listenerClazz.getValue())){
              listenerClasses.add(listenerClazz.getValue());
            }
          }
        }

        return  listenerClasses;
      }

      private Class<? extends AsyncEvent> getListenerEventClass(ListenerClazz listenerClazz) {
        Type[] paramTypes = listenerClazz.getValue().getGenericInterfaces();

        ParameterizedType listenerType = null;
        for(Type type : paramTypes){
          if(type instanceof ParameterizedType &&  ((ParameterizedType) type).getRawType().equals(AsyncEventListener.class)){
            listenerType = (ParameterizedType) type;
          }
        }

        if(listenerType != null){
          return (Class<? extends AsyncEvent>) listenerType.getActualTypeArguments()[0]; //0 because we have only 1 generic type for listeners
        }

        return null;
      }
    };
  }

}