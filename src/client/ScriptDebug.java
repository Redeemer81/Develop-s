/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;
import server.TimerManager;
import scripting.npc.NPCConversationManager;
import java.util.concurrent.ScheduledFuture;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;
import tools.PrimitiveLogger;

/**
 *
 * @author Simon
 */
public class ScriptDebug {
    private MapleClient client;
    private String path;
    private ScheduledFuture timeout;
    private NPCConversationManager cm;

    public ScriptDebug(MapleClient client, String path, NPCConversationManager cm)
    {
        this.client = client;
        this.path = path;
        this.cm = cm;
        InitiateTimeoutTask();
    }

    private void InitiateTimeoutTask() {
        timeout = (TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                logAsFaultyAndDispose();
            }
        }, 300000)); //5min timeout = 300000ms. testing: 5000ms
    }

    private void logAsFaultyAndDispose()
    {
       PrimitiveLogger.logScriptTimeout(path);
       cm.dispose();
    }

    public void empty()
    {
        this.client.setScriptDebug(null);
        this.client = null;
        this.cm = null;
        this.timeout.cancel(false);
    }
}
