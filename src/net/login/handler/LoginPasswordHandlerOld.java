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
package net.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import java.util.Calendar;
import net.MaplePacketHandler;
import net.login.LoginServer;
import tools.DatabaseConnection;

public final class LoginPasswordHandlerOld implements MaplePacketHandler {
    @Override
    public boolean validateState(MapleClient c) {
            return !c.isLoggedIn();
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int loginok = 0;
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        c.setAccountName(login);
        final boolean isBanned = c.hasBannedIP() || c.hasBannedMac();
        loginok = c.login(login, pwd, isBanned);
        Calendar tempbannedTill = c.getTempBanCalendar();
        if (loginok == 0 && isBanned) {
            loginok = 3;
            MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Mac/IP Re-ban", false);
        } else if (loginok == 5) { // 자동가입
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (`name`, `password`) VALUES (?, ?);");
                ps.setString(1, login);
                ps.setString(2, pwd);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
            }
            c.getSession().write(MaplePacketCreator.serverNotice(1, "가입이 완료되었습니다. 기본 주민등록번호는 1234567 입니다. RedeemerMS 에 오신 것을 환영합니다!"));
            c.getSession().write(MaplePacketCreator.getLoginFailed(30));
            return;
        } else if (loginok != 0) {
            c.getSession().write(MaplePacketCreator.getLoginFailed(loginok));
            return;
        } 
        if (tempbannedTill.getTimeInMillis() != 0) {
            long tempban = MaplePacketCreator.getTempBanTimestamp(tempbannedTill.getTimeInMillis());
            byte reason = c.getBanReason();
            c.getSession().write(MaplePacketCreator.getTempBan(tempban, reason));
            return;
        }
        if (c.finishLogin() == 0) {
            c.getSession().write(MaplePacketCreator.getAuthSuccessRequestPin(c, c.getAccountName()));
            try {
                c.getSession().write(MaplePacketCreator.getServerList(0, "리디머", LoginServer.getInstance().getWorldInterface().getChannelLoad(0)));
            } catch (RemoteException e) {
            }
            c.getSession().write(MaplePacketCreator.getEndOfServerList());
            c.getSession().write(MaplePacketCreator.getLastSelectedWorld());
            c.getSession().write(MaplePacketCreator.sendRecommendedServers());
            final MapleClient client = c;
            c.setIdleTask(TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    client.getSession().close(true);
                }
            }, 600000));
        } else {
            c.getSession().write(MaplePacketCreator.getLoginFailed(7));
        }
    }
}
