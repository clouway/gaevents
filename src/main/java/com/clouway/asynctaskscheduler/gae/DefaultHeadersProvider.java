package com.clouway.asynctaskscheduler.gae;

import com.clouway.asynctaskscheduler.spi.HeadersProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Georgi Georgiev (georgi.georgiev@clouway.com)
 */
public class DefaultHeadersProvider implements HeadersProvider {
  @Override
  public Map<String, String> get() {
    return new HashMap<String, String>();
  }
}
