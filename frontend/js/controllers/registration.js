angular.module('whiteboard').controller('RegistrationCtrl', ['$scope', 'userService', function($scope, userService) {
	'use strict';

	$scope.register = function(firstname, lastname, username, password, repeatPassword) {
		if (!firstname || !lastname || !username || !password || !repeatPassword) {
			$scope.errorMessage = 'Bitte alle Felder ausfüllen.';
		} else if (password != repeatPassword) {
			$scope.errorMessage = 'Passwörter stimmen nicht überein.';
		} else {
			userService.register({
				firstname: firstname,
				lastname: lastname,
				username: username,
				password: password
			}).then(function() {
				$scope.successMessage = 'Registrierung erfolgreich.';
			}, function() {
				$scope.errorMessage = 'Fehler bei der Registrierung.';
			});
		}
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
