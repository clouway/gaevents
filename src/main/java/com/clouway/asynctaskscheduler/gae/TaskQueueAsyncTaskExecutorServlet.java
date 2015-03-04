package com.clouway.asynctaskscheduler.gae;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Represents the HttpServlet that is executed form the added Task Queues and executes the Job that they specify.
 *
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class TaskQueueAsyncTaskExecutorServlet extends HttpServlet {
  public static final String URL = "/worker/taskQueue";
  private final RoutingEventDispatcher eventDispatcher;
  private final RoutingTaskDispatcher taskDispatcher;

  @Inject
  public TaskQueueAsyncTaskExecutorServlet(RoutingEventDispatcher eventDispatcher,RoutingTaskDispatcher taskDispatcher) {
    this.eventDispatcher = eventDispatcher;
    this.taskDispatcher = taskDispatcher;
  }



  @Override
  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
    doPost(httpServletRequest, httpServletResponse);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try {

      String asyncTaskClass = getParameter(request, TaskQueueAsyncTaskScheduler.TASK_QUEUE);

      //event details
      String eventClassAsString = getParameter(request, TaskQueueAsyncTaskScheduler.EVENT);
      String eventAsJson = getParameter(request, TaskQueueAsyncTaskScheduler.EVENT_AS_JSON);
      //listener details
      String listenerClassAsString = getParameter(request, TaskQueueAsyncTaskScheduler.LISTENER);
       //handler details
      String handlerClassAsString = getParameter(request, TaskQueueAsyncTaskScheduler.HANDLER);

      dispatchAsyncEvent(eventClassAsString, eventAsJson, listenerClassAsString, handlerClassAsString);
      dispatchEventHandler(handlerClassAsString, eventClassAsString, eventAsJson);
      dispatchEventListener(listenerClassAsString, eventClassAsString, eventAsJson);
      dispatchAsyncTask(asyncTaskClass ,request);


    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

  }

  private void dispatchAsyncTask(String asyncTaskClass, HttpServletRequest request) throws ClassNotFoundException {
    if (!Strings.isNullOrEmpty(asyncTaskClass)) {
      Map<String, String[]> params = Maps.newHashMap(request.getParameterMap());

      //todo do not pass map here;
      taskDispatcher.dispatchAsyncTask(params, asyncTaskClass);
    }
  }

  private void dispatchAsyncEvent(String eventClassAsString, String eventAsJson, String listenerClassAsString, String handlerClassAsString) throws ClassNotFoundException {
    if (!Strings.isNullOrEmpty(eventClassAsString) && !Strings.isNullOrEmpty(eventAsJson) && Strings.isNullOrEmpty(handlerClassAsString) && Strings.isNullOrEmpty(listenerClassAsString)) {
      eventDispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);
    }
  }

  private void dispatchEventListener(String listenerClassAsString, String eventClassAsString, String eventAsJson) throws ClassNotFoundException {
    if (!Strings.isNullOrEmpty(listenerClassAsString) && !Strings.isNullOrEmpty(eventClassAsString) && !Strings.isNullOrEmpty(eventAsJson)) {
      eventDispatcher.dispatchEventListener(eventClassAsString, eventAsJson, listenerClassAsString);
    }
  }

  private void dispatchEventHandler(String handlerClassAsString, String eventClassAsString, String eventAsJson) throws ClassNotFoundException {
    if (!Strings.isNullOrEmpty(handlerClassAsString) && !Strings.isNullOrEmpty(eventClassAsString) && !Strings.isNullOrEmpty(eventAsJson)) {
      eventDispatcher.dispatchEventHandler(eventClassAsString, eventAsJson, handlerClassAsString);
    }
  }

  private String getParameter(HttpServletRequest request, String pramName) throws UnsupportedEncodingException {
    String param = request.getParameter(pramName);

    if (param != null) {

      String decodedParam = URLDecoder.decode(param, "UTF8");
      return decodedParam;

    }
    return null;
  }

}

