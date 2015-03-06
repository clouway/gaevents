package com.clouway.asynctaskscheduler.spi;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
public class AsyncEventBusBinder {
  private Multibinder<Listener> binder;

  public interface Listener{
    Class<? extends AsyncEventListener> getListenerClass();
  }

  public AsyncEventBusBinder(Binder binder) {
     this.binder = Multibinder.newSetBinder(binder, Listener.class);
  }

  public AsyncEventBusBinder addBinding(final Class<? extends AsyncEventListener> listenerClass){
    binder.addBinding().toInstance(new Listener() {
      @Override
      public Class<? extends AsyncEventListener> getListenerClass() {
        return listenerClass;
      }
    });
    return this;
  }

}
