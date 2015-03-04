package com.clouway.asynctaskscheduler.common;

import com.clouway.asynctaskscheduler.spi.AsyncEvent;
import com.clouway.asynctaskscheduler.spi.AsyncEventListener;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class IndexingListener implements AsyncEventListener<ActionEvent>{
  public AsyncEvent event;

  @Override
  public void onEvent(ActionEvent event) {
    this.event = event;
  }
}
