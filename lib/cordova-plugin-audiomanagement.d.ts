export declare const enum AudioMode {
    SILENT = 0,
    VIBRATE = 1,
    NORMAL = 2
}
export declare const enum VolumeType {
    RING = 0,
    MUSIC = 1,
    NOTIFICATION = 2,
    SYSTEM = 3,
    VOICE_CALL = 4
}
export interface AudioModeResult {
    /** Current mode for the target type. */
    audioMode: AudioMode;
    /** Human-readable display text for the mode. */
    label: string;
}
export interface NotificationPolicyAccessState {
    isNotificationPolicyAccessGranted: boolean;
}
export interface VolumeResult {
    /**
     * Raw volume value for the target type, which is in range [0, maxVolume] where
     * `maxVolume` is an arbitrary number provided by the OS.
     */
    volume: number;
    /** Scaled percentage that will be in range [0, 100]. */
    scaledVolume: number;
}
export interface MaxVolumeResult {
    maxVolume: number;
}
export declare class AudioManagementCordovaInterface {
    constructor();
    getAudioMode(): Promise<AudioModeResult>;
    setAudioMode(mode: AudioMode): Promise<void>;
    getVolume(type: VolumeType): Promise<VolumeResult>;
    setVolume(type: VolumeType, volume: number, scaled?: boolean): Promise<void>;
    getMaxVolume(type: VolumeType): Promise<MaxVolumeResult>;
    getNotificationPolicyAccessState(): Promise<NotificationPolicyAccessState>;
    hasNotificationPolicyAccess(): Promise<boolean>;
    openNotificationPolicyAccessSettings(): Promise<void>;
}
export declare const AudioManagement: AudioManagementCordovaInterface;
