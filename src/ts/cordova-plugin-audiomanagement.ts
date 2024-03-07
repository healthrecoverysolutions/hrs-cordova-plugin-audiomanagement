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

	public setVolume(type: VolumeType, volume: number, scaled: boolean = true): Promise<void> {
		return invoke(`setVolume`, type, volume, scaled);
	}

	public getMaxVolume(type: VolumeType): Promise<MaxVolumeResult> {
		return invoke(`getMaxVolume`, type);
	}

	public getNotificationPolicyAccessState(): Promise<NotificationPolicyAccessState> {
		return invoke(`getNotificationPolicyAccessState`);
	}

	public hasNotificationPolicyAccess(): Promise<boolean> {
		return this.getNotificationPolicyAccessState()
			.then((state) => !!state?.isNotificationPolicyAccessGranted);
	}

	public openNotificationPolicyAccessSettings(): Promise<void> {
		return invoke(`openNotificationPolicyAccessSettings`);
	}
}

export const AudioManagement = new AudioManagementCordovaInterface();