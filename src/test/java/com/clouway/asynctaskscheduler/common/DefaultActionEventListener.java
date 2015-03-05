package com.clouway.asynctaskscheduler.common;

import com.clouway.asynctaskscheduler.gae.QueueName;
import com.clouway.asynctaskscheduler.spi.AsyncEventListener;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
@QueueName(name = "customListenerTaskQueue")
public class DefaultActionEventListener implements AsyncEventListener<ActionEvent>{
  public static final String CUSTOM_LISTENER_TASK_QUEUE_NAME = "customListenerTaskQueue";
  public ActionEvent actionEvent;

  @Override
  public void onEvent(ActionEvent event) {
    this.actionEvent = event;
  }
}
