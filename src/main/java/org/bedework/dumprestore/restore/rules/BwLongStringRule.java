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

import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.dumprestore.restore.RestoreGlobals;

import org.xml.sax.Attributes;

/**
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class BwLongStringRule extends RestoreRule {
  /** Constructor
   *
   * @param globals
   */
  public BwLongStringRule(RestoreGlobals globals) {
    super(globals);
  }

  public void begin(String ns, String name, Attributes att) {
    if (thisOrAfterVersion(3, 5) && !name.equals("bwlongstring")) {
      // Pre 3.5 rule - ignore
      return;
    }

    push(new BwLongString());
  }

  public void end(String ns, String name) throws Exception {
    if (name.equals("bwlongstring")) {
      // 3.5 onwards we wrapped with a tag. Do nothing
      return;
    }

    BwLongString entity;
    try {
      entity = (BwLongString)pop();

      if (top() instanceof BwFilterDef) {
        BwFilterDef f = (BwFilterDef)top();

        if (name.equals("subaddr")) {
          f.addDescription(entity);
        } else {
          throw new Exception("unknown tag " + name);
        }
        return;
      }

      EventInfo ei = (EventInfo)top();

      BwEvent e = ei.getEvent();
      if (e instanceof BwEventProxy) {
        e = ((BwEventProxy)e).getRef();
      }

      if (name.equals("description")) {
        e.addDescription(entity);
      } else {
        throw new Exception("unknown tag " + name);
      }
    } catch (Throwable t) {
      handleException(t);
    }
  }
}

