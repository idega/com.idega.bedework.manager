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

import org.bedework.calcore.hibernate.CalintfHelperHib;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEventAnnotation;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.BwUser;
import org.bedework.calfacade.configs.DumpRestoreConfig;
import org.bedework.calfacade.configs.SystemRoots;
import org.bedework.calfacade.env.CalOptionsFactory;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.dumprestore.Counters;
import org.bedework.dumprestore.ExternalSubInfo;

import edu.rpi.cmt.access.WhoDefs;
import edu.rpi.cmt.timezones.Timezones;

import org.apache.commons.digester.Digester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Globals for the restore phase
 *
 * @author Mike Douglass   douglm   rpi.edu
 * @version 1.0
 */
public class RestoreGlobals extends Counters {
  /* ********************************************************************
   * Properties of the dump, version date etc.
   * ******************************************************************** */

  /** undefined version numbers */
  public final static int bedeworkVersionDefault = -1;

  /** When this changes - everything is different */
  public int bedeworkMajorVersion = bedeworkVersionDefault;

  /** When this changes - schema and api usually have significant changes */
  public int bedeworkMinorVersion = bedeworkVersionDefault;

  /** Minor functional updates */
  public int bedeworkUpdateVersion = bedeworkVersionDefault;

  /** Patches which might introduce schema incompatibility if needed.
   * Essentially a bug fix
   */
  public String bedeworkPatchLevel = null;

  /** Result of concatenating the above */
  public String bedeworkVersion;

  /** Date from dump data */
  public String dumpDate;

  /* ********************************************************************
   * Elimination of private timezones
   * ******************************************************************** */

  /** true if we are converting private timezones to public
   */
  public boolean eliminatePrivateTimezones = true;

  /** Accumulate unmatched ids */
  public Set<String> unmatchedTzids = new TreeSet<String>();

  /** */
  public long convertedTzids;

  /** */
  public long discardedTzs;

  /**
   * The digester.
   */
  public Digester digester;

  /* ********************************************************************
   * Restore flags and variables.
   * ******************************************************************** */

  /** If we are converting we either convert all or the default calendar only.
   */
  public boolean convertScheduleDefault;

  /** Try to batch up entities */
  public int hibBatchSize = 0;

  /** Number in batch */
  public int curHibBatchSize = 0;

  /** This is not the way to use the digester. We could possibly build the xml
   * rules directly from the hibernate schema or from java annotations.
   *
   * For the moment I just need to get this going.
   */
  public boolean inOwnerKey;

  /** True if we skip creation of special calendars. This is for the conversion
   * from 3.0 which may have a lot of empty special calendars created
   */
  public boolean skipSpecialCals;

  /** Set false at start of entity, set true on entity error
   */
  public boolean entityError;

  /** Config properties from options file.
   */
  public DumpRestoreConfig config;

  /** Map user with id of zero on to this id - fixes oversight */
  public static int mapUser0 = 1;

  /** System parameters object */
  private BwSystem syspars = new BwSystem();

  /** System parameters object */
  public BwSystem defaultSyspars = new BwSystem();

  /** Messages for subscriptions changed   // PRE3.5
   */
  public ArrayList<String> subscriptionFixes = new ArrayList<String>();

  /** User entry for owner of public entities. This is used to fix up entries.
   */
  private BwUser publicUser;

  /** Incremented if we can't map something */
  public int calMapErrors = 0;

  /** If true stop restore on any error, otherwise just flag it.
   */
  public boolean failOnError = false;

  /** Incremented for each start datetime but end date. We drop the end.
   */
  public int fixedNoEndTime;

  /** counter */
  public int errors;

  /** counter */
  public int warnings;

  /** So we can output messages on the jmx console */
  public static class RestoreMessage {
    /** log4j level */
    public String level;

    /** The message */
    public String msg;

    RestoreMessage(final String level, final String msg) {
      this.level = level;
      this.msg = msg;
    }

    @Override
    public String toString() {
      return level + ": " + msg;
    }
  }

  /** Messages we output */
  public static class RestoreMessages extends ArrayList<RestoreMessage> {
    /** Add a warning
     * @param msg
     */
    public void warningMessage(final String msg) {
      add(new RestoreMessage(" WARN", msg));
    }

    /** Add an error
     * @param msg
     */
    public void errorMessage(final String msg) {
      add(new RestoreMessage("ERROR", msg));
    }
  }

  /**
   */
  public RestoreMessages messages = new RestoreMessages();

  /** Only Users mapping */
  public OnlyUsersMap onlyUsersMap = new OnlyUsersMap();

  /**
   */
  public static class EventKeyMap extends HashMap<Integer, ArrayList<Integer>> {
    /**
     * @param keyid
     * @param eventid
     */
    public void put(final int keyid, final int eventid) {
      ArrayList<Integer> al = get(keyid);
      if (al == null) {
        al = new ArrayList<Integer>();
        put(keyid, al);
      }

      al.add(eventid);
    }
  }

  /** Save ids of alias events and their targets
   */
  public static class AliasMap extends HashMap<Integer, Integer> {
    /**
     * @param val
     */
    public void put(final BwEventAnnotation val) {
      put(val.getId(), val.getTarget().getId());
    }
  }

  /**
   */
  public class UserMap extends HashMap<Integer, BwUser> {
    HashMap<String, BwUser> nameMap = new HashMap<String, BwUser>();

    /**
     * @param val
     */
    public void put(final BwUser val) {
      int id = val.getId();
      if (id ==0) {
        id = mapUser0;
        val.setId(id);
      }

      if (get(id) != null) {
        throw new RuntimeException("User already in table with id " + id);
      }
      put(id, val);
      nameMap.put(val.getAccount(), val);

      if (val.getAccount().equals(getSyspars().getPublicUser())) {
        setPublicUser(val);
      }
    }

    /**
     * @param id
     * @return BwUser
     */
    public BwUser getMapped(int id) {
      if (id ==0) {
        id = mapUser0;
      }
      return get(id);
    }

    /**
     * @param account
     * @return BwUser
     */
    public BwUser get(final String account) {
      return nameMap.get(account);
    }

    /**
     * @return collection from names table.
     */
    public Collection<BwUser> nameValues() {
      return nameMap.values();
    }
  }

  /**
   */
  public static class CalendarMap extends HashMap<String, BwCalendar> {
    /**
     * @param val
     */
    public void put(final BwCalendar val) {
      put(val.getPath(), val);
    }
  }

  /** */
  // PRE3.3
  public static class EventPropertyKey {
    /** */
    public String uid;
    /** */
    public BwUser owner;
    /** */
    public BwString keystr;

    EventPropertyKey(final String uid, final BwUser owner) {
      this.uid = uid;
      this.owner = owner;
    }

    EventPropertyKey(final BwString keystr, final BwUser owner) {
      this.keystr = keystr;
      this.owner = owner;
    }
  }

  /**
   */
  public static class CategoryMap extends HashMap<Integer, OwnerUidKey> {
    /**
     * @param val
     */
    public void put(final BwCategory val) {
      put(val.getId(), new OwnerUidKey(val.getOwnerHref(), val.getUid()));
    }
  }

  /** PRE3.3 - here we had the location db id in the event
   */
  public static class LocationMap extends HashMap<Integer, String> {
    /**
     * @param val
     */
    public void put(final BwLocation val) {
      put(val.getId(), val.getUid());
    }
  }

  /** PRE3.5 - here we had the owner and location uid in the event.
   * If we have to remap a location we put an entry in here.
   */
  public static class LocationUidMap extends HashMap<OwnerUidKey, String> {
    /**
     * @param val
     */
    public void put(final BwLocation val) {
      put(new OwnerUidKey(val.getOwnerHref(), val.getUid()), val.getUid());
    }
  }

  /** PRE3.3
   */
  public static class ContactMap extends HashMap<Integer, String> {
    /**
     * @param val
     */
    public void put(final BwContact val) {
      put(val.getId(), val.getUid());
    }
  }

  /** PRE3.5
   */
  public static class ContactUidMap extends HashMap<OwnerUidKey, String> {
    /**
     * @param val
     */
    public void put(final BwContact val) {
      put(new OwnerUidKey(val.getOwnerHref(), val.getUid()), val.getUid());
    }
  }

  /**
   */
  public static class AttendeeMap extends HashMap<Integer, BwAttendee> {
    /**
     * @param val
     */
    public void put(final BwAttendee val) {
      put(val.getId(), val);
    }
  }

  /** PRE3.5
   * Because it's not a db entity any more
   */
  public static class OrganizerEntity {
    /** */
    public int id;
    /** */
    public BwOrganizer organizer = new BwOrganizer();
  }

  /**
   */
  public static class OrganizerMap extends HashMap<Integer, OrganizerEntity> {
    /**
     * @param val
     */
    public void put(final OrganizerEntity val) {
      put(val.id, val);
    }
  }

  /** */
  public SubscriptionsMap subscriptionsTbl = new SubscriptionsMap();

  /** Collections marked as external subscriptions. We may need to resubscribe
   */
  public List<ExternalSubInfo> externalSubs = new ArrayList<ExternalSubInfo>();

  /** PRE3.1? */
  public UserMap usersTbl = new UserMap();

  /** */
  public PrincipalMap principalsTbl = new PrincipalMap();

  /** Members to add to admin groups */
  public HashMap<String, ArrayList<PrincipalHref>> adminGroupMembers =
    new HashMap<String, ArrayList<PrincipalHref>>();

  /** */
  public CategoryMap categoriesTbl = new CategoryMap();
  /** */
  public LocationMap locationsTbl = new LocationMap();
  /** */
  public LocationUidMap locationsUidTbl = new LocationUidMap();
  /** */
  public ContactMap contactsTbl = new ContactMap();
  /** */
  public ContactUidMap contactsUidTbl = new ContactUidMap();
  /** */
  public OrganizerMap organizersTbl = new OrganizerMap();
  /** */
  public AttendeeMap attendeesTbl = new AttendeeMap();

  /** */
  public CalendarMap calendarsTbl = new CalendarMap();

  //private BwIndexer publicIndexer;
  //private IndexerMap userIndexers;

  /** */
  public RestoreIntf rintf;

  RestoreGlobals() throws Throwable {
    calCallback = new CalintfHelperCallback(this);
  }

  private String defaultTzid;

  /** This must be called after syspars has been initialised.
   *
   * @throws Throwable
   */
  public void setTimezones() throws Throwable {
    if (syspars.getTzid() == null) {
      // Not enough info yet
      return;
    }

    if ((defaultTzid != null) &&
       (defaultTzid.equals(syspars.getTzid()))) {
      // Already set
      return;
    }

    String tzserverUri = CalOptionsFactory.getOptions().
                     getGlobalStringProperty("timezonesUri");

    if (tzserverUri == null) {
      throw new CalFacadeException("No timezones server URI defined");
    }

    Timezones.initTimezones(tzserverUri);

    Timezones.setSystemDefaultTzid(syspars.getTzid());
  }

  /**
   * @param val
   */
  public void setPublicUser(final BwUser val) {
    publicUser = val;
  }

  /** Get the account which owns public entities
   *
   * @return BwUser account
   * @throws Throwable if account name not defined
   */
  public BwUser getPublicUser() throws Throwable {
    return publicUser;
  }

  /** Who we are pretending to be for the core classes
   */
  public BwUser currentUser;

  /**
   */
  public CalintfHelperCallback calCallback;

  /**
   * @param val
   * @throws Throwable
   */
  public void setSyspars(final BwSystem val) throws Throwable {
    syspars = val;
    setTimezones();
  }

  /**
   * @return BwSystem
   */
  public BwSystem getSyspars() {
    return syspars;
  }

  private Collection<String> rootUsers;

  /**
   * @return collection string
   * @throws Exception
   */
  public Collection<String> getRootUsers() throws Exception {
    if (rootUsers != null) {
      return rootUsers;
    }

    rootUsers = new ArrayList<String>();

    String rus = getSyspars().getRootUsers();

    if (rus == null) {
      return rootUsers;
    }

    try {
      int pos = 0;

      while (pos < rus.length()) {
        int nextPos = rus.indexOf(",", pos);
        if (nextPos < 0) {
          rootUsers.add(rus.substring(pos));
          break;
        }

        rootUsers.add(rus.substring(pos, nextPos));
        pos = nextPos + 1;
      }
    } catch (Throwable t) {
      throw new Exception(t);
    }

    return rootUsers;
  }

  /**
   * @param val
   * @throws Exception
   */
  public void addRootUser(final String val) throws Exception {
    Collection<String> rus = getRootUsers();

    if (rus.contains(val)) {
      return;
    }

    String srus = getSyspars().getRootUsers();
    StringBuilder sb;
    if (srus == null) {
      sb = new StringBuilder();
    } else {
      sb = new StringBuilder(srus);
      sb.append(",");
    }

    sb.append(val);

    getSyspars().setRootUsers(sb.toString());
    try {
      rintf.updateSyspars(getSyspars());
    } catch (Throwable t) {
      throw new Exception(t);
    }
  }

  private static class CalintfHelperCallback implements CalintfHelperHib.Callback {
    private RestoreGlobals gbls;

    CalintfHelperCallback(final RestoreGlobals gbls) {
      this.gbls = gbls;
    }

    @Override
    public void rollback() throws CalFacadeException {
      gbls.rintf.getSession().rollback();
    }

    @Override
    public BwSystem getSyspars() throws CalFacadeException {
      return gbls.syspars;
    }

    @Override
    public BwUser getUser() throws CalFacadeException {
      return gbls.currentUser;
    }

    @Override
    public boolean getSuperUser() throws CalFacadeException {
      return true;
    }
  }

  /**
   *
   */
  @Override
  public void stats(final List<String> infoLines) {
    if (!subscriptionFixes.isEmpty()) {
      for (String m: subscriptionFixes) {
        info(infoLines, m);
      }

      info(infoLines, " ");
    }

    if (messages.size() > 0) {
      info(infoLines, "Errors and warnings. See log for details. ");
      info(infoLines, " ");

      for (RestoreMessage rm: messages) {
        info(infoLines, rm.toString());
      }

      info(infoLines, " ");
    }

    if (!unmatchedTzids.isEmpty()) {
      info(infoLines, "    Unmatched timezone ids: " + unmatchedTzids.size());
      for (String tzid: unmatchedTzids) {
        StringBuilder sb = new StringBuilder();

        sb.append(tzid);

        info(infoLines, sb.toString());
      }
    } else {
      info(infoLines, "    No unmatched timezone ids");
    }

    info(infoLines, " ");
    info(infoLines, "    Converted tzids: " + convertedTzids);
    info(infoLines, "    Discarded tzs: " + discardedTzs);

    super.stats(infoLines);

    info(infoLines, " ");
    info(infoLines, "    Fixed end times: " + fixedNoEndTime);
    info(infoLines, " ");
    info(infoLines, "           warnings: " + warnings);
    info(infoLines, "             errors: " + errors);
    info(infoLines, " ");
  }

  /**
   * @param config
   */
  public void init(final DumpRestoreConfig config) {
    this.config = config;
  }

  private Map<String, BwPrincipal> principalMap = new HashMap<String, BwPrincipal>();

  private long lastFlush = System.currentTimeMillis();
  private static final long flushInt = 1000 * 30 * 5; // 5 minutes

  private static String principalRoot;
  private static String userPrincipalRoot;
  private static String groupPrincipalRoot;
  private static String bwadmingroupPrincipalRoot;

  //private static int principalRootLen;
  private static int userPrincipalRootLen;
  //private static int groupPrincipalRootLen;

  private static void setRoots() throws Throwable {
    if (principalRoot != null) {
      return;
    }

    SystemRoots sysRoots = (SystemRoots)CalOptionsFactory.getOptions().getGlobalProperty("systemRoots");

    principalRoot = setRoot(sysRoots.getPrincipalRoot());
    userPrincipalRoot = setRoot(sysRoots.getUserPrincipalRoot());
    groupPrincipalRoot = setRoot(sysRoots.getGroupPrincipalRoot());
    bwadmingroupPrincipalRoot = setRoot(sysRoots.getBwadmingroupPrincipalRoot());

    //principalRootLen = principalRoot.length();
    userPrincipalRootLen = userPrincipalRoot.length();
    //groupPrincipalRootLen = groupPrincipalRoot.length();
  }

  /**
   * @param val
   * @return String
   */
  public static String setRoot(final String val) {
    if (val.endsWith("/")) {
      return val;
    }

    return val + "/";
  }

  private BwPrincipal mappedPrincipal(final String val) {
    long now = System.currentTimeMillis();

    if ((now - lastFlush) > flushInt) {
      principalMap.clear();
      lastFlush = now;
      return null;
    }

    return principalMap.get(val);
  }

  /**
   * @return principal root
   * @throws Throwable
   */
  public static String getUserPrincipalRoot() throws Throwable {
    setRoots();

    return userPrincipalRoot;
  }

  /**
   * @return principal root
   * @throws Throwable
   */
  public static String getGroupPrincipalRoot() throws Throwable {
    setRoots();

    return groupPrincipalRoot;
  }

  /**
   * @return principal root
   * @throws Throwable
   */
  public static String getBwadmingroupPrincipalRoot() throws Throwable {
    setRoots();

    return bwadmingroupPrincipalRoot;
  }

  /**
   * @param p
   * @return prefix
   */
  public static String getPrincipalHrefPrefix(final BwPrincipal p) {
    String account = p.getAccount();

    if (account.startsWith("/")) {
      // Assume a principal

      int pos = account.lastIndexOf("/");
      return setRoot(account.substring(pos));
    }

    try {
      if (p instanceof BwUser) {
        return setRoot(getUserPrincipalRoot());
      }

      if (p instanceof BwAdminGroup) {
        return setRoot(setRoot(getBwadmingroupPrincipalRoot()));
      }

      if (p instanceof BwGroup) {
        return setRoot(setRoot(getGroupPrincipalRoot()));
      }
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }

    return null;
  }

  /**
   * @param p
   * @throws Throwable
   */
  public void setPrincipalHref(final BwPrincipal p) throws Throwable {
    String account = p.getAccount();

    if (account.startsWith("/")) {
      // Assume a principal

      p.setPrincipalRef(account);
      return;
    }

    if (p instanceof BwUser) {
      p.setPrincipalRef(setRoot(getUserPrincipalRoot()) + p.getAccount());
      return;
    }

    if (p instanceof BwAdminGroup) {
      p.setPrincipalRef(setRoot(getBwadmingroupPrincipalRoot()) + p.getAccount());
      return;
    }

    if (p instanceof BwGroup) {
      p.setPrincipalRef(setRoot(getGroupPrincipalRoot()) + p.getAccount());
      return;
    }
  }

  /**
   * @param id
   * @param whoType
   * @return Sring principal
   * @throws Throwable
   */
  public String getPrincipalHref(final String id, final int whoType) throws Throwable {
    if (id.startsWith("/")) {
      // Assume a principal

      return id;
    }

    if (whoType == WhoDefs.whoTypeUser) {
      return setRoot(getUserPrincipalRoot()) + id;
    }

    if (whoType == WhoDefs.whoTypeGroup) {
      return setRoot(getBwadmingroupPrincipalRoot()) + id;
    }

    return id;
  }

  /**
   * @param val
   * @return BwPrincipal
   * @throws Throwable
   */
  public BwPrincipal getPrincipal(final String val) throws Throwable {
    BwPrincipal p = mappedPrincipal(val);

    if (p != null) {
      return p;
    }

    setRoots();

    if (!val.startsWith(principalRoot)) {
      return null;
    }

    if (val.startsWith(userPrincipalRoot)) {
      BwUser u = rintf.getUser(val.substring(userPrincipalRootLen));

      if (u != null) {
        principalMap.put(val, u);
      }

      return u;
    }

    if (val.startsWith(groupPrincipalRoot)) {
      throw new Exception("unimplemented");
    }

    return null;
  }

  /** We may be able to discard this as we move more towards principals
   *
   * @param href
   * @return BwUser
   * @throws Throwable
   */
  public BwUser getUser(final String href) throws Throwable {
    return (BwUser)getPrincipal(href);
  }
}
