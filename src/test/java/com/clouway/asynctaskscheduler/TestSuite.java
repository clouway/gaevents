package com.clouway.asynctaskscheduler;

import com.clouway.asynctaskscheduler.gae.GsonEventSerializationTest;
import com.clouway.asynctaskscheduler.gae.RoutingEventDispatcherTest;
import com.clouway.asynctaskscheduler.gae.RoutingTaskDispatcherTest;
import com.clouway.asynctaskscheduler.gae.TaskQueueAsyncTaskExecutorServletTest;
import com.clouway.asynctaskscheduler.gae.TaskQueueAsyncTaskSchedulerErrorHandlingTest;
import com.clouway.asynctaskscheduler.gae.TaskQueueAsyncTaskSchedulerTest;
import com.clouway.asynctaskscheduler.gae.TaskQueueEventBusTest;
import com.clouway.asynctaskscheduler.spi.AsyncTaskParamsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AsyncTaskParamsTest.class,
        GsonEventSerializationTest.class,
        RoutingEventDispatcherTest.class,
        RoutingTaskDispatcherTest.class,
        TaskQueueAsyncTaskExecutorServletTest.class,
        TaskQueueAsyncTaskSchedulerErrorHandlingTest.class,
        TaskQueueAsyncTaskSchedulerTest.class,
        TaskQueueEventBusTest.class
})
public class TestSuite {
}
