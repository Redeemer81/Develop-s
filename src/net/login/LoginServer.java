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
package net.login;

import constants.ServerConstants;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import tools.DatabaseConnection;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.ServerMode;
import net.ServerMode.Mode;
import net.mina.MapleCodecFactory;
import net.world.remote.WorldLoginInterface;
import net.world.remote.WorldRegistry;
import server.TimerManager;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;

public class LoginServer implements Runnable {
    private IoAcceptor acceptor;
    private static WorldRegistry worldRegistry = null;
    private static Map<Integer, Map<Integer, String>> channelServer = new HashMap<Integer, Map<Integer, String>>();
    private LoginWorldInterface lwi;
    private WorldLoginInterface wli;
    private Boolean worldReady = Boolean.TRUE;
    private Properties subnetInfo = new Properties();
    private static LoginServer instance = new LoginServer();

    public static LoginServer getInstance() {
        return instance;
    }

    public void addChannel(int world, int channel, String ip) {
        if (!channelServer.containsKey(world)) {
            channelServer.put(world, new HashMap<Integer, String>());
        }
        channelServer.get(world).put(channel, ip);
    }

    public void removeChannel(int channel) {
        channelServer.remove(channel);
    }

    @Override
    public void run() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            lwi = new LoginWorldInterfaceImpl();
            wli = worldRegistry.registerLoginServer("releaselogin", lwi);
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.addBatch("UPDATE accounts SET loggedin = 0");
            stmt.addBatch("UPDATE characters SET HasMerchant = 0");
            stmt.executeBatch();
            stmt.close();
        } catch (RemoteException e) {
            throw new RuntimeException("Could not connect to world server.", e);
        } catch (NotBoundException ex) {
        } catch (SQLException sql) {
        }
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        MapleServerHandler handler = new MapleServerHandler(PacketProcessor.getProcessor(PacketProcessor.Mode.LOGINSERVER));
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 120);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setKeepAlive(false);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setReuseAddress(true);
        acceptor.setHandler(handler);
        try {
            acceptor.bind(new InetSocketAddress(8484));
            System.out.println("로그인 서버 : 포트 8484");
        } catch (IOException ex) {
        }
    }

    public void shutdown() {
        try {
            worldRegistry.deregisterLoginServer(lwi);
        } catch (RemoteException e) {
        }
        TimerManager.getInstance().stop();
        System.out.println("Login Server offline.");
        System.exit(0);
    }

    public WorldLoginInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady) {
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return wli;
    }

    public static void main(String args[]) {
        ServerMode.setServerMode(Mode.LOGIN);
        LoginServer.getInstance().run();
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public String getIP(int world, int channel) {
        return channelServer.get(world).get(channel);
    }
}
