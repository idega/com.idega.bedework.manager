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

import org.bedework.calfacade.BwXproperty;
import org.bedework.dumprestore.restore.RestoreGlobals;

/**
 * @author Mike Douglass
 * @version 1.0
 */
public class XpropertyFieldRule extends EntityFieldRule {
  XpropertyFieldRule(RestoreGlobals globals) {
    super(globals);
  }

  public void field(String name) throws Exception {
    BwXproperty xp = (BwXproperty)top();

    if (taggedEntityId(xp, name)) {
      return;
    }

    if (name.equals("name")) {
      xp.setName(stringFld());
    } else if (name.equals("pars")) {
      xp.setPars(stringFld());
    } else if (name.equals("value")) {
      xp.setValue(stringFld());
    } else if (name.equals("byteSize")) {
      xp.setByteSize(intFld());
    } else {
      unknownTag(name);
    }
  }
}

