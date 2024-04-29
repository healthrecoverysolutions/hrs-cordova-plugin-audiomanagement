"use strict";
////////////////////////////////////////////////////////////////
// Generic Cordova Utilities
////////////////////////////////////////////////////////////////
Object.defineProperty(exports, "__esModule", { value: true });
exports.AudioManagement = exports.AudioManagementCordovaInterface = void 0;
function noop() {
    return;
}
function cordovaExec(plugin, method, successCallback, errorCallback, args) {
    if (successCallback === void 0) { successCallback = noop; }
    if (errorCallback === void 0) { errorCallback = noop; }
    if (args === void 0) { args = []; }
    if (window.cordova) {
        window.cordova.exec(successCallback, errorCallback, plugin, method, args);
    }
    else {
        console.warn("".concat(plugin, ".").concat(method, "(...) :: cordova not available"));
        errorCallback && errorCallback("cordova_not_available");
    }
}
function cordovaExecPromise(plugin, method, args) {
    return new Promise(function (resolve, reject) {
        cordovaExec(plugin, method, resolve, reject, args);
    });
}
////////////////////////////////////////////////////////////////
// Plugin Interface
////////////////////////////////////////////////////////////////
var PLUGIN_NAME = 'AudioManagement';
function invoke(method) {
    var args = [];
    for (var _i = 1; _i < arguments.length; _i++) {
        args[_i - 1] = arguments[_i];
    }
    return cordovaExecPromise(PLUGIN_NAME, method, args);
}
function unwrapBatchStreamSetResult(result) {
    var _a;
    if (((_a = result === null || result === void 0 ? void 0 : result.errors) === null || _a === void 0 ? void 0 : _a.length) > 0) {
        return Promise.reject(result);
    }
    return Promise.resolve();
}
var AudioManagementCordovaInterface = /** @class */ (function () {
    function AudioManagementCordovaInterface() {
    }
    AudioManagementCordovaInterface.prototype.getAudioMode = function () {
        return invoke("getAudioMode");
    };
    AudioManagementCordovaInterface.prototype.setAudioMode = function (mode) {
        return invoke("setAudioMode", mode);
    };
    AudioManagementCordovaInterface.prototype.getVolume = function (type) {
        return invoke("getVolume", type);
    };
    AudioManagementCordovaInterface.prototype.setVolume = function (type, volume, scaled) {
        if (scaled === void 0) { scaled = true; }
        return invoke("setVolume", type, volume, scaled);
    };
    AudioManagementCordovaInterface.prototype.getMaxVolume = function (type) {
        return invoke("getMaxVolume", type);
    };
    AudioManagementCordovaInterface.prototype.getNotificationPolicyAccessState = function () {
        return invoke("getNotificationPolicyAccessState");
    };
    AudioManagementCordovaInterface.prototype.hasNotificationPolicyAccess = function () {
        return this.getNotificationPolicyAccessState()
            .then(function (state) { return !!(state === null || state === void 0 ? void 0 : state.isNotificationPolicyAccessGranted); });
    };
    AudioManagementCordovaInterface.prototype.openNotificationPolicyAccessSettings = function () {
        return invoke("openNotificationPolicyAccessSettings");
    };
    AudioManagementCordovaInterface.prototype.setVolumeBatchForResult = function (config) {
        return invoke('setVolumeBatch', config);
    };
    AudioManagementCordovaInterface.prototype.setVolumeBatch = function (config) {
        return this.setVolumeBatchForResult(config).then(unwrapBatchStreamSetResult);
    };
    return AudioManagementCordovaInterface;
}());
exports.AudioManagementCordovaInterface = AudioManagementCordovaInterface;
exports.AudioManagement = new AudioManagementCordovaInterface();
