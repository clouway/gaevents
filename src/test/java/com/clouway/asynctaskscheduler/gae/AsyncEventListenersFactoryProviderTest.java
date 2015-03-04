package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.common.ActionEvent;
import com.clouway.asynctaskscheduler.common.DefaultActionEvent;
import com.clouway.asynctaskscheduler.common.IndexingListener;
import com.clouway.asynctaskscheduler.common.TestEventListener;
import com.clouway.asynctaskscheduler.spi.AsyncEvent;
import com.clouway.asynctaskscheduler.spi.AsyncEventListener;
import com.clouway.asynctaskscheduler.spi.AsyncEventListenersFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
public class AsyncEventListenersFactoryProviderTest {

  private AsyncEventListenersFactory listenersFactory;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new BackgroundTasksModule(), new AbstractModule() {
      @Override
      protected void configure() {
        TypeLiteral<Class<? extends AsyncEvent>> eventType = new TypeLiteral<Class<? extends AsyncEvent>>(){};
        TypeLiteral<List<Class<? extends AsyncEventListener>>> eventListenersType = new TypeLiteral<List<Class<? extends AsyncEventListener>>>(){};

        MapBinder<Class<? extends AsyncEvent>, List<Class<? extends AsyncEventListener>>> mapBinder = MapBinder.newMapBinder(binder(), eventType, eventListenersType);

        List<Class<? extends AsyncEventListener>> listeners = new ArrayList<Class<? extends AsyncEventListener>>();
        listeners.add(IndexingListener.class);
        listeners.add(TestEventListener.class);

        mapBinder.addBinding(ActionEvent.class).toInstance(listeners);

        listeners = new ArrayList<Class<? extends AsyncEventListener>>();
        listeners.add(IndexingListener.class);
        mapBinder.addBinding(DefaultActionEvent.class).toInstance(listeners);
      }

    });

    Provider<AsyncEventListenersFactory> provider = injector.getProvider(AsyncEventListenersFactory.class);

    listenersFactory = provider.get();
  }

  @Test
  public void createListOfListeners() throws Exception {
    assertThat(listenersFactory.create(ActionEvent.class).size(), is(2));
    assertThat(listenersFactory.create(DefaultActionEvent.class).size(), is(1));
  }

  @Test
  public void createListenerByName() throws Exception {
    IndexingListener indexingListener = (IndexingListener) listenersFactory.createListener(ActionEvent.class, IndexingListener.class.getSimpleName());
    assertThat(indexingListener, notNullValue());

    TestEventListener testEventListener = (TestEventListener) listenersFactory.createListener(ActionEvent.class, TestEventListener.class.getSimpleName());
    assertThat(testEventListener, notNullValue());
  }
}
