package com.hrs.audiomanagement;

import android.media.AudioManager;

import timber.log.Timber;

class Utils {
    public static final int TYPE_RING = 0;
    public static final int TYPE_MUSIC = 1;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_SYSTEM = 3;
    public static final int TYPE_VOICE_CALL = 4;
    public static final int TYPE_UNKNOWN = -1;

    public static int getVolume(AudioManager manager, int type) {
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

    public static int convertStreamTypeToNative(final int type) {
        return switch (type) {
            case TYPE_RING -> AudioManager.STREAM_RING;
            case TYPE_NOTIFICATION -> AudioManager.STREAM_NOTIFICATION;
            case TYPE_SYSTEM -> AudioManager.STREAM_SYSTEM;
            case TYPE_MUSIC -> AudioManager.STREAM_MUSIC;
            case TYPE_VOICE_CALL -> AudioManager.STREAM_VOICE_CALL;
            default -> TYPE_UNKNOWN;
        };
    }


    public static void setVolume(AudioManager audioManager, int streamType, int volume) {
        int maxVolume = audioManager.getStreamMaxVolume(streamType);
        int targetVolume = Math.max(0, Math.min(volume, maxVolume));

        audioManager.setStreamVolume(
                streamType,
                targetVolume,
                0  // No flags - silent change without UI
        );
    }
}
