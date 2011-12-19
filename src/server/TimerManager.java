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
package server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import client.messages.MessageCallback;

public class TimerManager implements TimerManagerMBean {
    private static TimerManager instance = new TimerManager();
    private ScheduledThreadPoolExecutor ses;

    private TimerManager() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mBeanServer.registerMBean(this, new ObjectName("server:type=TimerManger"));
        } catch (Exception e) {
        }
    }

    public static TimerManager getInstance() {
        return instance;
    }

    public void start() {
        if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
            return;
        }
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("TimerManager-Worker-" + threadNumber.getAndIncrement());
                return t;
            }
        });
        stpe.setKeepAliveTime(5, TimeUnit.MINUTES);
        stpe.allowCoreThreadTimeOut(true);
        stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        ses = stpe;
    }

    public void stop() {
        ses.shutdown();
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime) {
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable r, long delay) {
        return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
        return schedule(r, timestamp - System.currentTimeMillis());
    }

    @Override
    public long getActiveCount() {
        return ses.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return ses.getCompletedTaskCount();
    }

    @Override
    public int getQueuedTasks() {
        return ses.getQueue().toArray().length;
    }

    @Override
    public long getTaskCount() {
        return ses.getTaskCount();
    }

    @Override
    public boolean isShutdown() {
        return ses.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return ses.isTerminated();
    }

    public void removeTask(Runnable taskToRemove)
    {
        ses.remove(taskToRemove);
    }

    public void purgeTM()
    {
        ses.purge();
    }

    public void dropDebugInfo(MessageCallback callback) {
		StringBuilder builder = new StringBuilder();
		builder.append("Terminated: ");
		builder.append(ses.isTerminated());
		builder.append(" Shutdown: ");
		builder.append(ses.isShutdown());
		callback.dropMessage(builder.toString());

		builder = new StringBuilder();
		builder.append("Completed Tasks: ");
		builder.append(ses.getCompletedTaskCount());
		builder.append(" Active Tasks: ");
		builder.append(ses.getActiveCount());
		builder.append(" Task Count: ");
		builder.append(ses.getTaskCount());
		callback.dropMessage(builder.toString());

		builder = new StringBuilder();
		builder.append("Queued Tasks: ");
		builder.append(ses.getQueue().toArray().length);
		callback.dropMessage(builder.toString());
	}

    private static class LoggingSaveRunnable implements Runnable {
        Runnable r;

        public LoggingSaveRunnable(Runnable r) {
            this.r = r;
        }

        @Override
        public void run() {
            try {
                r.run();
            } catch (Throwable t) {
            }
        }
    }
}