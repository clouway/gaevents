package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.common.ActionEvent;
import com.clouway.asynctaskscheduler.common.IndexingListener;
import com.clouway.asynctaskscheduler.common.TestEventListener;
import com.clouway.asynctaskscheduler.spi.AsyncEventBusBinder;
import com.clouway.asynctaskscheduler.spi.AsyncEventListenersFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;

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
        new AsyncEventBusBinder(binder())
                .addBinding(IndexingListener.class);

      }
    }, new AbstractModule() {
      @Override
      protected void configure() {
        new AsyncEventBusBinder(binder())
                .addBinding(TestEventListener.class);
      }
    });

    Provider<AsyncEventListenersFactory> provider = injector.getProvider(AsyncEventListenersFactory.class);

    listenersFactory = provider.get();
  }

  @Test
  public void createListOfListeners() throws Exception {
    assertThat(listenersFactory.getListenerClasses(ActionEvent.class).size(), is(2));
  }

  @Test
  public void createListenerByName() throws Exception {
    IndexingListener indexingListener = (IndexingListener) listenersFactory.createListener(ActionEvent.class, IndexingListener.class.getSimpleName());
    assertThat(indexingListener, notNullValue());

    TestEventListener testEventListener = (TestEventListener) listenersFactory.createListener(ActionEvent.class, TestEventListener.class.getSimpleName());
    assertThat(testEventListener, notNullValue());
  }

  @Test
  public void pretendToNotExecuteSameListenerEvenIfAddedMoreThanOce() throws Exception {
    Injector injector = Guice.createInjector(new BackgroundTasksModule(), new AbstractModule() {
      @Override
      protected void configure() {
        new AsyncEventBusBinder(binder())
                .addBinding(IndexingListener.class)
                .addBinding(TestEventListener.class)
                .addBinding(TestEventListener.class)
                .addBinding(TestEventListener.class);
      }
    });

    listenersFactory = injector.getProvider(AsyncEventListenersFactory.class).get();

    assertThat(listenersFactory.getListenerClasses(ActionEvent.class).size(), is(2));
  }
}
