package com.hrs.audiomanagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import java.util.ArrayList;

import timber.log.Timber;

public class AudioManagement extends CordovaPlugin {

    private static final String ACTION_SET_MODE = "setAudioMode";
    private static final String ACTION_GET_MODE = "getAudioMode";
    private static final String ACTION_SET_VOLUME = "setVolume";
    private static final String ACTION_GET_VOLUME = "getVolume";
    private static final String ACTION_GET_MAX_VOLUME = "getMaxVolume";
    private static final String ACTION_SET_VOLUME_BATCH = "setVolumeBatch";
    // These are required for SDK 23 and up
    private static final String ACTION_GET_NOTIFICATION_ACCESS_POLICY_STATE = "getNotificationPolicyAccessState";
    private static final String ACTION_OPEN_NOTIFICATION_ACCESS_POLICY_SETTINGS = "openNotificationPolicyAccessSettings";

    private static final int SILENT_MODE = 0;
    private static final int VIBRATE_MODE = 1;
    private static final int NORMAL_MODE = 2;

    private static final int TYPE_RING = 0;
    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_NOTIFICATION = 2;
    private static final int TYPE_SYSTEM = 3;
    private static final int TYPE_VOICE_CALL = 4;
    private static final int TYPE_UNKNOWN = -1;
    private static final int HIDE_FLAG_UI = 0;

    private static final int SCALED_MAX_VOLUME = 100;

    private static final String LABEL_NORMAL = "Normal";
    private static final String LABEL_SILENT = "Silent";
    private static final String LABEL_VIBRATE = "Vibrate";

    private static final String KEY_AUDIO_MODE = "audioMode";
    private static final String KEY_LABEL = "label";
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_SCALED_VOLUME = "scaledVolume";
    private static final String KEY_MAX_VOLUME = "maxVolume";
    private static final String KEY_NOTIFICATION_POLICY_ACCESS_GRANTED = "isNotificationPolicyAccessGranted";
    private static final String KEY_STREAMS = "streams";
    private static final String KEY_STREAM_TYPE = "streamType";
    private static final String KEY_ERRORS = "errors";
    private static final String KEY_SCALED = "scaled";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    private AudioManager manager;
    private NotificationManager notificationManager;

    public void pluginInitialize() {
        Activity activity = this.cordova.getActivity();
        this.manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        this.notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
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

        } else if (ACTION_SET_VOLUME_BATCH.equals(action)) {
            setVolumeBatch(args.getJSONObject(0), callbackContext);

        } else if (ACTION_GET_MAX_VOLUME.equals(action)) {
            final int type = args.getInt(0);
            getMaxVolumeAction(type, callbackContext);

        } else if (ACTION_GET_NOTIFICATION_ACCESS_POLICY_STATE.equals(action)) {
            getNotificationPolicyAccessState(callbackContext);

        } else if (ACTION_OPEN_NOTIFICATION_ACCESS_POLICY_SETTINGS.equals(action)) {
            openNotificationPolicyAccessSettings(callbackContext);

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

    private void getNotificationPolicyAccessState(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            granted = this.notificationManager.isNotificationPolicyAccessGranted();
        }
        result.put(KEY_NOTIFICATION_POLICY_ACCESS_GRANTED, granted);
        callbackContext.success(result);
    }

    private void openNotificationPolicyAccessSettings(CallbackContext callbackContext) {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        cordova.getActivity().startActivity(intent);
        callbackContext.success();
    }

    private void setModeAction(int mode, CallbackContext callbackContext) {
        if (setAudioMode(mode)) {
            callbackContext.success();
        } else {
            notifyActionError(callbackContext, "Unknown audio mode! " + mode);
        }
    }

    private void getVolumeAction(int type, CallbackContext callbackContext) throws JSONException {
        final int volume = getVolume(type);

        if (volume == -1) {
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
        int streamType = convertStreamTypeToNative(type);

        if (streamType != TYPE_UNKNOWN) {
            try {
                volume = manager.getStreamVolume(streamType);
                Timber.d("getVolume() loaded volume = " + volume + " for type = " + type);
            } catch (Exception e) {
                Timber.e("getStreamVolume() ERROR: %s", e.getMessage());
            }
        }

        return volume;
    }

    private void setVolumeBatch(JSONObject volumeConfig, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<JSONObject> errors = new ArrayList<>();

                try {
                    JSONArray streams = volumeConfig.getJSONArray(KEY_STREAMS);
                    int streamCount = streams.length();
                    Timber.d("setVolumeBatch() updating %s streams", streamCount);
                    for (int i = 0; i < streamCount; i++) {
                        try {
                            final JSONObject streamConfig = streams.getJSONObject(i);
                            if (streamConfig == null) {
                                Timber.w("setVolumeBatch() skipping invalid config at index %s", i);
                                continue;
                            }

                            final int type = streamConfig.getInt(KEY_STREAM_TYPE);
                            final int streamType = convertStreamTypeToNative(type);
                            if (streamType == TYPE_UNKNOWN) {
                                Timber.w("setVolumeBatch() invalid stream type at index %s", i);
                                errors.add(new JSONObject()
                                    .put(KEY_STREAM_TYPE, type)
                                    .put(KEY_ERROR_MESSAGE, "unknown stream type: " + type));
                                continue;
                            }

                            final int inputVolume = streamConfig.getInt(KEY_VOLUME);
                            if (inputVolume < 0 || inputVolume > 100) {
                                Timber.w("setVolumeBatch() invalid volume at index %s", i);
                                errors.add(new JSONObject()
                                    .put(KEY_STREAM_TYPE, type)
                                    .put(KEY_ERROR_MESSAGE, "invalid volume level: " + inputVolume));
                                continue;
                            }

                            final boolean scaled = streamConfig.optBoolean(KEY_SCALED, true);
                            final int maxVolume = manager.getStreamMaxVolume(streamType);
                            final int targetVolume = sanitizeVolume(inputVolume, maxVolume, scaled);
                            Timber.v("set stream %s = %s percent (actual = %s)", streamType, inputVolume, targetVolume);
                            manager.setStreamVolume(streamType, targetVolume, HIDE_FLAG_UI);

                        } catch (Exception e) {
                            Timber.e(e, "caught error attempting to set stream");
                            errors.add(new JSONObject()
                                .put(KEY_ERROR_MESSAGE, e.getMessage()));
                        }
                    }

                    callbackContext.success(new JSONObject().put(KEY_ERRORS, errors));
                } catch (Exception e) {
                    notifyActionError(callbackContext, "setVolumeBatch error: " + e.getMessage());
                }
            }
        });
    }

    private void setVolume(final int type, final int volume, final boolean scaled, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Timber.v("setVolume() type = " + type + ", volume = " + volume);
                int streamType = convertStreamTypeToNative(type);

                if (streamType == TYPE_UNKNOWN) {
                    String errorMessage = "Unknown type " + type;
                    Timber.e(errorMessage);
                    callbackContext.error(errorMessage);
                    return;
                }

                int maxVolume = manager.getStreamMaxVolume(type);
                int sanitizedVolume = sanitizeVolume(volume, maxVolume, scaled);
                Timber.d("setStreamVolume()"
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

        switch (currentRingerMode) {
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

    private boolean setAudioMode(int mode) {
        Timber.d("setAudioMode() mode = %s", mode);
        int targetMode;

        switch (mode) {
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

    private int getMaxVolumeValue(int type) {
        Timber.d("getMaxVolumeValue() type = %s", type);
        int max = -1;
        int streamType = convertStreamTypeToNative(type);

        if (streamType != TYPE_UNKNOWN) {
            try {
                max = manager.getStreamMaxVolume(streamType);
                Timber.d("getMaxVolumeValue() loaded max = " + max + " for type = " + type);
            } catch (Exception e) {
                Timber.e("getMaxVolumeValue() ERROR: %s", e.getMessage());
            }
        } else {
            Timber.w("getMaxVolumeValue() unknown type! %s", type);
        }

        return max;
    }

    private int convertStreamTypeToNative(final int type) {
        switch (type) {
            case TYPE_RING:
                return AudioManager.STREAM_RING;
            case TYPE_NOTIFICATION:
                return AudioManager.STREAM_NOTIFICATION;
            case TYPE_SYSTEM:
                return AudioManager.STREAM_SYSTEM;
            case TYPE_MUSIC:
                return AudioManager.STREAM_MUSIC;
            case TYPE_VOICE_CALL:
                return AudioManager.STREAM_VOICE_CALL;
            default:
                return TYPE_UNKNOWN;
        }
    }

    private int sanitizeVolume(int sourceVolume, int maxVolume, boolean scaled) {
        return scaled ? interpolateVolume(sourceVolume, maxVolume) : clamp(sourceVolume, 0, maxVolume);
    }

    private int interpolateVolume(int sourceVolume, int maxVolume) {
        return interpolate(sourceVolume, 0, SCALED_MAX_VOLUME, 0, maxVolume);
    }

    private int getVolumePercentage(int sourceVolume, int maxVolume) {
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
        double ratio = clamp(value, sourceMin, sourceMax) / (double) (sourceMax - sourceMin);
        int result = (int) Math.round(ratio * (destMax - destMin)) + destMin;
        return clamp(result, destMin, destMax);
    }
}
