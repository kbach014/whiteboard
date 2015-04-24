describe('whiteboard', function() {
	'use strict';

	var $scope, ctrl;

	beforeEach(function() {
		module('whiteboard');

		inject(function($controller, $rootScope, $q) {
			$scope = $rootScope.$new();
			ctrl = $controller('WhiteboardCtrl');
		});
	});

	it('should not crash', function() {
		expect(ctrl).not.toBe(null);
	});



});
