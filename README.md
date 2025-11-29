# cordova-plugin-multi-image-picker
Cordova Plugin for Android Multi Image Picker


## Installation

    cordova plugin add https://github.com/templatetuners/cordova-plugin-multi-image-picker

## Supported Platforms

- Android

## Quick Example

    window.MultiImagePicker.pick(function (results) {
        if (!results || !results.length) {
          console.log('No images selected');
          return;
        }
    }, function(err) {
        console.log(''Error opening Photo Library' + err);
    });

