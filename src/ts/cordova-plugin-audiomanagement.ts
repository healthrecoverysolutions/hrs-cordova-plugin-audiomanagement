////////////////////////////////////////////////////////////////
// Generic Cordova Utilities
////////////////////////////////////////////////////////////////

type CordovaSuccessCallback<TValue> = (value: TValue) => void;
type CordovaErrorCallback = (error: any) => void;

function noop() {
    return;
}

function cordovaExec<T>(
    plugin: string,
	method: string,
	successCallback: CordovaSuccessCallback<T> = noop,
	errorCallback: CordovaErrorCallback = noop,
	args: any[] = [],
): void {
    if (window.cordova) {
        window.cordova.exec(successCallback, errorCallback, plugin, method, args);

    } else {
        console.warn(`${plugin}.${method}(...) :: cordova not available`);
        errorCallback && errorCallback(`cordova_not_available`);
    }
}

function cordovaExecPromise<T>(plugin: string, method: string, args?: any[]): Promise<T> {
    return new Promise<T>((resolve, reject) => {
        cordovaExec<T>(plugin, method, resolve, reject, args);
    });
}

////////////////////////////////////////////////////////////////
// Plugin Interface
////////////////////////////////////////////////////////////////

const PLUGIN_NAME = 'AudioManagement';

function invoke<T>(method: string, ...args: any[]): Promise<T> {
    return cordovaExecPromise<T>(PLUGIN_NAME, method, args);
}

export const enum AudioMode {
    SILENT = 0,
    VIBRATE = 1,
    NORMAL = 2
}

export const enum VolumeType {
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
}

export interface MaxVolumeResult {
	maxVolume: number;
}

export class AudioManagementCordovaInterface {

	constructor() {
	}

	public getAudioMode(): Promise<AudioModeResult> {
		return invoke(`getAudioMode`);
	}

	public setAudioMode(mode: AudioMode): Promise<void> {
		return invoke(`setAudioMode`, mode);
	}

	public getVolume(type: VolumeType): Promise<VolumeResult> {
		return invoke(`getVolume`, type);
	}

	public setVolume(type: VolumeType, volume: number): Promise<void> {
		return invoke(`setVolume`, type, volume);
	}

	public getMaxVolume(type: VolumeType): Promise<MaxVolumeResult> {
		return invoke(`getMaxVolume`, type);
	}
}

export const AudioManagement = new AudioManagementCordovaInterface();