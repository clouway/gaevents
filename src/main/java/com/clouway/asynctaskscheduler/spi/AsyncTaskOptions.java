package com.clouway.asynctaskscheduler.spi;

import com.google.common.collect.Maps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * AsyncTaskOptions is an options class which is used for holding of the optional information for the Task.
 *
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class AsyncTaskOptions {
  private Class<? extends AsyncTask> asyncTask;
  private Map<String, String> params;
  private long delayMills = 0;
  private long executionDateMills = 0;
  private AsyncEvent event;
  private String taskName;
  private Boolean transactionless = false;
  private String eventListener;
  private String eventHandler;


  private AsyncTaskOptions() {
  }

  public AsyncTaskOptions param(String name, String value) {
    if (name != null && value != null) {
      params.put(name, value);
    }
    return this;
  }

  public AsyncTaskOptions paramDate(String name, Date value) {
    if (name != null && value != null) {
      params.put(name, new SimpleDateFormat(AsyncTaskParams.DEFAULT_DATE_AND_TIME_PATTERN).format(value));
    }
    return this;
  }


  public <T> AsyncTaskOptions param(String name, T value, Class<? extends ParamFormat<T>> paramFormatClass) {
    if (name != null && value != null && paramFormatClass != null) {
      try {

        ParamFormat<T> paramFormat = paramFormatClass.newInstance();

        String valueAsString = paramFormat.format(value);

        params.put(name, valueAsString);

      } catch (InstantiationException e) {
        throw new IllegalArgumentException(e);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return this;
  }

  public static AsyncTaskOptions task(Class<? extends AsyncTask> asyncTaskClass) {
    AsyncTaskOptions taskOptions = new AsyncTaskOptions();
    taskOptions.asyncTask = asyncTaskClass;
    taskOptions.params = Maps.newHashMap();
    return taskOptions;

  }

  public static AsyncTaskOptions event(AsyncEvent event) {
    AsyncTaskOptions taskOptions = new AsyncTaskOptions();
    taskOptions.event = event;
    taskOptions.params = Maps.newHashMap();
    return taskOptions;
  }

  public AsyncTaskOptions delay(long delayMills) {
    this.delayMills = delayMills;
    executionDateMills = 0;
    return this;
  }

  public AsyncTaskOptions executionDate(Date executionDate) {
    delayMills = 0;
    this.executionDateMills = executionDate.getTime();
    return this;
  }

  public AsyncTaskOptions named(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public AsyncTaskOptions transactionless() {
    transactionless = true;
    return this;
  }

  public AsyncTaskOptions eventListener(String eventListener) {
    this.eventListener = eventListener;
    return this;
  }

  public AsyncTaskOptions eventHandler(String eventHandler) {
    this.eventHandler = eventHandler;
    return this;
  }

  public Class<? extends AsyncTask> getAsyncTask() {
    return asyncTask;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public long getDelayMills() {
    return delayMills;
  }

  public long getExecutionDateMills() {
    return executionDateMills;
  }

  public String getAsyncTaskAsString() {
    return asyncTask.getName();
  }

  public AsyncEvent getEvent() {
    return event;
  }

  public String getTaskName() {
    return taskName;
  }

  public Boolean isTransactionless() {
    return transactionless;
  }

  public boolean isEventTaskOption() {
    if (event != null) {
      return true;
    }
    return false;
  }

  public String getEventListener() {
    return eventListener;
  }

  public String getEventHandler() {
    return eventHandler;
  }
}
