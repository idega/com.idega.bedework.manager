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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/** Map for subscriptions
 *
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class SubscriptionsMap
        extends HashMap<PrincipalHref, Collection<? extends Object>> {
  /**
   * @author douglm
   */
  public static class SubInfo {
    /** */
    public String name;
    /** */
    public String path;
    /** */
    public boolean dropped;
    /** */
    public boolean display;
    /** */
    public boolean affectsFreeBusy;
    /** */
    public boolean ignoreTransparency;
    /** */
    public boolean unremoveable;
    /** */
    public String color;

    SubInfo(BwCalendar cal, boolean dropped) {
      name = cal.getName();
      path = cal.getPath();
      this.dropped = dropped;
      this.display = cal.getDisplay();
      this.affectsFreeBusy = cal.getAffectsFreeBusy();
      this.ignoreTransparency = cal.getIgnoreTransparency();
      this.unremoveable = cal.getUnremoveable();
      this.color = cal.getColor();
    }
  }

  /**
   * @param key   BwUser
   * @param cal
   */
  public void put(BwUser key, BwCalendar cal) {
    put(PrincipalHref.makeOwnerHref(key), new SubInfo(cal, false));
  }

  /**
   * @param key   BwUser
   * @param cal
   */
  public void putDropped(BwUser key, BwCalendar cal) {
    put(PrincipalHref.makeOwnerHref(key), new SubInfo(cal, true));
  }

  /**
   * @param key   OwnerInfo
   * @param val
   */
  public void put(PrincipalHref key, SubInfo val) {
    Collection<SubInfo> al = getSubs(key);
    if (al == null) {
      al = new ArrayList<SubInfo>();
      put(key, al);
    }

    al.add(val);
  }

  /**
   * @param key   OwnerInfo
   * @return Collection of String paths
   */
  public Collection<SubInfo> getSubs(PrincipalHref key) {
    return (Collection<SubInfo>)get(key);
  }

  /**
   * @param owner   BwUser owner
   * @param name
   * @return String path
   */
  public String getSub(BwUser owner, String name) {
    PrincipalHref key = PrincipalHref.makeOwnerHref(owner);
    Collection<SubInfo> subs = getSubs(key);

    if (subs == null) {
      return null;
    }

    for (SubInfo sub: subs) {
      if (!sub.dropped && (sub.name.equals(name))) {
        return sub.path;
      }
    }

    return null;
  }

  /**
   * @param owner   BwUser owner
   * @param name
   * @return String path
   */
  public String getSubDropped(BwUser owner, String name) {
    PrincipalHref key = PrincipalHref.makeOwnerHref(owner);
    Collection<SubInfo> subs = getSubs(key);

    if (subs == null) {
      return null;
    }

    for (SubInfo sub: subs) {
      if (sub.dropped && (sub.name.equals(name))) {
        return sub.path;
      }
    }

    return null;
  }
}
