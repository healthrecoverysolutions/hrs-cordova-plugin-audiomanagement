////////////////////////////////////////////////////////////////
// Generic Cordova Utilities
////////////////////////////////////////////////////////////////
function noop() {
    return;
}
function cordovaExec(plugin, method, successCallback = noop, errorCallback = noop, args = []) {
    if (window.cordova) {
        window.cordova.exec(successCallback, errorCallback, plugin, method, args);
    }
    else {
        console.warn(`${plugin}.${method}(...) :: cordova not available`);
        errorCallback && errorCallback(`cordova_not_available`);
    }
}
function cordovaExecPromise(plugin, method, args) {
    return new Promise((resolve, reject) => {
        cordovaExec(plugin, method, resolve, reject, args);
    });
}
////////////////////////////////////////////////////////////////
// Plugin Interface
////////////////////////////////////////////////////////////////
const PLUGIN_NAME = 'AudioManagement';
function invoke(method, ...args) {
    return cordovaExecPromise(PLUGIN_NAME, method, args);
}
function unwrapBatchStreamSetResult(result) {
    if (Array.isArray(result === null || result === void 0 ? void 0 : result.errors) && result.errors.length > 0) {
        return Promise.reject(result);
    }
    return Promise.resolve();
}
export class AudioManagementCordovaInterface {
    constructor() {
    }
    getAudioMode() {
        return invoke(`getAudioMode`);
    }
    setAudioMode(mode) {
        return invoke(`setAudioMode`, mode);
    }
    getVolume(type) {
        return invoke(`getVolume`, type);
    }
    setVolume(type, volume, scaled = true) {
        return invoke(`setVolume`, type, volume, scaled);
    }
    getMaxVolume(type) {
        return invoke(`getMaxVolume`, type);
    }
    getNotificationPolicyAccessState() {
        return invoke(`getNotificationPolicyAccessState`);
    }
    hasNotificationPolicyAccess() {
        return this.getNotificationPolicyAccessState()
            .then((state) => !!(state === null || state === void 0 ? void 0 : state.isNotificationPolicyAccessGranted));
    }
    openNotificationPolicyAccessSettings() {
        return invoke(`openNotificationPolicyAccessSettings`);
    }
    setVolumeBatchForResult(config) {
        return invoke('setVolumeBatch', config);
    }
    setVolumeBatch(config) {
        return this.setVolumeBatchForResult(config).then(unwrapBatchStreamSetResult);
    }
    startVolumeListener(successCallback, errorCallback) {
        cordovaExec(PLUGIN_NAME, 'startVolumeListener', successCallback, errorCallback, []);
    }
    stopVolumeListener(successCallback, errorCallback) {
        cordovaExec(PLUGIN_NAME, 'stopVolumeListener', successCallback, errorCallback, []);
    }
}
export const AudioManagement = new AudioManagementCordovaInterface();
