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

import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventAnnotation;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.BwGeo;
import org.bedework.calfacade.CalFacadeDefs;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.dumprestore.restore.RestoreGlobals;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mike Douglass
 * @version 1.0
 */
public class EventFieldRule extends EntityFieldRule {
  private static Collection<String> skippedNames;

  static {
    skippedNames = new ArrayList<String>();

    skippedNames.add("alarms");
    skippedNames.add("attachments");
    skippedNames.add("attendee");
    skippedNames.add("attendees");
    skippedNames.add("contacts");
    skippedNames.add("categories");
    skippedNames.add("descriptions");
    skippedNames.add("exdates");
    skippedNames.add("exrules");
    skippedNames.add("master");
    skippedNames.add("override");    // Set on object creation
    skippedNames.add("overrides");
    skippedNames.add("rdates");
    skippedNames.add("recipients");
    skippedNames.add("rrules");
    skippedNames.add("summaries");
    skippedNames.add("target");
    skippedNames.add("xproperties");

    // 3.6: Incorrectly dumped property
    skippedNames.add("significantChange");

    //PRE3.5
    skippedNames.add("contact-key");
    skippedNames.add("location-key");
    skippedNames.add("eventAttendees");
    skippedNames.add("eventCategories");
    skippedNames.add("eventComments");
    skippedNames.add("eventRecurrence");
    skippedNames.add("eventResources");
  }

  EventFieldRule(final RestoreGlobals globals) {
    super(globals);
  }

  @Override
  public void field(final String name) throws Exception {
    if (skippedNames.contains(name)) {
      return;
    }

    DateTimeValues dtv = null;

    if (top() instanceof DateTimeValues) {
      dtv = (DateTimeValues)pop();
    }

    EventInfo ei = (EventInfo)getTop(EventInfo.class, name);
    BwEventAnnotation ann = null;

    BwEvent e = ei.getEvent();
    if (e instanceof BwEventProxy) {
      ann = ((BwEventProxy)e).getRef();
      //e = ann;
    }

    if (shareableContainedEntityTags(e, name)) {
      return;
    }

    try {
      if (pre3p3Fields(e, name)) {
        return;
      }

      if (name.equals("location")) {
        if (beforeVersion(3, 5)) {
          e.setLocation(locationFld());
        }
        return;
      }

      if (name.equals("category")) {
        if (beforeVersion(3, 5)) {
          e.addCategory(categoryFld());
        }
        return;
      }

      if (name.equals("emptyFlags")) {
        char[] flags = stringFld().toCharArray();

        for (char c: flags) {
          if ((c != 'T' ) && (c != 'F')) {
            error("Bad empty flags '" + stringFld() + "' for event " + ann);
          }
        }

        ann.setEmptyFlags(new String(flags));

        /* ------------------- Start/end --------------------------- */
      } else if (name.equals("noStart")) {
        if (ann != null) {
          ann.setNoStart(booleanFld());
        } else {
          e.setNoStart(booleanFld());
        }

      } else if (name.equals("dtstart")) {
        if (ann != null) {
          ann.setDtstart(dateTimeFld(dtv));
        } else {
          e.setDtstart(dateTimeFld(dtv));
        }

      } else if (name.equals("dtend")) {
        if (ann != null) {
          ann.setDtend(dateTimeFld(dtv));
        } else {
          e.setDtend(dateTimeFld(dtv));
        }

      } else if (pre3p5Fields(ei, e, ann, dtv, name)) {
        // done

      } else if (name.equals("duration")) {
        // XXX Fix bad duration value due to old bug
        String dur = stringFld();
        char endType = e.getEndType();
        if ("PT1S".equals(dur) && (endType == StartEndComponent.endTypeNone)) {
          dur = "PT0S";
        }
        if (ann != null) {
          ann.setDuration(dur);
        } else {
          e.setDuration(dur);
        }
      } else if (name.equals("endType")) {
        if (ann != null) {
          ann.setEndType(charFld());
        } else {
          e.setEndType(charFld());
        }

      } else if (name.equals("entityType")) {
        e.setEntityType(intFld());
      } else if (name.equals("name")) {
        if (ann != null) {
          ann.setName(stringFld());
        } else {
          e.setName(stringFld());
        }
      } else if (name.equals("uid")) {
        if (ann != null) {
          ann.setUid(stringFld());
        } else {
          e.setUid(stringFld());
        }
      } else if (name.equals("classification")) {
        e.setClassification(stringFld());

      } else if (name.equals("link")) {
        e.setLink(stringFld());

      } else if (name.equals("geo-latitude")) {
        BwGeo geo = e.getGeo();
        if (geo == null) {
          geo = new BwGeo();
          e.setGeo(geo);
        }
        //geo.setLatitude(bigDecimalFld());
        geo.setLatitude(bigDecimalFld());
      } else if (name.equals("geo-longitude")) {
        BwGeo geo = e.getGeo();
        if (geo == null) {
          geo = new BwGeo();
          e.setGeo(geo);
        }
        //geo.setLongitude(bigDecimalFld());
        geo.setLongitude(bigDecimalFld());

      } else if (name.equals("status")) {
        String status = stringFld();
        if ((status != null) &&
            (!status.equals("F"))) {       // 2.3.2
          e.setStatus(status);
        }
      } else if (name.equals("cost")) {
        e.setCost(stringFld());
      } else if (name.equals("deleted")) {
        e.setDeleted(booleanFld());
      } else if (name.equals("tombstoned")) {
        e.setTombstoned(booleanFld());

      } else if (name.equals("dtstamp")) {
        e.setDtstamp(stringFld());
      } else if (name.equals("lastmod")) {
        e.setLastmod(stringFld());
      } else if (name.equals("created")) {
        e.setCreated(stringFld());

      } else if (name.equals("byteSize")) {
        e.setByteSize(intFld());

      } else if (name.equals("priority")) {
        e.setPriority(integerFld());

      } else if (name.equals("transparency")) {
        e.setTransparency(stringFld());

      } else if (name.equals("relatedTo")) {
//        e.setRelatedTo(stringFld());

      } else if (name.equals("percentComplete")) {
        e.setPercentComplete(integerFld());
      } else if (name.equals("completed")) {
        e.setCompleted(stringFld());

      } else if (name.equals("ctoken")) {
        e.setCtoken(stringFld());

        /* --------------- Recurrence fields ---------------------- */
      } else if (name.equals("recurring")) {
        e.setRecurring(new Boolean(booleanFld()));

      } else if (name.equals("rrule")) {
        e.addRrule(stringFld());
      } else if (name.equals("exrule")) {
        e.addExrule(stringFld());

      } else if (name.equals("rdate")) {
        e.addRdate(dateTimeFld(dtv));
      } else if (name.equals("exdate")) {
        e.addExdate(dateTimeFld(dtv));

      } else if (name.equals("recurrenceId")) {
        e.setRecurrenceId(stringFld());
      } else if (name.equals("latestDate")) {
        e.setCtoken(stringFld());

        /* --------------- Scheduling fields ---------------------- */

      } else if (name.equals("organizer")) {
        if (beforeVersion(3, 5)) {
          e.setOrganizer(organizerFld());
        }

      } else if (name.equals("sequence")) {
        e.setSequence(intFld());
      } else if (name.equals("scheduleMethod")) {
        e.setScheduleMethod(intFld());
      } else if (name.equals("originator")) {
        e.setOriginator(stringFld());
      } else if (name.equals("recipient")) {
        e.addRecipient(stringFld());
      } else if (name.equals("scheduleState")) {
        e.setScheduleState(intFld());
      } else if (name.equals("organizerSchedulingObject")) {
        e.setOrganizerSchedulingObject(booleanFld());
      } else if (name.equals("attendeeSchedulingObject")) {
        e.setAttendeeSchedulingObject(booleanFld());
      } else if (name.equals("stag")) {
        e.setStag(stringFld());

        /* ------------------- vavailability --------------------------- */
      } else if (name.equals("busyType")) {
        e.setBusyType(intFld());
      } else {
        unknownTag(name);
      }
    } catch (Exception ex) {
      error("Error processing event uid " + e.getUid(), ex);
      globals.entityError = true;
    }
  }

  /* PRE3.3
   */
  private boolean pre3p3Fields(final BwEvent e,
                               final String name) throws Exception {
    if (thisOrAfterVersion(3, 3)) {
      return false;
    }

    if (name.equals("summary")) {
      e.setSummary(stringFld());
      return true;
    }

    if (name.equals("description")) {
      e.setDescription(stringFld());
      return true;
    }

    if (name.equals("guid")) {
      e.setUid(stringFld());
      return true;
    }

    if (name.equals("sponsor")) {
      e.setContact(contactFld());
      return true;
    }

    return false;
  }

  /* PRE3.5
   */
  private boolean pre3p5Fields(final EventInfo ei, final BwEvent e,
                               final BwEventAnnotation ann,
                               DateTimeValues dtv,
                               final String name) throws Exception {
    if (thisOrAfterVersion(3, 5)) {
      return false;
    }

    if (oldAnnotationFlags(ann, name)) {
      return true;
    }

    /* ----------- Annotation fields - terminated by uid ----------------- */
    if (name.equals("target-calendar")) {
      annotationTarget(ann, calendarFld(), null, null);
    } else if (name.equals("target-recurrenceId")) {
      annotationTarget(ann, null, stringFld(), null);
    } else if (name.equals("target-guid")) {
      annotationTarget(ann, null, null, stringFld());

    } else if (name.equals("master-calendar")) {
      annotationMaster(ei, ann, calendarFld(), null, null);
    } else if (name.equals("master-recurrenceId")) {
      annotationMaster(ei, ann, null, stringFld(), null);
    } else if (name.equals("master-guid")) {
      annotationMaster(ei, ann, null, null, stringFld());


    /* ------------------- Start --------------------------- */

    } else if (name.equals("start-date-type")) {
      dtv = getDtv(dtv, true);
      dtv.dateType = booleanFld();
    } else if (name.equals("start-tzid")) {
      dtv = getDtv(dtv, true);
      dtv.tzid = stringFld();
    } else if (name.equals("start-dtval")) {
      dtv = getDtv(dtv, true);
      dtv.dtval = fixedDateTimeFld();
    } else if (name.equals("start-date")) {
      dtv = getDtv(dtv, false);
      dtv.date = stringFld();

      if (ann != null) {
        ann.setDtstart(dateTimeFld(dtv));
      } else {
        e.setDtstart(dateTimeFld(dtv));
      }

      /* ------------------------- end ----------------------- */
    } else if (name.equals("end-date-type")) {
      dtv = getDtv(dtv, true);
      dtv.dateType = booleanFld();
    } else if (name.equals("end-tzid")) {
      dtv = getDtv(dtv, true);
      dtv.tzid = stringFld();
    } else if (name.equals("end-dtval")) {
      dtv = getDtv(dtv, true);
      dtv.dtval = fixedDateTimeFld();
    } else if (name.equals("end-date")) {
      dtv = getDtv(dtv, false);
      dtv.date = stringFld();

      if (ann != null) {
        ann.setDtend(dateTimeFld(dtv));
      } else {
        e.setDtend(dateTimeFld(dtv));
      }

      /* ------------------------- rdate ----------------------- */
    } else if (name.equals("rdate-date-type")) {
      dtv = getDtv(dtv, true);
      dtv.dateType = booleanFld();
    } else if (name.equals("rdate-tzid")) {
      dtv = getDtv(dtv, true);
      dtv.tzid = stringFld();
    } else if (name.equals("rdate-dtval")) {
      dtv = getDtv(dtv, true);
      dtv.dtval = stringFld();
    } else if (name.equals("rdate-date")) {
      dtv = getDtv(dtv, false);
      dtv.date = stringFld();

      e.addRdate(dateTimeFld(dtv));

      /* ------------------------- exdate ----------------------- */
    } else if (name.equals("exdate-date-type")) {
      dtv = getDtv(dtv, true);
      dtv.dateType = booleanFld();
    } else if (name.equals("exdate-tzid")) {
      dtv = getDtv(dtv, true);
      dtv.tzid = stringFld();
    } else if (name.equals("exdate-dtval")) {
      dtv = getDtv(dtv, true);
      dtv.dtval = stringFld();
    } else if (name.equals("exdate-date")) {
      dtv = getDtv(dtv, false);
      dtv.date = stringFld();

      e.addExdate(dateTimeFld(dtv));

    } else if (name.equals("attendee")) {
      BwAttendee att = globals.attendeesTbl.get(intFld());

      if (att == null) {
        error("Missing attendee - id=" + intFld());
        globals.entityError = true;
      } else {
        e.addAttendee(att);
        att.setId(CalFacadeDefs.unsavedItemKey); // Mark unsaved
      }

    } else if (name.equals("end-type")) {
      if (ann != null) {
        ann.setEndType(charFld());
      } else {
        e.setEndType(charFld());
      }
    } else if (name.equals("last-mod")) {
      e.setLastmod(stringFld());
    } else if (name.equals("create-date")) {
      e.setCreated(stringFld());


    } else if (name.equals("nostart")) {
      if (ann != null) {
        ann.setNoStart(booleanFld());
      } else {
        e.setNoStart(booleanFld());
      }

    } else {
      return false;
    }

    return true;
  }

  /* PRE3.5
   */
  private boolean oldAnnotationFlags(final BwEventAnnotation ann,
                                     final String name) throws Exception {
    if (thisOrAfterVersion(3, 5)) {
      return false;
    }

    if (name.equals("alarmsEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiAlarms, booleanFld());
    } else if (name.equals("attendeesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiAttendees, booleanFld());
    } else if (name.equals("categoriesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiCategories, booleanFld());
    } else if (name.equals("commentsEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiComments, booleanFld());
    } else if (name.equals("contactsEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiContacts, booleanFld());
    } else if (name.equals("descriptionsEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiDescriptions, booleanFld());
    } else if (name.equals("exdatesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiExdates, booleanFld());
    } else if (name.equals("exrulesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiExrules, booleanFld());
    } else if (name.equals("rdatesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiRdates, booleanFld());
    } else if (name.equals("recipientsEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiRecipients, booleanFld());
    } else if (name.equals("requestStatusesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiRequestStatuses, booleanFld());
    } else if (name.equals("resourcesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiResources, booleanFld());
    } else if (name.equals("rrulesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiRrules, booleanFld());
    } else if (name.equals("summariesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiSummaries, booleanFld());
    } else if (name.equals("xpropertiesEmpty")) {
      ann.setEmptyFlag(BwEvent.ProxiedFieldIndex.pfiXproperties, booleanFld());

    } else {
      return false;
    }

    return true;
  }

  private DateTimeValues getDtv(DateTimeValues dtv,
                                final boolean push) {
    if (dtv == null) {
      dtv = new DateTimeValues();
    }

    if (push) {
      push(dtv);
    }

    return dtv;
  }

  // PRE3.5
  private void annotationTarget(final BwEventAnnotation ann,
                                final BwCalendar cal,
                                final String recurrenceId,
                                final String uid) throws Exception {
    ann.setTarget(setReffed(ann.getTarget(), ann, cal, recurrenceId, uid));
  }

  // PRE3.5
  private void annotationMaster(final EventInfo ei, final BwEventAnnotation ann,
                                final BwCalendar cal,
                                final String recurrenceId,
                                final String uid) {
    if (ann.testOverride()) {
      return;
    }

    ann.setMaster(setReffed(ann.getMaster(), ann, cal, recurrenceId, uid));
  }

  // PRE3.5
  private BwEvent setReffed(BwEvent e,
                            final BwEventAnnotation ann,
                            final BwCalendar cal,
                            final String recurrenceId,
                            final String uid) {
    if (e == null) {
      e = new BwEventObj();
    }

    if (cal != null) {
      e.setColPath(cal.getPath());
    } else if (recurrenceId != null) {
      e.setRecurrenceId(recurrenceId);
    } else {
      e.setUid(uid);
    }

    return e;
  }

  /*
  private BwEvent fetchReffed(BwEvent event) throws Throwable {
    BwCalendar cal = event.getCalendar();
    String guid = event.getUid();

    if ((cal == null) || (guid == null)) {
      return null;
    }

    event = globals.rintf.getEvent(cal, guid);
    if (event != null) {
      return event;
    }

    error("Error fetching target event " + cal.getPath() + ": " + guid);
    return null;
  }
  */
}

