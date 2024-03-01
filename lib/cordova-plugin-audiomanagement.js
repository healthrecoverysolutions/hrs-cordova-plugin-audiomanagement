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
    setVolume(type, volume, scaled = false) {
        return invoke(`setVolume`, type, volume, scaled);
    }
    getMaxVolume(type) {
        return invoke(`getMaxVolume`, type);
    }
}
export const AudioManagement = new AudioManagementCordovaInterface();
