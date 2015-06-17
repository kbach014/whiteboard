angular.module('whiteboard').controller('LoginCtrl', ['$scope', '$location', 'userService', function($scope, $location, userService) {
	'use strict';

	$scope.login = function(username, password) {
		userService.login(username, password).then(function() {
			$scope.successMessage = 'Login erfolgreich.';
			$location.url('/whiteboards');
		}, function() {
			$scope.errorMessage = 'Login fehlgeschlagen.';
		});
	};

	$scope.logout = function() {
		userService.logout();
		$location.url('/loggedOut');
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
