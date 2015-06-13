angular.module('whiteboard').controller('WhiteboardListCtrl', ['$scope', 'whiteboardsService', function($scope, whiteboardsService) {
	'use strict';

	$scope.whiteboards = [];

	$scope.createWhiteboard = function() {
		whiteboardsService.createWhiteboards().then(function(whiteboard) {
			$scope.whiteboards.push(whiteboard);
			$scope.successMessage = 'Whiteboard #' + whiteboard.id + ' angelegt';
		}, function() {
			$scope.errorMessage = 'Fehler beim Anlegen des Whiteboards.';
		});
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
