angular.module('whiteboard').controller('LoginCtrl', ['$scope', 'userService', function($scope, userService) {
	'use strict';

	$scope.login = function(username, password) {
		userService.login().then(function() {
			$scope.successMessage = 'Login erfolgreich.';
		}, function() {
			$scope.errorMessage = 'Login fehlgeschlagen.';
		});
	};

	$scope.logout = function() {
		userService.logout();
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
