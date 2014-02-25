package com.clouway.asynctaskscheduler.spi;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface EventTransport {

  <T> T in(Class<T> eventClass, String event);

  String out(Class<?> eventClass, Object event);
}
