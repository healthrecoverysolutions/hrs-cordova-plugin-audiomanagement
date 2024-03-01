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
    AudioManagementCordovaInterface.prototype.setVolume = function (type, volume) {
        return invoke("setVolume", type, volume);
    };
    AudioManagementCordovaInterface.prototype.getMaxVolume = function (type) {
        return invoke("getMaxVolume", type);
    };
    return AudioManagementCordovaInterface;
}());
exports.AudioManagementCordovaInterface = AudioManagementCordovaInterface;
exports.AudioManagement = new AudioManagementCordovaInterface();
