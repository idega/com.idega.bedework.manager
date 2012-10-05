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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwUser;
import org.bedework.calfacade.env.CalOptionsFactory;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.prefs.BwPreferences;
import org.bedework.dumprestore.restore.RestoreGlobals;

import edu.rpi.cmt.access.Access;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mike Douglass   douglm rpi.edu
 * @version 1.0
 */
public class UserPrefsFieldRule extends EntityFieldRule {
  private static Collection<String> skippedNames;

  static {
    skippedNames = new ArrayList<String>();

    skippedNames.add("properties");
    skippedNames.add("views");
    skippedNames.add("collectionPaths");

    //PRE3.5
    skippedNames.add("subscriptions");
  }

  private static String adminGroupPrefix;

  UserPrefsFieldRule(final RestoreGlobals globals) {
    super(globals);
  }

  /* (non-Javadoc)
   * @see org.apache.commons.digester.Rule#begin(java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void begin(final String namespace,
                    final String name,
                    final Attributes attributes) throws Exception {
    super.begin(namespace, name, attributes);

    if (name.equals("subscription")) {       // PRE3.5
      push(new BwCalendar());
    } else if (name.equals("view")) {
      push(new BwView());
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.restore.rules.EntityFieldRule#field(java.lang.String)
   */
  @Override
  public void field(final String name) throws Throwable{
    if (skippedNames.contains(name)) {
      return;
    }

    BwPreferences p = null;

    if (top() instanceof BwView) {
      BwView view = (BwView)top();

      if (name.equals("view")) {
        processViewEnd(view);
        return;
      }

      if (!viewField(view, name)) {
        unknownTag(name);
      }

      return;
    }

    if (name.equals("subscription") || (name.startsWith("sub-"))) { // PRE3.5
    } else {
      p = (BwPreferences)getTop(BwPreferences.class, name);
    }

    try {
      if (ownedEntityTags(p, name)) {
        return;
      }

      if (name.equals("email")) {
        p.setEmail(stringFld());
      } else if (name.equals("default-calendar-path")) {  // PRE3.5
        p.setDefaultCalendarPath(stringFld());
      } else if (name.equals("defaultCalendarPath")) {
        p.setDefaultCalendarPath(stringFld());
      } else if (name.equals("skinName")) {
        p.setSkinName(stringFld());
      } else if (name.equals("skinStyle")) {
        p.setSkinStyle(stringFld());
      } else if (name.equals("preferredView")) {
        p.setPreferredView(stringFld());
      } else if (name.equals("preferredViewPeriod")) {
        p.setPreferredViewPeriod(stringFld());
      } else if (name.equals("workDays")) {
        p.setWorkDays(stringFld());
      } else if (name.equals("workdayStart")) {
        p.setWorkdayStart(intFld());
      } else if (name.equals("workdayEnd")) {
        p.setWorkdayEnd(intFld());
      } else if (name.equals("preferredEndType")) {
        p.setPreferredEndType(stringFld());
      } else if (name.equals("userMode")) {
        p.setUserMode(intFld());
      } else if (name.equals("pageSize")) {
        p.setPageSize(intFld());
      } else if (name.equals("hour24")) {
        p.setHour24(booleanFld());
      } else if (name.equals("scheduleAutoRespond")) {
        p.setScheduleAutoRespond(booleanFld());
      } else if (name.equals("scheduleAutoCancelAction")) {
        p.setScheduleAutoCancelAction(intFld());
      } else if (name.equals("scheduleDoubleBook")) {
        p.setScheduleDoubleBook(booleanFld());
      } else if (name.equals("scheduleAutoProcessResponses")) {
        p.setScheduleAutoProcessResponses(intFld());

      } else if (name.equals("byteSize")) {
        p.setByteSize(intFld());

      // PRE3.5 subscription fields

      } else if (subscriptionField(name)) {
        // All done

      } else {
        unknownTag(name);
      }
    } catch (Throwable t) {
      error("Exception setting prefs " + p);
      handleException(t);
    }
  }

  /* View definitions.
   */
  private boolean viewField(final BwView view, final String name) throws Throwable {
    if (beforeVersion(3, 5)) {
      return oldViewField(view, name);
    }

    if (taggedEntityId(view, name)) {
      return true;
    }

    if (name.equals("name")) {
      view.setName(stringFld());
      return true;
    }

    if (name.equals("path")) {
      view.addCollectionPath(stringFld());
      return true;
    }

    if (name.equals("byteSize")) {
      view.setByteSize(intFld());
      return true;
    }

    return false;
  }

  private boolean oldViewField(BwView view, final String name) throws Throwable {
    if (!name.startsWith("view-")) {
      return false;
    }

    if (name.equals("view-owner")) {                 // PRE3.3
      return true;
    }

    if (name.equals("view-seq")) {                 // PRE3.5
      return true;
    }

    if (name.equals("view-id")) {
      view = new BwView();         // XXX ????
      view.setId(intFld());
      return true;
    }

    if (name.equals("view-name")) {
      view.setName(stringFld());
      return true;
    }

    if (name.equals("view-subscriptions")) {       // PRE3.5
      return true;
    }

    if (name.equals("view-collections")) {
      return true;
    }

    if (!name.startsWith("view-sub-")) {
      return false;
    }

    String viewPath = null; // If non-null add given collection to view

    String ownerHref = ((BwPreferences)getDigester().peek(1)).getOwnerHref();
    BwUser owner = globals.getUser(ownerHref);

    if (name.equals("view-sub-name")) {       // PRE3.5
      viewPath = globals.subscriptionsTbl.getSub(owner, stringFld());

      if (viewPath == null) {
        if (globals.subscriptionsTbl.getSubDropped(owner,
                                                   stringFld()) == null) {
          error("  Unknown subscription " + stringFld() + " for view " + view);
        }
      }
    } else if (name.equals("view-sub-path")) {
      viewPath = stringFld();
    }

    if (viewPath == null) {
      // If this is the default view add the user home path.
      if (view.getName().equals(globals.getSyspars().getDefaultUserViewName())) {
        viewPath = globals.rintf.getUserHome(owner); // TRAILSLASH
      }
    }

    if (viewPath != null) {
      view.addCollectionPath(viewPath);
    }

    return true;
  }

  private void processViewEnd(final BwView view) throws Throwable {
    pop();

    BwPreferences p = (BwPreferences)getTop(BwPreferences.class, "view");

    p.addView(view);
  }

  /* PRE3.5  subscriptions definitions.
   */
  private boolean subscriptionField(final String name) throws Throwable {
    BwCalendar cal = null;

    if (name.equals("subscription")) {
      processSubscriptionEnd();
      return true;
    }

    if (!name.startsWith("sub-")) {
      return false;
    }

    cal = (BwCalendar)getTop(BwCalendar.class, name);

    if (name.equals("sub-owner")) {                // PRE3.3
      cal.setOwnerHref(userFld().getPrincipalRef());
    } else if (name.equals("view-seq")) {
    } else if (name.equals("sub-calendarDeleted")) {

    } else if (name.equals("sub-id")) {
      cal.setId(intFld());
    } else if (name.equals("sub-seq")) {
    } else if (name.equals("sub-name")) {
      cal.setName(stringFld());
    } else if (name.equals("sub-uri")) {
      cal.setAliasUri(stringFld());
    } else if (name.equals("sub-affectsFreeBusy")) {
      cal.setAffectsFreeBusy(booleanFld());
    } else if (name.equals("sub-ignoreTransparency")) {
      cal.setIgnoreTransparency(booleanFld());
    } else if (name.equals("sub-display")) {
      cal.setDisplay(booleanFld());
    } else if (name.equals("sub-style")) {
      cal.setColor(stringFld());
    } else if (name.equals("sub-internalSubscription")) {
      //cal.setInternalSubscription(booleanFld());
    } else if (name.equals("sub-emailNotifications")) {
      //cal.setEmailNotifications(booleanFld());
    } else if (name.equals("sub-unremoveable")) {
      cal.setUnremoveable(booleanFld());
    } else {
      error("Unknown subscription field " + name);
    }

    return true;
  }

  private void processSubscriptionEnd() throws Throwable {
    BwCalendar cal = (BwCalendar)getTop(BwCalendar.class, "subscription");

    pop();
    BwPreferences p = (BwPreferences)getTop(BwPreferences.class, "subscription");

    cal.setCreatorHref(cal.getOwnerHref());

    if (cal.getOwnerHref() == null) {
      error("No owner for " + cal);
      return;
    }

    if (!p.getOwnerHref().equals(cal.getOwnerHref())) {
      error("Owners don't match for " + cal);
      error("  Found owner " + cal.getOwnerHref() + " expected " + p.getOwnerHref());
      cal.setOwnerHref(p.getOwnerHref());
      return;
    }

    cal.setCalType(BwCalendar.calTypeAlias);

    /* If this is a subscription to our own collection we don't need an
     * alias
     */
    BwUser calOwner = globals.getUser(cal.getOwnerHref());
    String calHome = globals.rintf.getUserHome(calOwner); // TRAILSLASH
    String path = cal.getInternalAliasPath();

    cal.setPath(path);

    if (adminGroupPrefix == null) {
      adminGroupPrefix = CalOptionsFactory.getOptions().getGlobalStringProperty("adminGroupsIdPrefix");

      if (adminGroupPrefix == null) {
        error("Unable to retrieve property admingroupsidprefix");
      }
    }

    String href = cal.getOwnerHref();
    int pos = href.lastIndexOf("/");
    String account = href.substring(pos + 1);

    boolean publicOwner = account.startsWith(adminGroupPrefix) ||
                          account.equals(globals.getSyspars().getPublicUser());

    if (path.equals(calHome)) { // TRAILSLASH
      cal.setName(account);
      globals.subscriptionsTbl.put(calOwner, cal);

      globals.subscriptionFixes.add("Subscription owner: " + account +
                                    "\t added\t " + cal.getPath());

      return;
    }

    if (path.startsWith(calHome + "/")) { // TRAILSLASH
      /* This is a subscription into one of our own collections. Early versions
       * of bedework had subscriptions to the individual collections. Very quickly
       * it was changed to a subscription to the calendar home.
       *
       * Drop anything and add a calendar home subscription.
       */
      globals.subscriptionsTbl.putDropped(calOwner, cal);

      globals.subscriptionFixes.add("Subscription owner: " + account +
                                    "\t dropped\t " + cal.getPath());

      if (globals.subscriptionsTbl.getSub(calOwner, account) == null) {
        cal.setPath(calHome);
        cal.setName(account);

        globals.subscriptionsTbl.put(calOwner, cal);

        globals.subscriptionFixes.add("Subscription owner: " + account +
                                      "\t added\t " + cal.getPath());
      }

      return;
    }

    /* This is a subscription to a collection the current user does not own.
     * Create an alias with the current object.
     *
     * If this is an admin group we need to set the access to be world readable
     * so that it can be accessed via the public client.
     */

    if (publicOwner) {
      cal.setAccess(Access.getDefaultPublicAccess());
    }

    int suffix = 1;
    String origName = cal.getName();

    for (;;) {
      try {
        globals.rintf.addCalendar(cal, calHome);
        break;
      } catch (CalFacadeException cfe) {
        if (CalFacadeException.duplicateCalendar.equals(cfe.getMessage())) {
          if (suffix == 1) {
            warn("Renamed subscription " + cal.getName());
          }

          cal.setName(origName + "_" + suffix);
          suffix++;
        } else {
          throw cfe;
        }
      }
    }

    globals.subscriptionsTbl.put(calOwner, cal);

    StringBuilder sb = new StringBuilder();

    sb.append("Subscription owner: ");
    sb.append(account);
    sb.append("\t added\t ");
    sb.append(cal.getPath());

    if (publicOwner) {
      sb.append("\t World readable");
    }

    globals.subscriptionFixes.add(sb.toString());
  }
}

