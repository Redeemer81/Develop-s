package scripting.event;
import java.util.LinkedList;
import java.util.List;
import client.MapleCharacter;
import net.MaplePacket;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.world.remote.EventInfo;

/**
 * Event teams are designed to be like leaderless parties, with event-related methods
 * and event-related attributes, and without the MaplePartyCharacter crap which is unneeded for event
 * scripts.
 * @author Simon
 */
public class EventTeam {
    private int id = 0;
    private int kills = 0;
    private int deaths = 0;
    private int points = 0;
    private String name = "";
    private ReentrantReadWriteLock characterlock = new ReentrantReadWriteLock();
    private List<MapleCharacter> members = new LinkedList<MapleCharacter>();
    private EventInstanceManager eim;
    private EventInfo gmEvent;

    public EventTeam(int id, String name, EventInstanceManager eim)
    {
        this.id = id;
        this.eim = eim;
    }

    public EventTeam(int id, EventInfo gme)
    {
        this.id = id;
        this.gmEvent = gme;
    }

    public void broadcastMessage(MapleCharacter source, MaplePacket packet) {
        characterlock.readLock().lock();
        try
        {
            for (MapleCharacter chr : members) {
                if (chr != source) {
                    chr.getClient().getSession().write(packet);
                }
            }
        } finally {
            characterlock.readLock().unlock();
        }
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getKills()
    {
        return kills;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public int getPoints()
    {
        return points;
    }

    public EventInstanceManager getEIM()
    {
        return eim;
    }

    public void incrementKills()
    {
        kills++;
    }

    public void incrementDeaths()
    {
        deaths++;
    }

    public void incrementPoints()
    {
        points++;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setKills(int kills)
    {
        this.kills = kills;
    }

    public void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }

    public void setPoints(int points)
    {
        this.points = points;
    }

    public List<MapleCharacter> getMembers()
    {
        return members;
    }

    public void addCharacter(MapleCharacter toAdd)
    {
        characterlock.writeLock().lock();
        try {
            members.add(toAdd);
            toAdd.setEventTeam(this);
        } finally {
            characterlock.writeLock().unlock();
        }
    }

    public void removeCharacter(MapleCharacter toRemove)
    {
        characterlock.writeLock().lock();
        try {
            members.add(toRemove);
            toRemove.setEventTeam(null);
        } finally {
            characterlock.writeLock().unlock();
        }
    }

    public void dispose()
    {
        characterlock.writeLock().lock();
        try {
            for(MapleCharacter c : members)
                c.setEventTeam(null);
            members.clear();
            eim = null;
        } finally {
            characterlock.writeLock().unlock();
        }
    }

    public EventInfo getGMEventInfo()
    {
        return this.gmEvent;
    }
}
