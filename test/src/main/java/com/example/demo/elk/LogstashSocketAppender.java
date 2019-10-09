package com.example.demo.elk;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.ZeroConfSupport;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/8/27 16:31
 * @Version 1.0
 */

public class LogstashSocketAppender extends AppenderSkeleton {

    private static final int DEFAULT_PORT = 4560;

    private static final int DEFAULT_RECONNECTION_DELAY = 30000;

    private static final String ZONE = "_log4j_obj_tcpconnect_appender.local.";

    private String ignoreThreadPrefix;
    private List<String> ignoreThreadPrefixList;
    private String application;
    private String remoteHost;
    private InetAddress address;
    private BufferedOutputStream oos;
    private int port = DEFAULT_PORT;
    private int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

    private LogstashSocketAppender.Connector connector;
    private boolean advertiseViaMulticastDNS;
    private ZeroConfSupport zeroConf;

    public LogstashSocketAppender() {
    }

    public LogstashSocketAppender(InetAddress address, int port) {
        this.address = address;
        this.remoteHost = address.getHostName();
        this.port = port;
        connect(address, port);
    }

    public LogstashSocketAppender(String host, int port) {
        this.port = port;
        this.address = getAddressByName(host);
        this.remoteHost = host;
        connect(address, port);
    }

    @Override
    public void activateOptions() {
        if (advertiseViaMulticastDNS) {
            zeroConf = new ZeroConfSupport(ZONE, port, getName());
            zeroConf.advertise();
        }
        connect(address, port);
    }

    @Override
    synchronized public void close() {
        if (closed) {
            return;
        }

        this.closed = true;
        if (advertiseViaMulticastDNS) {
            zeroConf.unadvertise();
        }

        cleanUp();
    }

    private void cleanUp() {
        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                if (e instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("Could not close oos.", e);
            }
            oos = null;
        }
        if (connector != null) {
            connector.interrupted = true;
            // allow gc
            connector = null;
        }
    }

    private void connect(InetAddress address, int port) {
        if (this.address == null) {
            return;
        }
        try {
            // First, close the previous connection if any.
            cleanUp();
            oos = new BufferedOutputStream(new Socket(address, port).getOutputStream());
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            String msg = "Could not connect to remote log4j server at ["
                    + address.getHostName() + "].";
            if (reconnectionDelay > 0) {
                msg += " We will try again later.";
                fireConnector(); // fire the connector thread
            } else {
                msg += " We are not retrying.";
                errorHandler.error(msg, e, ErrorCode.GENERIC_FAILURE);
            }
            LogLog.error(msg);
        }
    }

    @Override
    public void append(LoggingEvent event) {
        if (event == null || event.getThreadName() == null) {
            return;
        }

        if (ignoreThreadPrefixList.stream().anyMatch(prefix -> event.getThreadName().startsWith(prefix))) {
            LogLog.debug("Ignore the thread log, thread prefix in " + ignoreThreadPrefix);
            return;
        }

        if (address == null) {
            errorHandler.error("No remote host is set for SocketAppender named \"" +
                    this.name + "\".");
            return;
        }

        if (application != null) {
            event.setProperty("application", application);
        }

        this.layout.format(event);
    }

    public void setAdvertiseViaMulticastDNS(boolean advertiseViaMulticastDNS) {
        this.advertiseViaMulticastDNS = advertiseViaMulticastDNS;
    }

    public boolean isAdvertiseViaMulticastDNS() {
        return advertiseViaMulticastDNS;
    }

    void sendEvent(String data) throws IOException {
        if (StringUtils.isEmpty(data) || oos == null) {
            return;
        }
        oos.write(data.getBytes());
        oos.flush();
    }

    private void fireConnector() {
        if (connector == null) {
            LogLog.debug("Starting a new connector thread.");
            connector = new LogstashSocketAppender.Connector();
            connector.setDaemon(true);
            connector.setPriority(Thread.MIN_PRIORITY);
            connector.start();
        }
    }

    private static InetAddress getAddressByName(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (Exception e) {
            LogLog.error("Could not find address of [" + host + "].", e);
            return null;
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public String getIgnoreThreadPrefix() {
        return ignoreThreadPrefix;
    }

    public void setIgnoreThreadPrefix(String ignoreThreadPrefix) {
        this.ignoreThreadPrefix = ignoreThreadPrefix;

        if (StringUtils.isNotBlank(this.ignoreThreadPrefix)) {
            String[] prefixs = this.ignoreThreadPrefix.split(",", 2);
            ignoreThreadPrefixList = new ArrayList<>(prefixs.length);
            for (String prefix : prefixs) {
                ignoreThreadPrefixList.add(prefix.trim());
            }
        }
    }

    public void setRemoteHost(String host) {
        address = getAddressByName(host);
        remoteHost = host;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setApplication(String lapp) {
        this.application = lapp;
    }

    public String getApplication() {
        return application;
    }

    public void setReconnectionDelay(int delay) {
        this.reconnectionDelay = delay;
    }

    public int getReconnectionDelay() {
        return reconnectionDelay;
    }

    class Connector extends Thread {

        boolean interrupted = false;

        @Override
        public void run() {
            Socket socket;
            while (!interrupted) {
                try {
                    sleep(reconnectionDelay);
                    LogLog.debug("Attempting connection to " + address.getHostName());
                    socket = new Socket(address, port);
                    synchronized (this) {
                        oos = new BufferedOutputStream(socket.getOutputStream());
                        connector = null;
                        LogLog.debug("Connection established. Exiting connector thread.");
                        break;
                    }
                } catch (InterruptedException e) {
                    LogLog.debug("Connector interrupted. Leaving loop.");
                    return;
                } catch (java.net.ConnectException e) {
                    LogLog.debug("Remote host " + address.getHostName()
                            + " refused connection.");
                } catch (IOException e) {
                    if (e instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                    LogLog.debug("Could not connect to " + address.getHostName() +
                            ". Exception is " + e);
                }
            }
        }

    }

}


