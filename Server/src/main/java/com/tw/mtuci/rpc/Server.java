package com.tw.mtuci.rpc;

import com.tw.mtuci.rpc.impl.MusicStreamerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Endpoint;

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        MusicStreamerImpl musicStreamer = new MusicStreamerImpl();
        musicStreamer.start();
        LOGGER.info("Server is online");
        Endpoint.publish("http://25.71.127.55:2620/rpc/GetValue", musicStreamer);
    }
}