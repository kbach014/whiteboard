angular.module('whiteboard').factory('uuidService', [function() {
	'use strict';

	return {

		generateUUID: function() {
	    // taken from http://guid.us/GUID/JavaScript
	    function S4() {
	      return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
	    }
	    return (S4() + S4() + "-" + S4() + "-4" + S4().substr(0,3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
	  }

	};
}]);
