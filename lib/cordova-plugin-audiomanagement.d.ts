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
    volumePercentage: number;
}
export interface MaxVolumeResult {
    maxVolume: number;
}
export interface StreamSetConfig {
    streamType: number;
    volume: number;
    scaled?: boolean;
}
export interface BatchStreamSetConfig {
    streams: StreamSetConfig[];
}
export interface StreamSetResult {
    streamType?: number;
    errorMessage: string;
}
export interface BatchStreamSetResult {
    errors: StreamSetResult[];
}
export interface VolumeListenerResult {
    ring: number;
    notification: number;
    system: number;
    music: number;
    voice: number;
}
export declare class AudioManagementCordovaInterface {
    constructor();
    getAudioMode(): Promise<AudioModeResult>;
    setAudioMode(mode: AudioMode): Promise<void>;
    getVolume(type: VolumeType): Promise<VolumeResult>;
    setVolume(type: VolumeType, volume: number, scaled?: boolean): Promise<void>;
    getNotificationPolicyAccessState(): Promise<NotificationPolicyAccessState>;
    hasNotificationPolicyAccess(): Promise<boolean>;
    openNotificationPolicyAccessSettings(): Promise<void>;
    setVolumeBatchForResult(config: BatchStreamSetConfig): Promise<BatchStreamSetResult>;
    setVolumeBatch(config: BatchStreamSetConfig): Promise<void>;
    startVolumeListener(successCallback: (result: VolumeListenerResult) => void, errorCallback?: (error: any) => void): void;
    stopVolumeListener(successCallback?: () => void, errorCallback?: (error: any) => void): void;
    requestVolumeChangeToListener(): Promise<void>;
}
export declare const AudioManagement: AudioManagementCordovaInterface;
