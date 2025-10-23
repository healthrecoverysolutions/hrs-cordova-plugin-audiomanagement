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

    public static int getVolumePercentage(AudioManager manager, int type) {
        Timber.d("getVolume() type = %s", type);

        int volumePercentage = -1;
        int streamType = convertStreamTypeToNative(type);

        if (streamType == TYPE_UNKNOWN) {
            return volumePercentage;
        }

        try {
            volumePercentage = Math.round((float) manager.getStreamVolume(streamType) / manager.getStreamMaxVolume(streamType) * 100);
            Timber.d("getVolume() loaded volume = " + volumePercentage + " for type = " + type);
        } catch (Exception e) {
            Timber.e("getStreamVolume() ERROR: %s", e.getMessage());
        }

        return volumePercentage;
    }

    public static void setVolumePercentage(AudioManager audioManager, int type, int volumePercentage) {
        int streamType = convertStreamTypeToNative(type);
        int volumeValue = Math.round((float) (audioManager.getStreamMaxVolume(streamType) * volumePercentage) / 100);

        audioManager.setStreamVolume(
            streamType,
            volumeValue,
            0  // No flags - silent change without UI
        );
    }

    public static int convertStreamTypeToNative(final int type) {
        return switch (type) {
            case TYPE_VOICE_CALL -> AudioManager.STREAM_VOICE_CALL;
            case TYPE_SYSTEM -> AudioManager.STREAM_SYSTEM;
            case TYPE_RING -> AudioManager.STREAM_RING;
            case TYPE_MUSIC -> AudioManager.STREAM_MUSIC;
            case TYPE_NOTIFICATION -> AudioManager.STREAM_NOTIFICATION;
            default -> TYPE_UNKNOWN;
        };
    }
}
