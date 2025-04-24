package com.findata.mainapplication.Abstract;


import com.findata.mainapplication.model.Rate;

/**
 * PlatformConnector'ların Coordinator'a olayları bildirmek için kullanacağı arayüz.
 */
public interface CoordinatorCallback {

    void onConnect(String platformName);

    void onDisconnect(String platformName);

    void onRateUpdate(Rate rate);

    void onError(String platformName, String error);

}