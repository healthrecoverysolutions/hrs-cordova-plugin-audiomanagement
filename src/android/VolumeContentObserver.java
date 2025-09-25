package com.hrs.audiomanagement;

import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;


class VolumeContentObserver extends ContentObserver {
    private final AudioManager audioManager;
    @Nullable
    private final CallbackContext callbackContext;

    public VolumeContentObserver(Handler handler, AudioManager audioManager, @Nullable CallbackContext callbackContext) {
        super(handler);
        this.audioManager = audioManager;
        this.callbackContext = callbackContext;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if (callbackContext != null) {
            try {
                JSONObject volumeInfo = getVolumeInfo();

                PluginResult result = new PluginResult(PluginResult.Status.OK, volumeInfo);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            } catch (JSONException e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR,
                    "Error getting volume info: " + e.getMessage());
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        }
    }

    private JSONObject getVolumeInfo() throws JSONException {
        JSONObject volumeInfo = new JSONObject();

        int musicVolume = Utils.getVolume(audioManager, Utils.TYPE_MUSIC);
        int ringVolume = Utils.getVolume(audioManager, Utils.TYPE_RING);

        volumeInfo.put("musicVolume", musicVolume);
        volumeInfo.put("ringVolume", ringVolume);

        return volumeInfo;
    }
}
