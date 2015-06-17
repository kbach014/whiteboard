angular.module('whiteboard').controller('WhiteboardListCtrl', ['$scope', '$location', 'whiteboardsService', function($scope, $location, whiteboardsService) {
	'use strict';

	$scope.registeredWhiteboards = [];
	$scope.unregisteredWhiteboards = [];

	whiteboardsService.findRegisteredWhiteboards().then(function(whiteboards) {
		$scope.registeredWhiteboards = whiteboards;
	});

	whiteboardsService.findUnregisteredWhiteboards().then(function(whiteboards) {
		$scope.unregisteredWhiteboards = whiteboards;
	});

	$scope.createWhiteboard = function() {
		whiteboardsService.createWhiteboards().then(function(whiteboard) {
			$scope.registeredWhiteboards.push(whiteboard);
			$scope.successMessage = 'Whiteboard #' + whiteboard.id + ' angelegt';
		}, function() {
			$scope.errorMessage = 'Fehler beim Anlegen des Whiteboards.';
		});
	};

	$scope.toggleAccessibility = function(whiteboard) {
		var updatedWhiteboard = _.assign({}, whiteboard);
		if (updatedWhiteboard.accessType == 'PUBLIC') {
			updatedWhiteboard.accessType = 'PRIVATE';
		} else {
			updatedWhiteboard.accessType = 'PUBLIC';
		}
		whiteboardsService.update(updatedWhiteboard).then(function() {
			console.log('updated whiteboard', updatedWhiteboard);
			_.assign(whiteboard, updatedWhiteboard);
		}, function() {
			$scope.errorMessage = 'Konnte Öffentlichkeit nicht umschalten.';
		});
	};

	$scope.open = function(whiteboardId) {
		$location.url('/whiteboard/' + whiteboardId);
	};

	$scope.join = function(whiteboard) {
		// TODO: whiteboardsService.join
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
