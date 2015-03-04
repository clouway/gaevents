package com.clouway.asynctaskscheduler;

import com.clouway.asynctaskscheduler.gae.*;
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
        TaskQueueEventBusTest.class,
        AsyncEventListenersFactoryProviderTest.class
})
public class TestSuite {
}
