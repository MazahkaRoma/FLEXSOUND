package com.tw.mtuci.rpc.interfaces;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


/**
 * Интерфейс для всех сервисов, каждый сервис должен одержать этот метод
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface MusicStreamer {

    @WebMethod
    boolean streamMusic(long songHash, long userId);

    @WebMethod
    String[] getListOfAvailableSongs();

    @WebMethod
    boolean register(String login, String password);
}
