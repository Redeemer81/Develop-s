/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.messages.commands;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.MessageCallback;
import client.MapleClient;
import client.messages.IllegalCommandSyntaxException;
import java.util.ArrayList;
import net.channel.ChannelServer;
import client.MapleCharacter;
import java.rmi.RemoteException;
import server.PropertiesTable;

/**
 *
 * @author Simon
 */
public class PropertiesCommands implements Command {

      	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();

            if (splitted[0].equalsIgnoreCase("!set")) {
                 ArrayList<String> propNames;
                if(splitted.length != 4 || !(splitted[3].equalsIgnoreCase("on") || splitted[3].equalsIgnoreCase("off")) || !(splitted[1].equalsIgnoreCase("world") || splitted[1].equalsIgnoreCase("map")))
                {
                    c.getPlayer().dropMessage("Syntax helper: !set <map / world> <property> on / off");
                    return;
                }
                 else
                {
                    boolean world = splitted[1].equalsIgnoreCase("world");
                    try
                    {
                        if (world)
                            propNames = c.getChannelServer().getWorldRegistry().getPropertyNames();
                        else
                            propNames = c.getPlayer().getMap().getProperties().getPropertyNames();

                        if(propNames.contains(splitted[2]))
                        {
                            if (world)
                                c.getChannelServer().getWorldRegistry().setProperty(splitted[2], Boolean.valueOf(splitted[3].equalsIgnoreCase("on")));
                            else
                                c.getPlayer().getMap().getProperties().setProperty(splitted[2], Boolean.valueOf(splitted[3].equalsIgnoreCase("on")));
                            player.dropMessage("Property " + splitted[2] + " now changed to: " + splitted[3]);
                        } else {
                            player.dropMessage("Incorrect parameter. Current properties: ");
                            for(String s : propNames)
                                player.dropMessage(s);
                        }
                    } catch (RemoteException re)
                    {
                        cserv.reconnectWorld();
                    }
                 }

             }

            else if(splitted[0].equalsIgnoreCase("!get"))
            {
                if(splitted.length != 3 || !(splitted[1].equalsIgnoreCase("world") || splitted[1].equalsIgnoreCase("map")))
                {
                    c.getPlayer().dropMessage("Syntax helper: !get <map / world> <property>");
                    return;
                }
                    boolean world = splitted[1].equalsIgnoreCase("world");
                    try
                    {
                       // PropertiesTable properties = world ? c.getChannelServer().getWorldRegistry().getProperties() : c.getPlayer().getMap().getProperties();
                        ArrayList<String> propNames;
                        Object value;
                        if (world)
                        {
                            propNames = c.getChannelServer().getWorldRegistry().getPropertyNames();
                            value = c.getChannelServer().getWorldRegistry().getProperty(splitted[2]);
                        }
                        else
                        {
                            propNames = c.getPlayer().getMap().getProperties().getPropertyNames();
                            value = c.getPlayer().getMap().getProperties().getProperty(splitted[2]);
                        }
                        
                        if(propNames.contains(splitted[2]))
                        {
                            player.dropMessage("Property " + splitted[2] + " has value: " + (value.equals(Boolean.TRUE) ? "on" : "off"));
                        } else {
                            player.dropMessage("Property not found, please try again or use !listproperties.");
                        }
                    } catch (RemoteException re)
                    {
                        re.printStackTrace();
                        cserv.reconnectWorld();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }
            else if(splitted[0].equalsIgnoreCase("!listproperties"))
            {
                boolean world = splitted[1].equalsIgnoreCase("world");
                if(splitted.length != 2 || !(splitted[1].equalsIgnoreCase("world") || splitted[1].equalsIgnoreCase("map")))
                {
                    c.getPlayer().dropMessage("Syntax helper: !listproperties <map / world>");
                    return;
                }
                    try
                    {
                        ArrayList<String> propNames;
                        Object value;
                        
                        if (world)
                            propNames = c.getChannelServer().getWorldRegistry().getPropertyNames();
                        else
                            propNames = c.getPlayer().getMap().getProperties().getPropertyNames();

                        for(String s : propNames)
                        {
                            value = world ? c.getChannelServer().getWorldRegistry().getProperty(s) : c.getPlayer().getMap().getProperties().getProperty(s);
                            player.dropMessage("Property " + s + " has value " + (value.equals(Boolean.TRUE) ? "on" : "off"));
                        }
                    } catch (RemoteException re)
                    {
                        cserv.reconnectWorld();
                    }
            }
        }

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("set", "<map / world> <property name> <value>", "Sets the value of te specified property", 1),
			new CommandDefinition("get", "<map / world> <property name>", "Gets the value of the specified property", 1),
			new CommandDefinition("listproperties", "<map / world>", "Lists the available properties and their current values in the specified scope", 1),
		};
	}
}
