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
    audioMode: AudioMode;
    label: string;
}
export interface VolumeResult {
    volume: number;
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
}
export declare const AudioManagement: AudioManagementCordovaInterface;
