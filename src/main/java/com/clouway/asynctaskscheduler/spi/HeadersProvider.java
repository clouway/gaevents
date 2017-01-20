package com.clouway.asynctaskscheduler.spi;

import java.util.Map;

/**
 * @author Georgi Georgiev (georgi.georgiev@clouway.com)
 */
public interface HeadersProvider {

  Map<String, String> get();

}
