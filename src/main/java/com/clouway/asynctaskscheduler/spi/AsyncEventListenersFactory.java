package com.clouway.asynctaskscheduler.spi;

import java.util.List;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public interface AsyncEventListenersFactory {
  AsyncEventListener createListener(Class<? extends AsyncEvent> eventClass, String eventListenerClassName);
  List<Class<? extends AsyncEventListener>> getListenerClasses(Class<? extends AsyncEvent> eventClass);
}
