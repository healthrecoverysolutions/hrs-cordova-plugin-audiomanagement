package com.hrs.audiomanagement;

import static com.hrs.audiomanagement.Utils.TYPE_MUSIC;
import static com.hrs.audiomanagement.Utils.TYPE_NOTIFICATION;
import static com.hrs.audiomanagement.Utils.TYPE_RING;
import static com.hrs.audiomanagement.Utils.TYPE_SYSTEM;
import static com.hrs.audiomanagement.Utils.TYPE_VOICE_CALL;
import static com.hrs.audiomanagement.Utils.getVolume;
import static com.hrs.audiomanagement.Utils.setVolume;

import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

class VolumeContentObserver extends ContentObserver {
    private final AudioManager audioManager;
    @Nullable
    private final CallbackContext callbackContext;
    private final Handler flagResetHandler = new Handler(Looper.getMainLooper());
    private Runnable flagResetRunnable = null;

    // Flag to check when it's syncing and don't make Android loop over and over
    private boolean isSyncing = false;
    private boolean didFailedSync = false;
    // Flag to don't loop when changes are done programmatically, e.g when user press on + or - in
    // volume settings in the app.
    private boolean isApplyChangesStop = false;

    // Track previous volume state to detect changes
    private int lastRingVolume = -1;
    private int lastNotificationVolume = -1;
    private int lastSystemVolume = -1;
    private int lastMusicVolume = -1;
    private int lastVoiceVolume = -1;

    public VolumeContentObserver(Handler handler, AudioManager audioManager, @Nullable CallbackContext callbackContext) {
        super(handler);
        this.audioManager = audioManager;
        this.callbackContext = callbackContext;

        // Initialize with current volume
        changeLatestVolumeState();
    }

    /**
     * Sets the flag to prevent volume change detection.
     * The flag will be automatically cleared after a delay.
     *
     * @param isApplyChangesStop true to stop detecting changes, false to enable detection
     */
    public void stopApplyChanges(boolean isApplyChangesStop) {
        this.isApplyChangesStop = isApplyChangesStop;

        Timber.d("stopApplyChanges set to: %s", isApplyChangesStop);

        if (isApplyChangesStop) {
            // Cancel any pending flag reset
            if (flagResetRunnable != null) {
                flagResetHandler.removeCallbacks(flagResetRunnable);
            }

            // Schedule automatic flag reset
            // This ensures all volume change callbacks have completed
            flagResetRunnable = () -> {
                this.isApplyChangesStop = false;
                changeLatestVolumeState(); // Update state after programmatic changes
                Timber.d("stopApplyChanges automatically reset to false");
            };

            /* Multiple events happens during the 800 delay:
             * - Multiple volume streams being changed (100-200ms)
             * - Android system processing the changes (200-300ms)
             * - Multiple onChange callbacks firing (200-300ms)
             * - Safety buffer (100-200ms)
             * Consider to increase to 1000ms if something is weird.
             */
            flagResetHandler.postDelayed(flagResetRunnable, 800);
        } else {
            clearFlaResetFlagRunnable();
            changeLatestVolumeState(); // Update state when manually clearing
        }
    }

    private void clearFlaResetFlagRunnable() {
        if (flagResetRunnable != null) {
            flagResetHandler.removeCallbacks(flagResetRunnable);
            flagResetRunnable = null;
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        Integer changedVolume = detectVolumeChange();

        if (isApplyChangesStop || callbackContext == null || changedVolume == null || isSyncing)
            return;

        syncAllVolumes(changedVolume);

        // If an error happens, cancel emitting values to FE.
        if (didFailedSync) return;

        try {
            JSONObject volumeInfo = makePluginMessage();

            PluginResult result = new PluginResult(PluginResult.Status.OK, volumeInfo);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            PluginResult result = new PluginResult(
                    PluginResult.Status.ERROR,
                    "Error getting volume info: " + e.getMessage()
            );
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    private void changeLatestVolumeState() {
        lastRingVolume = getVolume(audioManager, TYPE_RING);
        lastNotificationVolume = getVolume(audioManager, TYPE_NOTIFICATION);
        lastSystemVolume = getVolume(audioManager, TYPE_SYSTEM);
        lastMusicVolume = getVolume(audioManager, TYPE_MUSIC);
        lastVoiceVolume = getVolume(audioManager, TYPE_VOICE_CALL);
    }

    private void syncAllVolumes(int targetVolume) {
        isSyncing = true;

        try {
            Timber.d("Syncing all volumes to: %s", targetVolume);

            setVolume(audioManager, Utils.TYPE_RING, targetVolume);
            setVolume(audioManager, Utils.TYPE_NOTIFICATION, targetVolume);
            setVolume(audioManager, Utils.TYPE_SYSTEM, targetVolume);
            setVolume(audioManager, Utils.TYPE_MUSIC, targetVolume);
            setVolume(audioManager, Utils.TYPE_VOICE_CALL, targetVolume);

            // Update with latest values
            changeLatestVolumeState();
            Timber.d("Volume sync completed");
        } catch (Exception e) {
            didFailedSync = true;
            Timber.e(e, "Error syncing volumes: %s", e.getMessage());
        } finally {
            // Use a handler to reset the flag after a short delay
            // This ensures Android has processed all volume changes
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isSyncing = false;
                didFailedSync = false;
                Timber.d("Sync flag reset");
            }, 150);
        }
    }

    @Nullable
    private Integer detectVolumeChange() {
        // Get current volumes
        int ring = Utils.getVolume(audioManager, Utils.TYPE_RING);
        int notification = Utils.getVolume(audioManager, Utils.TYPE_NOTIFICATION);
        int system = Utils.getVolume(audioManager, Utils.TYPE_SYSTEM);
        int music = Utils.getVolume(audioManager, Utils.TYPE_MUSIC);
        int voice = Utils.getVolume(audioManager, Utils.TYPE_VOICE_CALL);

        if (ring != lastRingVolume) {
            Timber.d("Ring volume changed: " + lastRingVolume + " -> " + ring);
            return ring;
        }
        if (notification != lastNotificationVolume) {
            Timber.d("Notification volume changed: " + lastNotificationVolume + " -> " + notification);
            return notification;
        }
        if (system != lastSystemVolume) {
            Timber.d("System volume changed: " + lastSystemVolume + " -> " + system);
            return system;
        }
        if (music != lastMusicVolume) {
            Timber.d("Music volume changed: " + lastMusicVolume + " -> " + music);
            return music;
        }
        if (voice != lastVoiceVolume) {
            Timber.d("Voice volume changed: " + lastVoiceVolume + " -> " + voice);
            return voice;
        }
        return null;
    }

    private JSONObject makePluginMessage() throws JSONException {
        JSONObject volumeInfo = new JSONObject();

        int ring = getVolume(audioManager, TYPE_RING);
        int notification = getVolume(audioManager, TYPE_NOTIFICATION);
        int system = getVolume(audioManager, TYPE_SYSTEM);
        int music = getVolume(audioManager, TYPE_MUSIC);
        int voice = getVolume(audioManager, TYPE_VOICE_CALL);

        volumeInfo.put("ring", ring);
        volumeInfo.put("notification", notification);
        volumeInfo.put("system", system);
        volumeInfo.put("music", music);
        volumeInfo.put("voice", voice);

        return volumeInfo;
    }

    public void cleanup() {
        clearFlaResetFlagRunnable();
    }
}
