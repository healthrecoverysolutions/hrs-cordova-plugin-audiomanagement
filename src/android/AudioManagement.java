package com.hrs.audiomanagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.R;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;

import timber.log.Timber;

public class AudioManagement extends CordovaPlugin {

  public static final String ACTION_SET_MODE = "setAudioMode";
  public static final String ACTION_GET_MODE = "getAudioMode";
  public static final String ACTION_SET_VOLUME = "setVolume";
  public static final String ACTION_GET_VOLUME = "getVolume";
  public static final String ACTION_GET_MAX_VOLUME = "getMaxVolume";

  private static final int SILENT_MODE = 0;
  private static final int VIBRATE_MODE = 1;
  private static final int NORMAL_MODE = 2;

  private static final int TYPE_RING = 0;
  private static final int TYPE_MUSIC = 1;
  private static final int TYPE_NOTIFICATION = 2;
  private static final int TYPE_SYSTEM = 3;
  private static final int TYPE_VOICE_CALL = 4;

  private static final int SCALED_MAX_VOLUME = 100;

  private static final String LABEL_NORMAL = "Normal";
  private static final String LABEL_SILENT = "Silent";
  private static final String LABEL_VIBRATE = "Vibrate";

  private static final String KEY_AUDIO_MODE = "audioMode";
  private static final String KEY_LABEL = "label";
  private static final String KEY_VOLUME = "volume";
  private static final String KEY_SCALED_VOLUME = "scaledVolume";
  private static final String KEY_MAX_VOLUME = "maxVolume";

  private AudioManager manager;
  private Context context;
  private NotificationManager notificationManager;

  private int maxVolumeRing;
  private int maxVolumeSystem;
  private int maxVolumeNotification;
  private int maxVolumeMusic;
  private int maxVolumeVoiceCall;

  public void pluginInitialize(){
    this.manager = (AudioManager) this.cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
    this.maxVolumeRing = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
    this.maxVolumeNotification = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    this.maxVolumeSystem = manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
    this.maxVolumeMusic = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    this.maxVolumeVoiceCall = manager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
    this.context = this.cordova.getActivity().getApplicationContext();
    this.notificationManager = (NotificationManager) this.context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Timber.v("execute action = " + action);
    boolean returnValue = true;

    if(ACTION_SET_MODE.equals(action)){

      final int content = args.getInt(0);

      if(setAudioMode(content)){
        callbackContext.success();
      }else{
        callbackContext.error("Unknown audio mode !");
        returnValue =  false;
      }

    } else if(ACTION_GET_MODE.equals(action)){

      getAudioMode(callbackContext);

    } else if(ACTION_GET_VOLUME.equals(action)){

      final int type = args.getInt(0);

      final int volume = getVolume(type);

      if(volume != -1){
        JSONObject vol = new JSONObject();
        final int scaledVolume = getVolumePercentage(volume, getMaxVolumeValue(type));
        vol.put(KEY_VOLUME, volume);
        vol.put(KEY_SCALED_VOLUME, scaledVolume);
        callbackContext.success(vol);
      }else{
        callbackContext.error("Unknown volume type !");
        returnValue =  false;
      }
    } else if(ACTION_SET_VOLUME.equals(action)){

      final int type = args.getInt(0);
      final int volume = args.getInt(1);
      final boolean scaled = args.optBoolean(2);
      setVolume(type, volume, scaled, callbackContext);

    }  else if(ACTION_GET_MAX_VOLUME.equals(action)){

      final int type = args.getInt(0);

      final int max = getMaxVolumeValue(type);

      if (max != -1) {
        JSONObject maxVol = new JSONObject();
        maxVol.put(KEY_MAX_VOLUME, max);
        callbackContext.success(maxVol);
      } else {
        String errorMessage = "Unknown volume type! " + type;
        Timber.w(errorMessage);
        callbackContext.error(errorMessage);
        returnValue =  false;
      }

    }  else {
      callbackContext.error("AudioManagement." + action + " not found !");
      returnValue =  false;
    }

    return returnValue;
  }

  private int getVolume(int type) {
    Timber.d("getVolume() type = " + type);

    int volume = -1;

    switch(type){
      case TYPE_RING:
        volume = manager.getStreamVolume(AudioManager.STREAM_RING);
        break;
      case TYPE_NOTIFICATION:
        volume = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        break;
      case TYPE_SYSTEM:
        volume = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        break;
      case TYPE_MUSIC:
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        break;
      case TYPE_VOICE_CALL:
        volume = manager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        break;
    }

    return volume;
  }

  private void setVolume(final int type, final int volume, final boolean scaled, final CallbackContext callbackContext){
    new Runnable(){
      @Override
      public void run() {
        Timber.d("setVolume() type = " + type + ", volume = " + volume);
        int HIDE_FLAG_UI = 0;
        switch(type){
          case TYPE_RING:
            manager.setStreamVolume(AudioManager.STREAM_RING, sanitizeVolume(volume, maxVolumeRing, scaled), HIDE_FLAG_UI);
            break;
          case TYPE_NOTIFICATION:
            manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, sanitizeVolume(volume, maxVolumeNotification, scaled), HIDE_FLAG_UI);
            break;
          case TYPE_SYSTEM:
            manager.setStreamVolume(AudioManager.STREAM_SYSTEM, sanitizeVolume(volume, maxVolumeSystem, scaled), HIDE_FLAG_UI);
            break;
          case TYPE_MUSIC:
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, sanitizeVolume(volume, maxVolumeMusic, scaled), HIDE_FLAG_UI);
            break;
          case TYPE_VOICE_CALL:
            manager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, sanitizeVolume(volume, maxVolumeVoiceCall, scaled), HIDE_FLAG_UI);
            break;
          default:
            String errorMessage = "Unknown type " + type;
            Timber.w(errorMessage);
            callbackContext.error(errorMessage);
            return;
        }
        callbackContext.success();
      }
    }.run();
  }

  private void getAudioMode(CallbackContext callbackContext) throws JSONException {
    Timber.d("getAudioMode()");

    final JSONObject mode = new JSONObject();
    int audioMode = NORMAL_MODE;
    String label = "Normal";

    switch(this.manager.getRingerMode()){
      case AudioManager.RINGER_MODE_SILENT:
        audioMode = SILENT_MODE;
        label = LABEL_SILENT;
        break;
      case AudioManager.RINGER_MODE_VIBRATE:
        audioMode = VIBRATE_MODE;
        label = LABEL_VIBRATE;
        break;
      case AudioManager.RINGER_MODE_NORMAL:
        audioMode = NORMAL_MODE;
        label = LABEL_NORMAL;
        break;
    }

    mode.put(KEY_AUDIO_MODE, audioMode);
    mode.put(KEY_LABEL, label);

    callbackContext.success(mode);
  }

  private boolean setAudioMode(int mode){
    Timber.d("setAudioMode() mode = " + mode);
    switch(mode){
      case SILENT_MODE:
        if(manager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
          manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
        return true;
      case VIBRATE_MODE:
        if(manager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE){
          manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        return true;
      case NORMAL_MODE:
        if(manager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL){
          manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        return true;
      default:
        return false;
    }
  }

  private int getMaxVolumeValue(int type){
    Timber.d("getMaxVolumeValue() type = " + type);
    int max = -1;

    switch(type){
      case TYPE_RING:
        max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        break;
      case TYPE_NOTIFICATION:
        max = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        break;
      case TYPE_SYSTEM:
        max = manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        break;
      case TYPE_MUSIC:
        max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        break;
      case TYPE_VOICE_CALL:
        max = manager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        break;
    }

    return max;
  }

  private int sanitizeVolume(int sourceVolume, int maxVolume, boolean scaled){
    return scaled ? interpolateVolume(sourceVolume, maxVolume) : clamp(sourceVolume, 0, maxVolume);
  }

  private int interpolateVolume(int sourceVolume, int maxVolume){
    return interpolate(sourceVolume, 0, SCALED_MAX_VOLUME, 0, maxVolume);
  }

  private int getVolumePercentage(int sourceVolume, int maxVolume){
    return interpolate(sourceVolume, 0, maxVolume, 0, SCALED_MAX_VOLUME);
  }

  // Forces `value` into range [`min`, `max`]
  private static int clamp(int value, int min, int max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
  }

  /**
   * Scales `value` from range [`sourceMin`, `sourceMax`] to range [`destMin`, `destMax`]
   *
   * Example:
   * interpolate(50, 0, 100, 0, 50); // 25, because 50% of 50
   * interpolate(75, 0, 100, -2, 2); // 1, because it is 75% of the total range (negative side included)
   */
  private static int interpolate(int value, int sourceMin, int sourceMax, int destMin, int destMax) {
    double ratio = clamp(value, sourceMin, sourceMax) / (double)(sourceMax - sourceMin);
    int result = (int) Math.round(ratio * (destMax - destMin)) + destMin;
    return clamp(result, destMin, destMax);
  }
}
