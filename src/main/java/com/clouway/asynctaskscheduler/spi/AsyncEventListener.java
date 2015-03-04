package com.clouway.asynctaskscheduler.spi;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public interface AsyncEventListener<E extends AsyncEvent>{

  void onEvent(E event);
}
