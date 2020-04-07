var exec = require('cordova/exec');

module.exports = {

  coolMethod: function (arg0, success, error) {
    exec(success, error, 'epsonprint', 'coolMethod', [arg0]);
  },
  portDiscovery: function(type, success, error) {
    exec(success, error, "EpsonPrint", "startDiscovery", [type]);
  },
};
