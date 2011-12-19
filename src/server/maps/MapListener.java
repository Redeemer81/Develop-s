/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package server.maps;
import client.MapleCharacter;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author Jay
 */
public class MapListener{
    private MaplePortal portal;
    private MapleReactor portalReactor;
    private MapleMap map;
    private boolean active;

    public MapListener(MaplePortal portal, MapleReactor portalReactor, MapleMap listenerMap) {
        this.portal = portal;
        this.portalReactor = portalReactor;
        this.map = listenerMap;
        this.active = false;
    }

    public void playerExit(MapleCharacter chr) {
        if (map.getCharacters().size() - 1 < 1) {
            map.killAllMonsters();
            //map.clearDrops();
            map.resetReactors();
            togglePortal(true);
            this.active = false;
        }
    }

    private void togglePortal(boolean status) {
        if (portal != null) {
            portal.setPortalStatus(status);
            if (portalReactor != null) {
                portalReactor.setState((byte) (status ? 0 : 1));
                portalReactor.getMap().broadcastMessage(MaplePacketCreator.triggerReactor(portalReactor, portalReactor.getState()));
            }
        }
    }

    public void disableEntry()
    {
        togglePortal(false);
        this.active = true;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setIsActive(boolean isActive)
    {
        this.active = isActive;
    }
}
