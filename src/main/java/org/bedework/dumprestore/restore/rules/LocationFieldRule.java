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

import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwString;
import org.bedework.dumprestore.restore.RestoreGlobals;

/**
 * @author Mike Douglass
 * @version 1.0
 */
public class LocationFieldRule extends EntityFieldRule {
  LocationFieldRule(RestoreGlobals globals) {
    super(globals);
  }

  public void field(String name) throws Exception {
    BwString str = null;

    if (top() instanceof BwString) {
      str = (BwString)pop();
    }

    BwLocation l = (BwLocation)getTop(BwLocation.class, name);

    if (shareableEntityTags(l, name)) {
      return;
    }

    if (!thisOrAfterVersion(3, 5)) {
      if (name.equals("address")) {                         // pre bwstring
        l.setAddress(new BwString(null, stringFld()));
        return;
      }

      if (name.equals("subaddress")) {               // pre bwstring
        l.setSubaddress(new BwString(null, stringFld()));
        return;
      }

      if (name.equals("addr")) {                       // PRE3.5
        return;
      }

      if (name.equals("subaddr")) {                    // PRE3.5
        return;
      }
    } else {
      if (name.equals("address")) {
        l.setAddress(str);
        return;
      }

      if (name.equals("subaddress")) {
        l.setSubaddress(str);
        return;
      }
    }

    if (name.equals("link")) {
      l.setLink(stringFld());
      return;
    }

    if (name.equals("uid")) {
      l.setUid(stringFld());
      return;
    }

    if (name.equals("byteSize")) {
      l.setByteSize(intFld());
      return;
    }

    unknownTag(name);
  }
}

