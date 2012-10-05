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
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEventProperty;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.prefs.CalendarPref;
import org.bedework.calfacade.svc.prefs.CategoryPref;
import org.bedework.calfacade.svc.prefs.LocationPref;
import org.bedework.calfacade.svc.prefs.ContactPref;
import org.bedework.dumprestore.restore.RestoreGlobals;

/**
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class AuthUserFieldRule extends EntityFieldRule {
  private boolean inCategoryPrefs;
  private boolean inCollectionsPrefs;
  private boolean inContactPrefs;
  private boolean inLocationPrefs;

  AuthUserFieldRule(RestoreGlobals globals) {
    super(globals);
  }

  public void fieldStart(String name) throws Exception {
    if (name.equals("categoryPrefs")) {
      inCategoryPrefs = true;
    } else if (name.equals("calendarPrefs")) {
      inCollectionsPrefs = true;
    } else if (name.equals("contactPrefs")) {
      inContactPrefs = true;
    } else if (name.equals("locationPrefs")) {
      inLocationPrefs = true;
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.restore.rules.EntityFieldRule#field(java.lang.String)
   */
  public void field(String name) throws Throwable {
    BwEventProperty ep = null;
    BwCalendar cal = null;

    try {
      if (top() instanceof BwEventProperty) {
        ep = (BwEventProperty)pop();
      } else if (top() instanceof BwCalendar) {
        cal = (BwCalendar)pop();
      }

      BwAuthUser au = (BwAuthUser)top();

      if (taggedEntityId(au, name)) {
        return;
      }

      if (name.equals("id")) { // PRE3.5
        au.setUserHref(userFld().getPrincipalRef());
      } else if (name.equals("userHref")) {
        au.setUserHref(stringFld());
      } else if (name.equals("account")) {
        au.setUserHref(globals.rintf.getUser(stringFld()).getPrincipalRef());
      } else if (name.equals("user")) {
        // done above
      } else if (name.equals("userType") ||     // PRE3.5
                 name.equals("usertype")) {
        int type = intFld();

        if ((type & 32768) != 0) {              // PRE3.5 Constant no longer in post 3.5
          // pre 3.5
          if (beforeVersion(3, 5)) {
            String href = au.getUserHref();
            int pos = href.lastIndexOf("/");
            globals.addRootUser(href.substring(pos + 1));
          }

          type -= 32768;
        }

        au.setUsertype(type);

        /* Prefs stuff next */

      } else if (name.equals("autoAdd")) {
        if (inCategoryPrefs) {
          getCategoryPrefs(au).setAutoAdd(booleanFld());
        } else if (inCollectionsPrefs) {
          getCalendarPrefs(au).setAutoAdd(booleanFld());
        } else if (inContactPrefs) {
          getContactPrefs(au).setAutoAdd(booleanFld());
        } else if (inLocationPrefs) {
          getLocationPrefs(au).setAutoAdd(booleanFld());
        } else {
          error("Not in any prefs for autoAdd");
        }

      } else if (name.equals("category")) {
        au.getPrefs().getCategoryPrefs().add((BwCategory)ep);
      } else if (name.equals("collection")) {
        au.getPrefs().getCalendarPrefs().add(cal);
      } else if (name.equals("contact")) {
        au.getPrefs().getContactPrefs().add((BwContact)ep);
      } else if (name.equals("location")) {
        au.getPrefs().getLocationPrefs().add((BwLocation)ep);

      } else if (name.equals("categoryPrefs")) {
        inCategoryPrefs = false;
      } else if (name.equals("calendarPrefs")) {
        inCollectionsPrefs = false;
      } else if (name.equals("contactPrefs")) {
        inContactPrefs = false;
      } else if (name.equals("locationPrefs")) {
        inLocationPrefs = false;

      } else if (name.equals("prefs")) {
      } else if (name.equals("preferences")) {   // PRE3.5

      } else if (name.equals("byteSize")) {
      } else if (oldPrefs(name, au)) {
      } else {
        unknownTag(name);
      }
    } catch (Throwable t) {
      handleException(t);
    }
  }

  // PRE3.5
  private boolean oldPrefs(String name,
                           BwAuthUser au) throws Exception {
    if (thisOrAfterVersion(3, 5)) {
      return false;
    }

    if (name.equals("autoAddCategories")) {
      getCategoryPrefs(au).setAutoAdd(booleanFld());
    } else if (name.equals("preferredCategory")) {
      getCategoryPrefs(au).add(categoryFld());

    } else if (name.equals("autoAddLocations")) {
      getLocationPrefs(au).setAutoAdd(booleanFld());
    } else if (name.equals("preferredLocation")) {
      getLocationPrefs(au).add(locationFld());

    } else if (name.equals("autoAddContacts")) {
      getContactPrefs(au).setAutoAdd(booleanFld());
    } else if (name.equals("preferredContact")) {
      getContactPrefs(au).add(contactFld());

    } else if (name.equals("autoAddCalendars")) {
      getCalendarPrefs(au).setAutoAdd(booleanFld());
    } else if (name.equals("preferredCalendar")) {
      getCalendarPrefs(au).add(calendarFld());

    } else if (name.equals("preferredCategories")) {
    } else if (name.equals("preferredCalendars")) {
    } else if (name.equals("preferredContacts")) {
    } else if (name.equals("preferredLocations")) {

    } else if (name.equals("autoAddSponsors")) {            // PRE3.3
      getContactPrefs(au).setAutoAdd(booleanFld());
    } else if (name.equals("preferredSponsor")) {            // PRE3.3
      getContactPrefs(au).add(contactFld());

    } else {
      return false;
    }

    return true;
  }

  /**
   * @param au
   * @return prefs
   */
  public BwAuthUserPrefs getPrefs(BwAuthUser au) {
    BwAuthUserPrefs aup = au.getPrefs();

    if (aup == null) {
      aup = new BwAuthUserPrefs();
      au.setPrefs(aup);
    }

    return aup;
  }

  private CategoryPref getCategoryPrefs(BwAuthUser au) {
    BwAuthUserPrefs aup = getPrefs(au);

    CategoryPref p = aup.getCategoryPrefs();
    if (p == null) {
      p = new CategoryPref();
      aup.setCategoryPrefs(p);
    }

    return p;
  }

  private LocationPref getLocationPrefs(BwAuthUser au) {
    BwAuthUserPrefs aup = getPrefs(au);

    LocationPref p = aup.getLocationPrefs();
    if (p == null) {
      p = new LocationPref();
      aup.setLocationPrefs(p);
    }

    return p;
  }

  private ContactPref getContactPrefs(BwAuthUser au) {
    BwAuthUserPrefs aup = getPrefs(au);

    ContactPref p = aup.getContactPrefs();
    if (p == null) {
      p = new ContactPref();
      aup.setContactPrefs(p);
    }

    return p;
  }

  private CalendarPref getCalendarPrefs(BwAuthUser au) {
    BwAuthUserPrefs aup = getPrefs(au);

    CalendarPref p = aup.getCalendarPrefs();
    if (p == null) {
      p = new CalendarPref();
      aup.setCalendarPrefs(p);
    }

    return p;
  }
}

