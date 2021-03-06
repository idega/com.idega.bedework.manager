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

import org.bedework.dumprestore.restore.RestoreGlobals;
import org.bedework.dumprestore.restore.RestoreGlobals.OrganizerEntity;

/**
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class OrganizerFieldRule extends EntityFieldRule {
  OrganizerFieldRule(final RestoreGlobals globals) {
    super(globals);
  }

  @Override
  public void field(final String name) throws Exception {
    OrganizerEntity ent = (OrganizerEntity)top();

    if (name.equals("id")) {
      ent.id = intFld();
      return;
    }

    if (name.equals("seq")) {
      return;
    }

    if (name.equals("cn")) {
      ent.organizer.setCn(stringFld());
    } else if (name.equals("dir")) {
      ent.organizer.setDir(stringFld());
    } else if (name.equals("lang")) {
      ent.organizer.setLanguage(stringFld());
    } else if (name.equals("sentBy")) {
      ent.organizer.setSentBy(stringFld());
    } else if (name.equals("organizerUri")) {
      ent.organizer.setOrganizerUri(stringFld());
    } else if (name.equals("dtstamp")) {
      ent.organizer.setDtstamp(stringFld());

    } else if (name.equals("scheduleStatus")) {
      ent.organizer.setScheduleStatus(stringFld());

    } else if (name.equals("size")) {
        // Should not emit this  1 or 2 days of 3.6 and 3.5 only

    } else if (beforeVersion(3, 5)) {
      // PRE3.5
      if (name.equals("sent-by")) {
        ent.organizer.setSentBy(stringFld());
      } else if (name.equals("organizer-uri")) {
        ent.organizer.setOrganizerUri(stringFld());
      } else if (name.equals("public")) {
        // We dropped owner information - ignore this
      }
    } else {
      unknownTag(name);
    }
  }
}

