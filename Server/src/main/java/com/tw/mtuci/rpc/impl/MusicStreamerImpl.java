package com.tw.mtuci.rpc.impl;

import com.tw.mtuci.rpc.Server;
import com.tw.mtuci.rpc.interfaces.MusicStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@WebService(endpointInterface = "com.tw.mtuci.rpc.interfaces.MusicStreamer")
public class MusicStreamerImpl implements MusicStreamer {

    private Map<Long, DataOutputStream> listeners;

    private final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private Long SUCCESS;
    private Long FAIL = -1L;

    public void start() throws IOException {
        listeners = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(500500);

        while (true) {
            try {
                Socket listener = serverSocket.accept();
                DataOutputStream dataOutputStream = new DataOutputStream(listener.getOutputStream());
                Long clientHash = (long) listener.getRemoteSocketAddress().hashCode();
                listeners.put(clientHash, dataOutputStream);
            } catch (IOException e) {
                LOGGER.error("Connection Failed");
            }
        }
    }

    @Override
    public Long logIn(String remoteSocketAddress, String login, String password) {
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

        /*ToDo
            Добавить конекшен к базе данных и проверку логина.
            Если логин или паролт не валидны, то убрать listnera из списка.
            И вернуть FAIL
            В качестве SUCCESS вернуть пользователю сгенерированный токен
            По токену и  будут происходить все операции
        */
        return SUCCESS;
    }

    @Override
    public boolean streamMusic(long songHash, long userToken) {
        String strFilename = "";
        /*ToDo
            Добавить получение пути к файлу по хэшу
            Из базы данных
         */
        try {
            File soundFile = new File(strFilename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            int nBytesRead = 0;
            int BUFFER_SIZE = 128000;
            byte[] abData = new byte[BUFFER_SIZE];
            DataOutputStream stream = listeners.get(userToken);
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
