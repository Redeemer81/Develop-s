/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.world.remote;
import java.io.Serializable;
/**
 *
 * @author Simon
 */
public class EventInfo implements Serializable {
    static final long serialVersionUID = 6238543897846112358L;
    private String GMName = "";
    private String description = "";
    private int mapId = -1;
    private int channel = -1;
    private int world = -1;
    private boolean active = false;

    //constructors
    public EventInfo() //empty constructor for default
    {
    }

    public EventInfo(String GMName, String desc, int mapID, int channel, int world)
    {
        this.GMName = GMName;
        this.description = desc;
        this.mapId = mapID;
        this.channel = channel;
        this.world = world;
        this.active = true;
        System.out.println("New event with GMNAME: " + GMName + " desc: " + desc + " mapid "  + mapID + " channel "
                + channel + " world " + world + " created.");
    }

    //get

    public String getGMName()
    {
        return GMName;
    }

    public String getDescription()
    {
        return description;
    }

    public int getMapId()
    {
        return mapId;
    }

    public int getChannel()
    {
        return channel;
    }

    public int getWorld()
    {
        return world;
    }

    public boolean isActive()
    {
        return this.active;
    }

    //set

    public void setGMName(String name)
    {
        this.GMName = name;
    }

    public void setDescription(String desc)
    {
        this.description = desc;
    }

    public void setMapId(int mapId)
    {
        this.mapId = mapId;
    }

    public void setChannel(int chan)
    {
        this.channel = chan;
    }

    public void setWorld(int world)
    {
        this.world = world;
    }

    public void setIsActive(boolean active)
    {
        this.active = active;
    }
}