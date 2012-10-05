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

import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwUser;
import java.util.HashMap;

/** Globals for the restore phase
 *
 * @author Mike Douglass   douglm@rpi.edu
 * @version 1.0
 */
public class PrincipalMap extends HashMap<PrincipalHref, BwPrincipal> {
  /**
   * @param val
   */
  public void put(BwPrincipal val) {
    PrincipalHref key = PrincipalHref.makeOwnerHref(val);

    if (get(key) != null) {
      throw new RuntimeException("Principal already in table with key " + key);
    }
    put(key, val);
  }

  /**
   * @param key  OwnerInfo
   * @return BwUser
   */
  public BwUser getUserOwner(PrincipalHref key) {
    return (BwUser)get(key);
  }
}