angular.module('whiteboard').controller('WhiteboardListCtrl', ['$scope', '$location', 'whiteboardsService', function($scope, $location, whiteboardsService) {
	'use strict';

	$scope.registeredWhiteboards = [];

	whiteboardsService.findRegisteredWhiteboards().then(function(whiteboards) {
		$scope.registeredWhiteboards = whiteboards;
	});

	$scope.createWhiteboard = function() {
		whiteboardsService.createWhiteboards().then(function(whiteboard) {
			$scope.registeredWhiteboards.push(whiteboard);
			$scope.successMessage = 'Whiteboard #' + whiteboard.id + ' angelegt';
		}, function() {
			$scope.errorMessage = 'Fehler beim Anlegen des Whiteboards.';
		});
	};

	$scope.join = function(whiteboardId) {
		$location.url('/whiteboard/' + whiteboardId);
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
