/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:
        
    http://www.apache.org/licenses/LICENSE-2.0
        
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.bedework.dumprestore;

import org.bedework.calfacade.exc.CalFacadeException;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.ReplicationMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/** Convenience class to do the actual hibernate interaction. Intended for
 * one use only.
 *
 * @author Mike Douglass douglm@rpi.edu
 */
public class HibSession implements Serializable {
  transient Logger log;

  Session sess;
  transient Transaction tx;

  transient Query q;
  transient Criteria crit;

  /** Exception from this session. */
  Throwable exc;

  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /** Set up for a hibernate interaction. Throw the object away on exception.
   *
   * @param sessFactory
   * @param log
   * @throws CalFacadeException
   */
  public HibSession(SessionFactory sessFactory,
                    Logger log) throws CalFacadeException {
    try {
      this.log = log;
      sess = sessFactory.openSession();
      sess.setFlushMode(FlushMode.COMMIT);
//      tx = sess.beginTransaction();
    } catch (Throwable t) {
      exc = t;
      tx = null;  // not even started. Should be null anyway
      close();
    }
  }

  /**
   * @return boolean true if open
   * @throws CalFacadeException
   */
  public boolean isOpen() throws CalFacadeException {
    try {
      return sess.isOpen();
    } catch (Throwable t) {
      handleException(t);
      return false;
    }
  }

  /** Clear a session
   *
   * @throws CalFacadeException
   */
  public void clear() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.clear();
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Disconnect a session
   *
   * @throws CalFacadeException
   */
  public void disconnect() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      if (exc instanceof CalFacadeException) {
        throw (CalFacadeException)exc;
      }
      throw new CalFacadeException(exc);
    }

    try {
      sess.disconnect();
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** set the flushmode
   *
   * @param val
   * @throws CalFacadeException
   */
  public void setFlushMode(FlushMode val) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      if (tx != null) {
        throw new CalFacadeException("Transaction already started");
      }

      sess.setFlushMode(val);
    } catch (Throwable t) {
      exc = t;
      throw new CalFacadeException(t);
    }
  }

  /** Begin a transaction
   *
   * @throws CalFacadeException
   */
  public void beginTransaction() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      if (tx != null) {
        throw new CalFacadeException("Transaction already started");
      }

      tx = sess.beginTransaction();
      if (tx == null) {
        throw new CalFacadeException("Transaction not started");
      }
    } catch (Throwable t) {
      exc = t;
      throw new CalFacadeException(t);
    }
  }

  /** Return true if we have a transaction started
   *
   * @return boolean
   */
  public boolean transactionStarted() {
    return tx != null;
  }

  /** Commit a transaction
   *
   * @throws CalFacadeException
   */
  public void commit() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
//      if (tx != null &&
//          !tx.wasCommitted() &&
//          !tx.wasRolledBack()) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("About to commit");
        }
        tx.commit();
//      }

      tx = null;
    } catch (Throwable t) {
      exc = t;
      throw new CalFacadeException(t);
    }
  }

  /** Rollback a transaction
   *
   * @throws CalFacadeException
   */
  public void rollback() throws CalFacadeException {
/*    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }
*/
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Enter rollback");
    }
    try {
      if (tx != null &&
          !tx.wasCommitted() &&
          !tx.wasRolledBack()) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("About to rollback");
        }
        tx.rollback();
      }

      tx = null;
    } catch (Throwable t) {
      exc = t;
      throw new CalFacadeException(t);
    }
  }

  /** Create a Criteria ready for the additon of Criterion.
   *
   * @param cl           Class for criteria
   * @return Criteria    created Criteria
   * @throws CalFacadeException
   */
  public Criteria createCriteria(Class cl) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      crit = sess.createCriteria(cl);
      q = null;

      return crit;
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Evict an object from the session.
   *
   * @param val          Object to evict
   * @throws CalFacadeException
   */
  public void evict(Object val) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.evict(val);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Create a query ready for parameter replacement or execution.
   *
   * @param s             String hibernate query
   * @throws CalFacadeException
   */
  public void createQuery(String s) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q = sess.createQuery(s);
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @return query string
   * @throws CalFacadeException
   */
  public String getQueryString() throws CalFacadeException {
    if (q == null) {
      return "*** no query ***";
    }

    try {
      return q.getQueryString();
    } catch (Throwable t) {
      handleException(t);
      return null;
    }
  }

  /** Create a sql query ready for parameter replacement or execution.
   *
   * @param s             String hibernate query
   * @param returnAlias
   * @param returnClass
   * @throws CalFacadeException
   */
  public void createSQLQuery(String s, String returnAlias, Class returnClass)
        throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      SQLQuery sq = sess.createSQLQuery(s);
      sq.addEntity(returnAlias, returnClass);

      q = sq;
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Create a named query ready for parameter replacement or execution.
   *
   * @param name         String named query name
   * @throws CalFacadeException
   */
  public void namedQuery(String name) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q = sess.getNamedQuery(name);
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Mark the query as cacheable
   *
   * @throws CalFacadeException
   */
  public void cacheableQuery() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setCacheable(true);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      String parameter value
   * @throws CalFacadeException
   */
  public void setString(String parName, String parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setString(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Date parameter value
   * @throws CalFacadeException
   */
  public void setDate(String parName, Date parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      // Remove any time component
      synchronized (dateFormatter) {
        q.setDate(parName, java.sql.Date.valueOf(dateFormatter.format(parVal)));
      }
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      boolean parameter value
   * @throws CalFacadeException
   */
  public void setBool(String parName, boolean parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setBoolean(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      int parameter value
   * @throws CalFacadeException
   */
  public void setInt(String parName, int parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setInteger(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      long parameter value
   * @throws CalFacadeException
   */
  public void setLong(String parName, long parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setLong(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Object parameter value
   * @throws CalFacadeException
   */
  public void setEntity(String parName, Object parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setEntity(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Object parameter value
   * @throws CalFacadeException
   */
  public void setParameter(String parName, Object parVal) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      q.setParameter(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Return the single object resulting from the query.
   *
   * @return Object          retrieved object or null
   * @throws CalFacadeException
   */
  public Object getUnique() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      if (q != null) {
        return q.uniqueResult();
      }

      return crit.uniqueResult();
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return a list resulting from the query.
   *
   * @return List          list from query
   * @throws CalFacadeException
   */
  public List getList() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      List l;
      if (q != null) {
        l = q.list();
      } else {
        l = crit.list();
      }

      if (l == null) {
        return new ArrayList();
      }

      return l;
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /**
   * @return int number updated
   * @throws CalFacadeException
   */
  public int executeUpdate() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      if (q == null) {
        throw new CalFacadeException("No query for execute update");
      }

      return q.executeUpdate();
    } catch (Throwable t) {
      handleException(t);
      return 0;  // Don't get here
    }
  }

  /** Update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @throws CalFacadeException
   */
  public void update(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.update(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Merge and update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @return Object   the persiatent object
   * @throws CalFacadeException
   */
  public Object merge(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      return sess.merge(obj);
    } catch (Throwable t) {
      handleException(t);
      return null;
    }
  }

  /** Save a new object or update an object which may have been loaded in a
   * previous hibernate session
   *
   * @param obj
   * @throws CalFacadeException
   */
  public void saveOrUpdate(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.saveOrUpdate(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Copy the state of the given object onto the persistent object with the
   * same identifier. If there is no persistent instance currently associated
   * with the session, it will be loaded. Return the persistent instance.
   * If the given instance is unsaved or does not exist in the database,
   * save it and return it as a newly persistent instance. Otherwise, the
   * given instance does not become associated with the session.
   *
   * @param obj
   * @return Object
   * @throws CalFacadeException
   */
  public Object saveOrUpdateCopy(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      return sess.merge(obj);
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    A serializable key
   * @return Object
   * @throws CalFacadeException
   */
  public Object get(Class cl, Serializable id) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      return sess.get(cl, id);
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    int key
   * @return Object
   * @throws CalFacadeException
   */
  public Object get(Class cl, int id) throws CalFacadeException {
    return get(cl, new Integer(id));
  }

  /** Save a new object.
   *
   * @param obj
   * @throws CalFacadeException
   */
  public void save(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.save(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* * Save a new object with the given id. This should only be used for
   * restoring the db from a save or for assigned keys.
   *
   * @param obj
   * @param id
   * @throws CalFacadeException
   * /
  public void save(Object obj, Serializable id) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.save(obj, id);
    } catch (Throwable t) {
      handleException(t);
    }
  }*/

  /** Delete an object
   *
   * @param obj
   * @throws CalFacadeException
   */
  public void delete(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.delete(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Save a new object with the given id. This should only be used for
   * restoring the db from a save.
   *
   * @param obj
   * @throws CalFacadeException
   */
  public void restore(Object obj) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.replicate(obj, ReplicationMode.IGNORE);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @param o
   * @throws CalFacadeException
   */
  public void reAttach(Object o) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.lock(o, LockMode.NONE);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @param o
   * @throws CalFacadeException
   */
  public void lockRead(Object o) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.lock(o, LockMode.READ);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @param o
   * @throws CalFacadeException
   */
  public void lockUpdate(Object o) throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    try {
      sess.lock(o, LockMode.UPGRADE);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @throws CalFacadeException
   */
  public void flush() throws CalFacadeException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new CalFacadeException(exc);
    }

    if (getLogger().isDebugEnabled()) {
      getLogger().debug("About to flush");
    }
    try {
      sess.flush();
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @throws CalFacadeException
   */
  public void close() throws CalFacadeException {
    if (sess == null) {
      return;
    }

//    throw new CalFacadeException("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");/*
    try {
      if (sess.isDirty()) {
        sess.flush();
      }
      if (tx != null) {
        tx.commit();
      }
    } catch (Throwable t) {
      if (exc == null) {
        exc = t;
      }
    } finally {
      tx = null;
      if (sess != null) {
        try {
          sess.close();
        } catch (Throwable t) {}
      }
    }

    sess = null;
    if (exc != null) {
      throw new CalFacadeException(exc);
    }
//    */
  }

  private void handleException(Throwable t) throws CalFacadeException {
    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("handleException called");
        getLogger().error(this, t);
      }
    } catch (Throwable dummy) {}

    try {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (Throwable t1) {
          rollbackException(t1);
        }
        tx = null;
      }
    } finally {
      try {
        sess.close();
      } catch (Throwable t2) {}
      sess = null;
    }

    exc = t;
    throw new CalFacadeException(t);
  }

  /** This is just in case we want to report rollback exceptions. Seems we're
   * likely to get one.
   *
   * @param t   Throwable from the rollback
   */
  private void rollbackException(Throwable t) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("HibSession: ", t);
    }
    getLogger().error(this, t);
  }

  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
