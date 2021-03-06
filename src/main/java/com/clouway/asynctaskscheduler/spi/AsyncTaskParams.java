package com.clouway.asynctaskscheduler.spi;

import com.google.common.collect.Maps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Represents an object that consist the params, given for the {@link AsyncTask} execution.
 * Consist methods that can return the values of the parameters in different Types
 *
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public class AsyncTaskParams {

  public static final String DEFAULT_DATE_PATTERN = "dd-MM-yyyy";
  public static final String DEFAULT_DATE_AND_TIME_PATTERN = "dd-MM-yyyy HH:mm";

  /**
   * A builder method
   * @return
   */
  public static Builder aNewParams() {
    return new Builder();
  }

  public static class Builder {

    private Map<String, String[]> params = Maps.newHashMap();

    public Builder addDouble(String key, Double value) {
      if (value != null) {
        putParam(key, value.toString());
      }
      return this;
    }

    public Builder addLong(String key, Double value) {
      if (value != null) {
        putParam(key, value.toString());
      }
      return this;
    }


    public Builder addString(String key, String value) {
      if (value != null) {
        putParam(key, value);
      }
      return this;
    }

    public Builder addDate(String key, Date date) {
      putParam(key, new SimpleDateFormat(DEFAULT_DATE_PATTERN).format(date));
      return this;
    }

    public Builder addDateAndTime(String key, Date date) {
      putParam(key, new SimpleDateFormat(DEFAULT_DATE_AND_TIME_PATTERN).format(date));
      return this;
    }

    public AsyncTaskParams build() {
      return new AsyncTaskParams(params);
    }

    private void putParam(String key, String value) {
      params.put(key, array(value));
    }
  }


  private Map<String, String[]> params;

  public AsyncTaskParams(Map<String, String[]> params) {
    if (params == null) {
      throw new IllegalArgumentException("params map cannot be null");
    }
    this.params = params;
  }

  /**
   * Checks if the parameters contains parameter with given key
   *
   * @param key
   * @return true if contains or false otherwise
   */
  public boolean containsKey(String key) {
    return params.containsKey(key);
  }

  /**
   * Gets the value of the parameter with given key as String
   *
   * @param key the given key
   * @return the value of the parameter as String
   */
  public String getString(String key) {
    return findParam(key);
  }

  /**
   * Gets the value of the parameter with given key as Integer
   *
   * @param key the given key
   * @return the value of the parameter as Integer
   */
  public Integer getInteger(String key) {
    if (isEmpty(findParam(key))) {
      return null;
    }
    return Integer.parseInt(findParam(key));
  }

  /**
   * Gets the value of the parameter with given key as Double
   *
   * @param key the given key
   * @return the value of the parameter as Double
   */
  public Double getDouble(String key) {
    if (isEmpty(findParam(key))) {
      return null;
    }
    return Double.parseDouble(findParam(key));
  }


  /**
   * Gets the value of the parameter with given key as Long
   *
   * @param key the given key
   * @return the value of the parameter as Double
   */
  public Long getLong(String key) {
    if (isEmpty(findParam(key))) {
      return null;
    }
    return Long.valueOf(findParam(key));
  }

  /**
   * Gets the value of the parameter with the given as {@link Date} parsed by the DEFAULT_DATE_PATTERN
   *
   * @param key
   * @return
   * @throws ParseException
   */
  public Date getDate(String key) throws ParseException {
    if (isEmpty(findParam(key))) {
      return null;
    }
    return new SimpleDateFormat(DEFAULT_DATE_PATTERN).parse(findParam(key));

  }

  /**
   * Gets the value of the parameter with the given as {@link Date} parsed by the DEFAULT_DATE_AND_TIME_PATTERN
   *
   * @param key
   * @return
   * @throws ParseException
   */
  public Date getDateAndTime(String key) throws ParseException {
    if (isEmpty(findParam(key))) {
      return null;
    }
    return new SimpleDateFormat(DEFAULT_DATE_AND_TIME_PATTERN).parse(findParam(key));
  }

  /**
   * Formats the parameter( with the given key) using the given parse class
   *
   * @param key
   * @param formatClass
   * @param <T>
   * @return
   */
  public <T> T format(String key, Class<? extends ParamFormat<T>> formatClass) {
    if (isEmpty(findParam(key)) && formatClass != null) {
      return null;
    }

    try {
      ParamFormat<T> paramFormat = formatClass.newInstance();

      T t = paramFormat.parse(findParam(key));

      return t;

    } catch (InstantiationException e) {
      throw new IllegalArgumentException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }


  private static String[] array(String... array) {
    return array;
  }

  private String findParam(String key) {
    String[] values = params.get(key);
    if (values == null) {
      return null;
    } else {
      return params.get(key)[0];
    }
  }

  private boolean isEmpty(String value) {
    if (value == null || "".equals(value)) {
      return true;
    }
    return false;
  }
}
