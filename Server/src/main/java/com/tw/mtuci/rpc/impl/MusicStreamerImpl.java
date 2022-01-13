package com.tw.mtuci.rpc.impl;

import com.tw.mtuci.rpc.Server;
import com.tw.mtuci.rpc.interfaces.MusicStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@WebService(endpointInterface = "com.tw.mtuci.rpc.interfaces.MusicStreamer")
public class MusicStreamerImpl implements MusicStreamer {

    private Map<Long, DataOutputStream> clients;
    private int BUFFER_SIZE = 128000;
    private final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private Long SUCCESS;
    private Long FAIL = -1L;
    private Connection conn = null;


    public MusicStreamerImpl() {
        Properties connectionProps = new Properties();
        connectionProps.put("user", "test");
        connectionProps.put("password", "test");

        try {
            conn = DriverManager.getConnection("", connectionProps);
            if (conn == null) {
                throw new Exception("Invalid DataBase");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        clients = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(500500);

        while (true) {
            try {
                Socket client = serverSocket.accept();
                BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                Long res = logIn(inputStreamReader, client.getRemoteSocketAddress().toString());
                if (!Objects.equals(res, FAIL)) {
                    DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                    clients.put(SUCCESS, dataOutputStream);
                }

            } catch (IOException e) {
                LOGGER.error("Connection Failed");
            }
        }
    }

    public Long logIn(BufferedReader inputStreamReader, String remoteSocketAddress) {
        try {
            String login = inputStreamReader.readLine();
            String password = inputStreamReader.readLine();

            LOGGER.info("Attempt to login with pair: " +
                            "login={}, " +
                            "password={} " +
                            "from address={}",
                    new Object[]{
                            login,
                            password,
                            remoteSocketAddress
                    }
            );
            try (ResultSet cursor = conn
                    .prepareStatement("SELECT Login, Password FROM USERS WHERE Login='" + login + "' AND Password='" + password + "'")
                    .executeQuery()) {
                while (cursor.next()) {
                    if (Objects.equals(cursor.getString("Login"), login) && Objects.equals(cursor.getString("Password"), password)) {
                        SUCCESS = (long) (login.hashCode() + password.hashCode());
                        return SUCCESS;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FAIL;
    }

    @Override
    public boolean streamMusic(long songHash, long userToken) {
        String strFilename = "";
        String sqlStatement = "SELECT FilePath FROM MUSIC_PATHS WHERE FileHash='" + songHash + "'";

        try(ResultSet cursor = conn.prepareStatement(sqlStatement).executeQuery()) {
            while(cursor.next()){
                strFilename = cursor.getString("FilePath");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            if(strFilename.isEmpty()){
                return false;
            }
            File soundFile = new File(strFilename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            int nBytesRead = 0;

            byte[] abData = new byte[BUFFER_SIZE];
            DataOutputStream stream = clients.get(userToken);
            while (nBytesRead != -1) {
                nBytesRead = audioStream.read(abData, 0, BUFFER_SIZE);
                if (nBytesRead >= 0) {
                    stream.write(abData, 0, nBytesRead);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String[] getListOfAvailableSongs() {
        return new String[0];
    }

    @Override
    public boolean register(String login, String password) {
        return false;
    }
}
