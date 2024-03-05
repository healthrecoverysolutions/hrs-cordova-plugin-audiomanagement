package com.hrs.audiomanagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.content.Context;
import android.media.AudioManager;

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

  private int maxVolumeRing;
  private int maxVolumeSystem;
  private int maxVolumeNotification;
  private int maxVolumeMusic;
  private int maxVolumeVoiceCall;

  public void pluginInitialize() {
    this.manager = (AudioManager) this.cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
    this.maxVolumeRing = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
    this.maxVolumeNotification = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    this.maxVolumeSystem = manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
    this.maxVolumeMusic = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    this.maxVolumeVoiceCall = manager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
  }

  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Timber.d("execute action = %s", action);

    if (ACTION_SET_MODE.equals(action)) {
      final int mode = args.getInt(0);
      setModeAction(mode, callbackContext);

    } else if (ACTION_GET_MODE.equals(action)) {
      getAudioMode(callbackContext);

    } else if (ACTION_GET_VOLUME.equals(action)) {
      final int type = args.getInt(0);
      getVolumeAction(type, callbackContext);

    } else if (ACTION_SET_VOLUME.equals(action)) {
      final int type = args.getInt(0);
      final int volume = args.getInt(1);
      final boolean scaled = args.optBoolean(2);
      setVolume(type, volume, scaled, callbackContext);

    } else if (ACTION_GET_MAX_VOLUME.equals(action)) {
      final int type = args.getInt(0);
      getMaxVolumeAction(type, callbackContext);

    } else {
      notifyActionError(callbackContext, "AudioManagement." + action + " not found !");
      return false;
    }

    return true;
  }

  private void notifyActionError(CallbackContext callbackContext, String errorMessage) {
      Timber.e(errorMessage);
      callbackContext.error(errorMessage);
  }

  private void setModeAction(int mode, CallbackContext callbackContext) {
      if(setAudioMode(mode)){
          callbackContext.success();
      } else {
          notifyActionError(callbackContext, "Unknown audio mode! " + mode);
      }
  }

  private void getVolumeAction(int type, CallbackContext callbackContext) throws JSONException {
      final int volume = getVolume(type);

      if(volume == -1){
          notifyActionError(callbackContext, "Unknown volume type! " + type);
          return;
      }

      JSONObject vol = new JSONObject();
      final int scaledVolume = getVolumePercentage(volume, getMaxVolumeValue(type));
      vol.put(KEY_VOLUME, volume);
      vol.put(KEY_SCALED_VOLUME, scaledVolume);
      callbackContext.success(vol);
  }

  private void getMaxVolumeAction(int type, CallbackContext callbackContext) throws JSONException {
      final int max = getMaxVolumeValue(type);

      if (max == -1) {
          notifyActionError(callbackContext, "Unknown volume type! " + type);
          return;
      }

      JSONObject maxVol = new JSONObject();
      maxVol.put(KEY_MAX_VOLUME, max);
      callbackContext.success(maxVol);
  }

  private int getVolume(int type) {
    Timber.d("getVolume() type = %s", type);

    int volume = -1;
    int streamType = -1;

    switch(type){
      case TYPE_RING:
        streamType = AudioManager.STREAM_RING;
        break;
      case TYPE_NOTIFICATION:
        streamType = AudioManager.STREAM_NOTIFICATION;
        break;
      case TYPE_SYSTEM:
        streamType = AudioManager.STREAM_SYSTEM;
        break;
      case TYPE_MUSIC:
        streamType = AudioManager.STREAM_MUSIC;
        break;
      case TYPE_VOICE_CALL:
        streamType = AudioManager.STREAM_VOICE_CALL;
        break;
      default:
        Timber.w("getVolume() unknown type! %s", type);
        break;
    }

    if (streamType >= 0) {
        try {
            volume = manager.getStreamVolume(streamType);
            Timber.d("getVolume() loaded volume = " + volume + " for type = " + type);
        } catch (Exception e) {
            Timber.e("getStreamVolume() ERROR: %s", e.getMessage());
        }
    }

    return volume;
  }

  private void setVolume(final int type, final int volume, final boolean scaled, final CallbackContext callbackContext){
    cordova.getActivity().runOnUiThread(new Runnable(){
      @Override
      public void run() {
        Timber.d("setVolume() type = " + type + ", volume = " + volume);
        final int HIDE_FLAG_UI = 0;
        int streamType;
        int maxVolume;

        switch(type){
          case TYPE_RING:
            streamType = AudioManager.STREAM_RING;
            maxVolume = maxVolumeRing;
            break;
          case TYPE_NOTIFICATION:
            streamType = AudioManager.STREAM_NOTIFICATION;
            maxVolume = maxVolumeNotification;
            break;
          case TYPE_SYSTEM:
            streamType = AudioManager.STREAM_SYSTEM;
            maxVolume = maxVolumeSystem;
            break;
          case TYPE_MUSIC:
            streamType = AudioManager.STREAM_MUSIC;
            maxVolume = maxVolumeMusic;
            break;
          case TYPE_VOICE_CALL:
            streamType = AudioManager.STREAM_VOICE_CALL;
            maxVolume = maxVolumeVoiceCall;
            break;
          default:
            String errorMessage = "Unknown type " + type;
            Timber.e(errorMessage);
            callbackContext.error(errorMessage);
            return;
        }

        int sanitizedVolume = sanitizeVolume(volume, maxVolume, scaled);
        Timber.i("setStreamVolume()"
            + " streamType = " + streamType
            + ", volume = " + volume
            + ", maxVolume = " + maxVolume
            + ", sanitizedVolume = " + sanitizedVolume);

        try {
            manager.setStreamVolume(streamType, sanitizedVolume, HIDE_FLAG_UI);
            callbackContext.success();
        } catch (Exception e) {
            notifyActionError(callbackContext, "setStreamVolume error: " + e.getMessage());
        }
      }
    });
  }

  private void getAudioMode(CallbackContext callbackContext) throws JSONException {
    Timber.d("getAudioMode()");

    final JSONObject mode = new JSONObject();
    int currentRingerMode = -1;
    int audioMode = NORMAL_MODE;
    String label = "Normal";

    try {
        currentRingerMode = this.manager.getRingerMode();
    } catch (Exception e) {
        Timber.e("manager.getRingerMode() ERROR: %s", e.getMessage());
    }

    switch(currentRingerMode){
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
      default:
        Timber.w("getAudioMode() unknown mode! %s", currentRingerMode);
        break;
    }

    mode.put(KEY_AUDIO_MODE, audioMode);
    mode.put(KEY_LABEL, label);

    callbackContext.success(mode);
  }

  private boolean setAudioMode(int mode){
    Timber.d("setAudioMode() mode = %s", mode);
    int targetMode;

    switch(mode){
      case SILENT_MODE:
        targetMode = AudioManager.RINGER_MODE_SILENT;
        break;
      case VIBRATE_MODE:
        targetMode = AudioManager.RINGER_MODE_VIBRATE;
        break;
      case NORMAL_MODE:
        targetMode = AudioManager.RINGER_MODE_NORMAL;
        break;
      default:
        Timber.w("setAudioMode() unknown mode!");
        return false;
    }

    try {
        int currentMode = manager.getRingerMode();
        if (currentMode != targetMode) {
            Timber.i("updating audio mode from " + currentMode + " to " + targetMode);
            manager.setRingerMode(targetMode);
        }
        return true;
    } catch (Exception e) {
        Timber.e("setAudioMode() ERROR: %s", e.getMessage());
        return false;
    }
  }

  private int getMaxVolumeValue(int type){
    Timber.d("getMaxVolumeValue() type = %s", type);
    int max = -1;
    int streamType = -1;

    switch(type){
      case TYPE_RING:
        streamType = AudioManager.STREAM_RING;
        break;
      case TYPE_NOTIFICATION:
        streamType = AudioManager.STREAM_NOTIFICATION;
        break;
      case TYPE_SYSTEM:
        streamType = AudioManager.STREAM_SYSTEM;
        break;
      case TYPE_MUSIC:
        streamType = AudioManager.STREAM_MUSIC;
        break;
      case TYPE_VOICE_CALL:
        streamType = AudioManager.STREAM_VOICE_CALL;
        break;
      default:
        Timber.w("getMaxVolumeValue() unknown type! %s", type);
        break;
    }

    if (streamType >= 0) {
        try {
            max = manager.getStreamMaxVolume(streamType);
            Timber.d("getMaxVolumeValue() loaded max = " + max + " for type = " + type);
        } catch (Exception e) {
            Timber.e("getMaxVolumeValue() ERROR: %s", e.getMessage());
        }
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
    return Math.min(value, max);
  }

  /**
   * Scales `value` from range [`sourceMin`, `sourceMax`] to range [`destMin`, `destMax`]
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
