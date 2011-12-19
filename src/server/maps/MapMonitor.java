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

import java.util.concurrent.ScheduledFuture;
import server.MaplePortal;
import server.TimerManager;
import tools.MaplePacketCreator;

public final class MapMonitor {
    private MapleMap map;
    private MaplePortal portal;
    private MapleReactor portalReactor;
    private ScheduledFuture<?> timer;

    public MapMonitor(MapleMapFactory mapleMapFactory, MapleMap mapleMap, String portalName, int portalMapId, String portalReactorName) {
        map = mapleMap;
        if (portalName != null) {
            MapleMap portalMap = mapleMapFactory.getMap(portalMapId);
            portal = portalMap.getPortal(portalName);
            portal.setPortalStatus(MaplePortal.CLOSED);
            if (portalReactorName != null) {
                portalReactor = portalMap.getReactorByName(portalReactorName);
                portalReactor.setState((byte) 1);
                portalMap.broadcastMessage(MaplePacketCreator.triggerReactor(portalReactor, 1));
            } else {
                portalReactor = null;
            }
        } else {
            portal = null;
            portalReactor = null;
        }
        timer = TimerManager.getInstance().register(new Runnable() {
            public void run() {
                if (map.getCharacters().size() < 1) {
                    timer.cancel(false);
                    map.killAllMonsters();
                    map.resetReactors();
                    if (portal != null) {
                        portal.setPortalStatus(MaplePortal.OPEN);
                        if (portalReactor != null) {
                            portalReactor.setState((byte) 0);
                            portalReactor.getMap().broadcastMessage(MaplePacketCreator.triggerReactor(portalReactor, 0));
                        }
                    }
                }
            }
        }, 3000);
    }
}
