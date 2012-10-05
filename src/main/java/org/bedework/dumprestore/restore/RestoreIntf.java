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
package org.bedework.dumprestore.restore;

import org.bedework.calcorei.HibSession;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.BwUser;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.prefs.BwPreferences;

/** Interface which defines the database functions needed to restore the
 * calendar database. The methods need to be called in the order defined
 * below.
 *
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public interface RestoreIntf {
  /**
   * @param globals
   * @throws Throwable
   */
  public void init(RestoreGlobals globals) throws Throwable;

  /**
   * @return Session
   * @throws CalFacadeException
   */
  public HibSession getSession() throws CalFacadeException;

  /** Call after any init phase
   *
   * @throws Throwable
   */
  public void open() throws Throwable;

  /** Call to start a transaction
   *
   * @throws Throwable
   */
  public void startTransaction() throws Throwable;

  /** Call to end a transaction - even if batched
   *
   * @throws Throwable
   */
  public void endTransactionNow() throws Throwable;

  /** Call to end a transaction - may be ignored if batching
   *
   * @throws Throwable
   */
  public void endTransaction() throws Throwable;

  /** Call at end of restoring to finish up. Will restore any cached values.
   *
   * @throws Throwable
   */
  public void close() throws Throwable;

  /** Restore system pars
   *
   * @param o
   * @throws Throwable
   */
  public void restoreSyspars(BwSystem o) throws Throwable;

  /** Update system pars
   *
   * @param o
   * @throws Throwable
   */
  public void updateSyspars(BwSystem o) throws Throwable;

  /** Restore user
   *
   * @param o
   * @throws Throwable
   */
  public void restoreUser(BwUser o) throws Throwable;

  /** Restore attendee
   *
   * @param o
   * @throws Throwable
   */
  public void restoreAttendee(BwAttendee o) throws Throwable;

  /** Restore an admin group - though not the user entries nor
   * the authuser entries.
   *
   * @param o   Object to restore
   * @throws Throwable
   */
  public void restoreAdminGroup(BwAdminGroup o) throws Throwable;

  /** Update an admin group
   *
   * @param o   Object to update
   * @throws Throwable
   */
  public void updateAdminGroup(BwAdminGroup o) throws Throwable;

  /**
   * @param o
   * @param pr
   * @throws Throwable
   */
  public void addAdminGroupMember(BwAdminGroup o, BwPrincipal pr) throws Throwable;

  /** Get an admin group given it's name.
   *
   * @param name     String name of the group
   * @return BwAdminGroup
   * @throws Throwable
   */
  public BwAdminGroup getAdminGroup(String name) throws Throwable;

  /** Restore an auth user and preferences
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreAuthUser(BwAuthUser o) throws Throwable;

  /** Restore an event and associated entries
   *
   * @param ei   Object to restore with id set
   * @throws Throwable
   */
  public void restoreEvent(EventInfo ei) throws Throwable;

  /** Get an event
   *
   * @param user - the current user we are acting as - eg, for an annotation and
   *           fetching the master event this will be the annotation owner.
   * @param colPath
   * @param recurrenceId
   * @param uid
   * @return BwEvent
   * @throws Throwable
   */
  public BwEvent getEvent(BwUser user,
                          String colPath,
                          String recurrenceId,
                          String uid) throws Throwable;

  /** See if an event name is in the given calendar
   *
   * @param cal
   * @param name
   * @return boolean
   * @throws Throwable
   */
  public boolean eventNameExists(BwCalendar cal, String name) throws Throwable;

  /** Update an event
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void update(BwEvent o) throws Throwable;

  /** Restore category
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreCategory(BwCategory o) throws Throwable;

  /** Restore calendar suite
   *
   * @param o   Object to restore
   * @throws Throwable
   */
  public void restoreCalSuite(BwCalSuite o) throws Throwable;

  /** Restore location
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreLocation(BwLocation o) throws Throwable;

  /** Restore contact
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreContact(BwContact o) throws Throwable;

  /** Restore filter
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreFilter(BwFilterDef o) throws Throwable;

  /** Restore user prefs
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   */
  public void restoreUserPrefs(BwPreferences o) throws Throwable;

  /* * Restore alarm - not needed - restored as part of event
   *
   * @param o   Object to restore with id set
   * @throws Throwable
   * /
  public void restoreAlarm(BwAlarm o) throws Throwable;
  */

  /** Update user.
   *
   * @param user  Object to restore with id set
   * @throws Throwable
   */
  public void update(BwUser user) throws Throwable;

  /**
   * @param path
   * @return BwCalendar
   * @throws Throwable
   */
  public BwCalendar getCalendar(String path) throws Throwable;

  /**
   * @param key - uid + owner
   * @return BwCategory
   * @throws Throwable
   */
  public BwCategory getCategory(OwnerUidKey key) throws Throwable;

  /**
   * @param uid
   * @return BwCategory
   * @throws Throwable
   */
  public BwCategory getCategory(String uid) throws Throwable;

  /** PRE3.5
   *
   * @param key - uid + owner
   * @return BwContact
   * @throws Throwable
   */
  public BwContact getContact(OwnerUidKey key) throws Throwable;

  /**
   * @param uid
   * @return BwContact
   * @throws Throwable
   */
  public BwContact getContact(String uid) throws Throwable;

  /** PRE3.5
   * @param key - uid + owner
   * @return BwLocation
   * @throws Throwable
   */
  public BwLocation getLocation(OwnerUidKey key) throws Throwable;

  /**
   * @param uid
   * @return BwLocation
   * @throws Throwable
   */
  public BwLocation getLocation(String uid) throws Throwable;

  /**
   * @param account
   * @return BwUser
   * @throws Throwable
   */
  public BwUser getUser(String account) throws Throwable;

  /** Save a single root calendar - no parent is set in the entity
   *
   * @param val
   * @throws Throwable
   */
  public void saveRootCalendar(BwCalendar val) throws Throwable;

  /** Restore a single calendar - parent is set in the entity
   *
   * @param val
   * @throws Throwable
   */
  public void addCalendar(BwCalendar val) throws Throwable;

  /** Get the user home collection for the given user
   *
   * @param user
   * @return String path
   * @throws Throwable
   */
  public String getUserHome(BwUser user) throws Throwable;

  /**
   * @param val
   * @param parentPath
   * @throws Throwable
   */
  public void addCalendar(BwCalendar val,
                          String parentPath) throws Throwable;
}

