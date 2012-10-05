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

package org.bedework.dumprestore.restore.rules;

import org.bedework.calfacade.BwSystem;
import org.bedework.dumprestore.restore.RestoreGlobals;

import org.xml.sax.Attributes;

/**
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class SysparsRule extends EntityRule {
  /** Constructor
   *
   * @param globals
   */
  public SysparsRule(RestoreGlobals globals) {
    super(globals);
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.restore.rules.EntityRule#begin(java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void begin(String ns, String name, Attributes att) throws Exception {
    super.begin(ns, name, att);

    /* Use the global object. */
    pop();
    push(globals.getSyspars());
  }

  public void end(String ns, String name) throws Exception {
    BwSystem ent = (BwSystem)pop();

    /* Try to ensure any new fields have a value.
     */
    ent.setName(setDefault(ent.getName(), globals.defaultSyspars.getName()));
    ent.setTzid(setDefault(ent.getTzid(), globals.defaultSyspars.getTzid()));
    ent.setSystemid(setDefault(ent.getSystemid(),
                               globals.defaultSyspars.getSystemid()));

    ent.setPublicCalendarRoot(setDefault(ent.getPublicCalendarRoot(),
                                         globals.defaultSyspars.getPublicCalendarRoot()));
    ent.setUserCalendarRoot(setDefault(ent.getUserCalendarRoot(),
                                       globals.defaultSyspars.getUserCalendarRoot()));
    ent.setUserDefaultCalendar(setDefault(ent.getUserDefaultCalendar(),
                                          globals.defaultSyspars.getUserDefaultCalendar()));
    ent.setDefaultTrashCalendar(setDefault(ent.getDefaultTrashCalendar(),
                                           globals.defaultSyspars.getDefaultTrashCalendar()));
    ent.setUserInbox(setDefault(ent.getUserInbox(),
                                globals.defaultSyspars.getUserInbox()));
    ent.setUserOutbox(setDefault(ent.getUserOutbox(),
                                 globals.defaultSyspars.getUserOutbox()));
    ent.setDeletedCalendar(setDefault(ent.getDeletedCalendar(),
                                      globals.defaultSyspars.getDeletedCalendar()));
    ent.setBusyCalendar(setDefault(ent.getBusyCalendar(),
                                   globals.defaultSyspars.getBusyCalendar()));

    ent.setDefaultUserViewName(setDefault(ent.getDefaultUserViewName(),
                                          globals.defaultSyspars.getDefaultUserViewName()));

    ent.setPublicUser(setDefault(ent.getPublicUser(),
                                 globals.defaultSyspars.getPublicUser()));

    //ent.setHttpConnectionsPerUser(setDefault(globals.defaultSyspars.getHttpConnectionsPerUser());
    //ent.setHttpConnectionsPerHost(setDefault(globals.defaultSyspars.getHttpConnectionsPerHost());
    //ent.setHttpConnections(setDefault(globals.defaultSyspars.getHttpConnections());

    //ent.setMaxPublicDescriptionLength(intFld());
    //ent.setMaxUserDescriptionLength(intFld());
    //ent.setMaxUserEntitySize(intFld());
    //ent.setDefaultUserQuota(setDefault(globals.defaultSyspars.getDefaultUserQuota(),
    //                               longFld()));

    ent.setMaxInstances(setIntDefault(ent.getMaxInstances(),
                                      globals.defaultSyspars.getMaxInstances()));
    ent.setMaxYears(setIntDefault(ent.getMaxYears(),
                                  globals.defaultSyspars.getMaxYears()));

    /* FROM-VERSION 3.4 */
    if ("org.bedework.calcore.hibernate.UserAuthUWDbImpl".equals(ent.getUserauthClass())) {
      ent.setUserauthClass(globals.defaultSyspars.getUserauthClass());
    }

    /* FROM-VERSION 3.4 */
    if ("org.bedework.calcore.hibernate.AdminGroupsDbImpl".equals(ent.getAdmingroupsClass())) {
      ent.setAdmingroupsClass(globals.defaultSyspars.getAdmingroupsClass());
    }

    /* FROM-VERSION 3.4 */
    if ("org.bedework.calcore.hibernate.GroupsDbImpl".equals(ent.getUsergroupsClass())) {
      ent.setUsergroupsClass(globals.defaultSyspars.getUsergroupsClass());
    }

    ent.setUserauthClass(setDefault(ent.getUserauthClass(),
                                    globals.defaultSyspars.getUserauthClass()));
    ent.setMailerClass(setDefault(ent.getMailerClass(),
                                  globals.defaultSyspars.getMailerClass()));
    ent.setAdmingroupsClass(setDefault(ent.getAdmingroupsClass(),
                                       globals.defaultSyspars.getAdmingroupsClass()));
    ent.setUsergroupsClass(setDefault(ent.getUsergroupsClass(),
                                      globals.defaultSyspars.getUsergroupsClass()));

    //ent.setDirectoryBrowsingDisallowed(booleanFld());

    ent.setIndexRoot(setDefault(ent.getIndexRoot(),
                                globals.defaultSyspars.getIndexRoot()));

    try {
      if (globals.rintf != null) {
        globals.rintf.restoreSyspars(ent);
      }

      globals.setSyspars(ent);
    } catch (Throwable t) {
      throw new Exception(t);
    }
  }

  private String setDefault(String val, String def) {
    if (val != null) {
      return val;
    }

    return def;
  }

  private int setIntDefault(int val, int def) {
    if (val != 0) {
      return val;
    }

    return def;
  }
}
