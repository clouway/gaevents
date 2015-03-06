package com.clouway.asynctaskscheduler.common;

import com.clouway.asynctaskscheduler.spi.AsyncEventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tsony Tsonev (tsony.tsonev@clouway.com)
 */
public class MultyInterfaceListener implements AsyncEventListener<ActionEvent>, Serializable, Set<Integer>{
  @Override
  public void onEvent(ActionEvent event) {

  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Iterator<Integer> iterator() {
    return null;
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return null;
  }

  @Override
  public boolean add(Integer integer) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {

  }
}
