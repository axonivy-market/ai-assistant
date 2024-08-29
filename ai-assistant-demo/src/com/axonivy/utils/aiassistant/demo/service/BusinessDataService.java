package com.axonivy.utils.aiassistant.demo.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import ch.ivyteam.ivy.business.data.store.BusinessDataInfo;
import ch.ivyteam.ivy.business.data.store.BusinessDataRepository;
import ch.ivyteam.ivy.environment.Ivy;

public abstract class BusinessDataService<T> {

  public static final int LIMIT_1 = 1;
  public static final int LIMIT_10 = 10;
  public static final int LIMIT_20 = 20;
  public static final int LIMIT_100 = 100;

  public T findById(String id) {
    return repo().find(id, getType());
  }

  public BusinessDataInfo<T> save(T object) {
    if (object == null) {
      return null;
    }
    return repo().save(object);
  }

  public List<T> findAll() {
    return repo().search(getType()).limit(LIMIT_100).execute().getAll();
  }

  public void delete(String id) {
    T object = findById(id);
    if (object == null) {
      return;
    }
    repo().delete(object);
  }

  public abstract Class<T> getType();

  protected BusinessDataRepository repo() {
    return Ivy.repo();
  }

  protected void deleteAll() {
    List<T> data = findAll();
    if (CollectionUtils.isNotEmpty(data)) {
      for (T entity : data) {
        repo().delete(entity);
      }
    }
  }

}