angular.module('whiteboard', ['ngRoute']);

angular.module('whiteboard').config(['$routeProvider', '$httpProvider', function($routeProvider) {
	'use strict';

	$routeProvider.when('/home', {
		controller: 'HomeCtrl',
		templateUrl: 'partials/home.html'
	});

	$routeProvider.when('/login', {
		controller: 'LoginCtrl',
		templateUrl: 'partials/login.html'
	});

	$routeProvider.when('/loggedOut', {
		controller: 'LoginCtrl',
		templateUrl: 'partials/logout.html'
	});

	$routeProvider.when('/registration', {
		controller: 'RegistrationCtrl',
		templateUrl: 'partials/registration.html'
	});

	$routeProvider.when('/whiteboards', {
		controller: 'WhiteboardListCtrl',
		templateUrl: 'partials/whiteboardList.html'
	});

	$routeProvider.when('/whiteboards/:id', {
		controller: 'WhiteboardCtrl',
		templateUrl: 'partials/whiteboard.html'
	});

	$routeProvider.otherwise({
		redirectTo: '/home'
	});

}]);

angular.module('whiteboard').run(['$location', '$rootScope', 'userService', function($location, $rootScope, userService) {

	// Routing

	$rootScope.currentPath = function() {
		return $location.path();
	};

	// Auth

	userService.registerLoginLogoutObserver(function() {
		$rootScope.currentUser = userService.getCurrentUser();
	});
	$rootScope.currentUser = userService.getCurrentUser();

}]);
