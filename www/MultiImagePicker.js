var exec = require('cordova/exec');

var MultiImagePicker = {
  pick: function (success, error) {
    exec(success, error, 'MultiImagePicker', 'pick', []);
  }
};

module.exports = MultiImagePicker;
