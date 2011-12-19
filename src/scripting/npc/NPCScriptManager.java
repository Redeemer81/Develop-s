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
package scripting.npc;

import java.util.HashMap;
import java.util.Map;
import javax.script.Invocable;
import client.MapleClient;
import client.MapleCharacter;
import client.ScriptDebug;
import java.lang.reflect.UndeclaredThrowableException;
import scripting.AbstractScriptManager;
import tools.PrimitiveLogger;

/**
 *
 * @author Matze
 */
public class NPCScriptManager extends AbstractScriptManager {
    private Map<MapleClient, NPCConversationManager> cms = new HashMap<MapleClient, NPCConversationManager>();
    private Map<MapleClient, NPCScript> scripts = new HashMap<MapleClient, NPCScript>();
    private static NPCScriptManager instance = new NPCScriptManager();

    public synchronized static NPCScriptManager getInstance() {
        return instance;
    }

    public void start(MapleClient c, int npc, String filename, MapleCharacter chr) {
        try {
            NPCConversationManager cm = new NPCConversationManager(c, npc);
            if (cms.containsKey(c)) {
                return;
            }
            cms.put(c, cm);
            Invocable iv = null;
            String path = "";
            if (filename != null) {
                path = "npc/" + filename + ".js";
                iv = getInvocable("npc/" + filename + ".js", c);
            }
            if (iv == null) {
                path = "npc/" + npc + ".js";
                iv = getInvocable("npc/" + npc + ".js", c);
            }
            if (iv == null || NPCScriptManager.getInstance() == null) {
                dispose(c);
                return;
            }
            engine.put("cm", cm);
            NPCScript ns = iv.getInterface(NPCScript.class);
            scripts.put(c, ns);
            c.setScriptDebug(new ScriptDebug(c, path, cm));
            if (chr == null) {
                ns.start();
            } else {
                ns.start(chr);
            }
        } catch (UndeclaredThrowableException ute) {
            ute.printStackTrace();
            System.out.println("Error: NPC " + npc + ". UndeclaredThrowableException.");
            PrimitiveLogger.logNPCException(String.valueOf(getCM(c).getNpc()), ute);
            dispose(c);
            cms.remove(c);
            notice(c);
        } catch (Exception e) {
            System.out.println("Error: NPC " + npc + ".");
            PrimitiveLogger.logNPCException(String.valueOf(getCM(c).getNpc()), e);
            dispose(c);
            cms.remove(c);
            notice(c);
        }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) {
        NPCScript ns = scripts.get(c);
        if (ns != null) {
            try {
                ns.action(mode, type, selection);
            } catch (UndeclaredThrowableException ute) {
                System.out.println("Error: NPC " + getCM(c).getNpc() + ". UndeclaredThrowableException.");
                ute.printStackTrace();
                PrimitiveLogger.logNPCException(String.valueOf(getCM(c).getNpc()), ute);
                dispose(c);
                notice(c);
            } catch (Exception e) {
                System.out.println("Error: NPC " + getCM(c).getNpc() + ".");
                PrimitiveLogger.logNPCException(String.valueOf(getCM(c).getNpc()), e);
                dispose(c);
                notice(c);
            }
        }
    }

    public void dispose(NPCConversationManager cm) {
        MapleClient client = cm.getClient();
        cms.remove(client);
        scripts.remove(client);
        resetContext("npc/" + cm.getNpc() + ".js", cm.getClient());
        if(client.getScriptDebug() != null)
            client.getScriptDebug().empty();
        cm = null;
    }

    public void dispose(MapleClient c) {
        if (cms.get(c) != null) {
            dispose(cms.get(c));
        }
    }

    public NPCConversationManager getCM(MapleClient c) {
        return cms.get(c);
    }

    private void notice(MapleClient c) {
        c.getPlayer().dropMessage(1, "This NPC is not working properly. Please report it.");
    }
}
