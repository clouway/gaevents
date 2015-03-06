package com.clouway.asynctaskscheduler.spi;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
public class AsyncEventBusBinder {
  private Multibinder<ListenerClazz> binder;

  public interface ListenerClazz {
    Class<? extends AsyncEventListener> getValue();
  }

  public AsyncEventBusBinder(Binder binder) {
     this.binder = Multibinder.newSetBinder(binder, ListenerClazz.class);
  }

  public AsyncEventBusBinder registerListener(final Class<? extends AsyncEventListener> listenerClass){
    binder.addBinding().toInstance(new ListenerClazz() {
      @Override
      public Class<? extends AsyncEventListener> getValue() {
        return listenerClass;
      }
    });
    return this;
  }

}
