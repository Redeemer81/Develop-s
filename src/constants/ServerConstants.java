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
package constants;

public class ServerConstants {
    public static final int EXP_RATE = 50;
    public static final int MESO_RATE = 5;
    public static final byte DROP_RATE = 2;
    public static final byte BOSS_DROP_RATE = 1;
    public static final byte NUM_WORLDS = 1;
    public static final byte FLAG = 1;
    public static final int CHANNEL_NUMBER = 2;
    public static final int CHANNEL_LOAD = 175;
    public static final String EVENT_MESSAGE = "#bRedeemer's #rDevelopment";
    public static final String RECOMMENDED_MESSAGE = "Redeemer(mrredeemer@naver.com) KMS Beta Test";
    public static final long RANKING_INTERVAL = 3600000;
    public static String SERVER_MESSAGE = "RedeemerMS Beta Test";
    public static final String EVENTS = "";
    public static String HOST = "180.68.242.3";
    public static final boolean DEBUG = false;
    public static String DATABASE_URL = "jdbc:mysql://localhost:3306/redeemer?autoReconnect=true&maxReconnects=999&useUnicode=true&characterEncoding=euckr";
    public static String DATABASE_USER = "root";
    public static String DATABASE_PASS = "v101";
    public static final short maple_version = 148;
    public static final byte minor_version = 4;
}