# cordova-plugin-clsprint
Cordova Plugin for Citizen CL-S521 Printer

Citizen Product and SDK
https://www.citizen-systems.com/en/products/printer/label/cl-s521/

## Installation

    cordova plugin add https://github.com/templatetuners/cordova-plugin-clsprinter/

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

