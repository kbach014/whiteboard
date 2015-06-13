angular.module('whiteboard', ['ngRoute']);

angular.module('whiteboard').config(['$routeProvider', '$httpProvider', function($routeProvider) {
	'use strict';

	$routeProvider.when('/login', {
		controller: 'LoginCtrl',
		templateUrl: 'partials/login.html'
	});

	$routeProvider.when('/registration', {
		controller: 'RegistrationCtrl',
		templateUrl: 'partials/registration.html'
	});

	$routeProvider.when('/myWhiteboards', {
		controller: 'WhiteboardListCtrl',
		templateUrl: 'partials/whiteboardList.html'
	});

	$routeProvider.when('/whiteboard/:id', {
		controller: 'WhiteboardCtrl',
		templateUrl: 'partials/whiteboard.html'
	});

	$routeProvider.otherwise({
		redirectTo: '/whiteboard/2'
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
;

angular.module('whiteboard').controller('LoginCtrl', ['$scope', 'userService', function($scope, userService) {
	'use strict';

	$scope.login = function(username, password) {
		userService.login(username, password).then(function() {
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
;

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
;

angular.module('whiteboard').controller('WhiteboardCtrl', ['$scope', '$routeParams', 'whiteboardService', function($scope, $routeParams, whiteboardService) {
	'use strict';

	$scope.shapes = [];

	$scope.shapetype = 'PATH';

	var addOrUpdateShape = function(shape) {
		console.log('updating shape', shape);
		var existingShape = _.findWhere($scope.shapes, {'uuid': shape.uuid});
		if (existingShape) {
			// udpate
			_.assign(existingShape, shape);
		} else {
			// add
			$scope.shapes.push(shape);
		}
	};

	if ($routeParams.id) {
		whiteboardService.connect($routeParams.id).then(function() {
			$scope.successMessage = 'Connection established.';
			whiteboardService.setReceiverCallback(function(remoteDrawEvent) {
				console.log('received', remoteDrawEvent);
				addOrUpdateShape(remoteDrawEvent.shape);
			});
		}, function() {
			$scope.errorMessage = 'Unable to connect to shared whiteboard.';
		});
	} else {
		$scope.errorMessage = 'Ungültiges Whiteboard.';
	}

	$scope.onDraw = function(event) {
		whiteboardService.scheduleDrawEvent(event);
		addOrUpdateShape(event.shape);
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
;

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
;

/**
 * <canvas whiteboard-event-callback="updateShape(shape)" whiteboard-event-fps="10" whiteboard-render-fps="30" whiteboard-shapes="myShapes" whiteboard-shape-type="'PATH'|'RECT'|'TEXT'"/>
 */
angular.module('whiteboard').directive('whiteboardShapes', ['$interval', 'uuidService', function($interval, uuidService) {
  'use strict';

  return {
    restrict: 'A',
    scope: {
      callback: '&whiteboardEventCallback',
      eventFps: '=whiteboardEventFps',
      renderFps: '=whiteboardRenderFps',
      shapes: '=whiteboardShapes',
      shapeType: '=whiteboardShapeType'
    },
    link: function ($scope, $element, $attrs) {
      var transientShape = null;
      var canvas = $element[0];
      var ctx = canvas.getContext('2d');

      var drawShape = function(shape) {
        if (!shape || !shape.type) {
          return;
        }
        ctx.fillStyle = shape.color || 'black';
        ctx.strokeStyle = shape.color || 'black';
        switch(shape.type) {
          case 'PATH':
            if (shape.points && shape.points.length > 1) {
              var firstPoint = _.first(shape.points);
              var remainingPoints = _.rest(shape.points);
              ctx.beginPath();
              ctx.moveTo(firstPoint.x, firstPoint.y);
              _.forEach(remainingPoints, function(point) {
                ctx.lineTo(point.x, point.y);
              });
              ctx.stroke();
            }
            return;
          case 'RECT':
            if (shape.p1 && shape.p2 && !_.isEqual(shape.p1, shape.p2)) {
              ctx.fillRect(Math.min(shape.p1.x, shape.p2.x), Math.min(shape.p1.y, shape.p2.y), Math.abs(shape.p1.x - shape.p2.x), Math.abs(shape.p1.y - shape.p2.y));
            }
            return;
          case 'TEXT':
            return;
          default:
            console.warn('Unsupported shape ', shape.shape);
        }
      };

      var draw = function() {
        ctx.clearRect (0, 0, canvas.width, canvas.height );
        var uniqueShapes = _.uniq($scope.shapes, false, 'uuid');
        _.forEach(uniqueShapes, function(shape) {
          drawShape(shape);
        });
        drawShape(transientShape);
      };

      var sendTransientShapeEvents = function() {
        if (transientShape !== null) {
          $scope.callback({event: {type: 'UPDATE', shape: transientShape}});
        }
      };

      $element.on('mousedown', function(e) {
        var x = e.offsetX || e.pageX - $element.offset().left;
        var y = e.offsetY || e.pageY - $element.offset().top;
        console.log('mousedown', x, y);
        switch($scope.shapeType) {
          case 'PATH':
            transientShape = {uuid: uuidService.generateUUID(), type: 'PATH', finished: false, points: [{x: x, y: y}]};
            break;
          case 'RECT':
            transientShape = {uuid: uuidService.generateUUID(), type: 'RECT', finished: false, p1: {x: x, y: y}, p2: {x: x, y: y}};
            break;
          case 'TEXT':
            transientShape = {uuid: uuidService.generateUUID(), type: 'TEXT', finished: false, p1: {x: x, y: y}};
            break;
          default:
            console.warn('Unsupported shape ', $scope.shapeType);
        }
      });

      $element.on('mousemove', function(e) {
        if (transientShape === null) {
          // noop
          return;
        }
        var x = e.offsetX || e.pageX - $element.offset().left;
        var y = e.offsetY || e.pageY - $element.offset().top;
        // intermediate update:
        switch(transientShape.type) {
          case 'PATH':
            transientShape.points.push({x: x, y: y});
            break;
          case 'RECT':
            transientShape.p2 = {x: x, y: y};
            break;
          default:
            //no-op
        }
      });

      $element.on('mouseup', function(e) {
        if (transientShape === null) {
          // noop
          return;
        }
        var x = e.offsetX || e.pageX - $element.offset().left;
        var y = e.offsetY || e.pageY - $element.offset().top;
        // final update:
        transientShape.finished = true;
        switch(transientShape.type) {
          case 'PATH':
            transientShape.points.push({x: x, y: y});
            break;
          case 'RECT':
            transientShape.p2 = {x: x, y: y};
            break;
          default:
            //no-op
        }
        // callback and cleanup:
        $scope.callback({event: {type: 'FINISH', shape: transientShape}});
        transientShape = null;
      });

      $element.on('mouseout', function() {
        if (transientShape === null) {
          // noop
          return;
        }
        // cancel:
        switch(transientShape.type) {
          case 'PATH':
            // path without points:
            transientShape.points = [];
            break;
          case 'RECT':
            // rect without dimensions:
            transientShape.p2 = transientShape.p1;
            break;
          case 'TEXT':
            // text without characters:
            transientShape.text = '';
            break;
          default:
            //no-op
        }
        $scope.callback({event: {type: 'CANCEL', shape: transientShape}});
        transientShape = null;
      });

      // interval based updates:
      var renderTimer = $interval(draw, 1000/($scope.renderFps || 1));
      var eventTimer = $interval(sendTransientShapeEvents, 1000/($scope.eventFps || 1));
      $element.on('$destroy', function() {
        $interval.cancel(renderTimer);
        $interval.cancel(eventTimer);
      });
    }
  };
}]);
;

angular.module('whiteboard').factory('userService', ['$http', '$q', function($http, $q) {
	'use strict';

	var loginLogoutObservers = [];

	var notifyLoginLogoutObservers = function() {
		_.forEach(loginLogoutObservers, function(callback) {
			callback();
		});
	};

	var currentUser = null;

	return {

		registerLoginLogoutObserver: function(callback) {
			loginLogoutObservers.push(callback);
		},

		unregisterLoginLogoutObserver: function(callback) {
			_.remove(loginLogoutObservers, callback);
		},

		getCurrentUser: function() {
			return currentUser;
		},

		login: function(username, password) {
			var deferred = $q.defer();
			$http.post('/backend/rest/users/login', {
				username: username,
				password: password
			}).success(function(user) {
				currentUser = user;
				notifyLoginLogoutObservers();
				deferred.resolve();
			}).error(function() {
				currentUser = null;
				notifyLoginLogoutObservers();
				deferred.reject();
			});
			return deferred.promise;
		},

		logout: function() {
			currentUser = null;
			notifyLoginLogoutObservers();

			$http.post('/backend/rest/users/logout');
		},

		register: function(user) {
			var deferred = $q.defer();
			$http.post('/backend/rest/users/register', user).success(function() {
				deferred.resolve();
			}).error(function() {
				deferred.reject();
			});
			return deferred.promise;
		}

	};
}]);
;

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
;

angular.module('whiteboard').factory('whiteboardService', ['$http', '$q', '$interval', function($http, $q, $interval) {
	'use strict';

	var socket;
	var eventQueue = [];
	var queueDrainer = null;
	var drainQueue = function() {
		if (socket && socket.readyState === WebSocket.OPEN && !_.isEmpty(eventQueue)) {
			var toSend = eventQueue;
			eventQueue = [];
			socket.send(JSON.stringify(toSend));
		}
	};

	return {

		connect: function(whiteboardId) {
			this.close();
			var deferred = $q.defer();
			socket = new WebSocket('ws://localhost:8080/backend/drawings/' + whiteboardId);

			socket.onerror = function() {
				deferred.reject();
			};

			socket.onopen = function() {
				socket.onerror = _.noop();
				queueDrainer = $interval(drainQueue, 100, false);
				deferred.resolve();
	    };

			return deferred.promise;
		},

		setErrorCallback: function(callback) {
			if (socket && _.isFunction(callback)) {
				socket.onerror = callback;
			} else {
				socket.onerror = _.noop();
			}
		},

		setReceiverCallback: function(callback) {
			if (socket && _.isFunction(callback)) {
				socket.onmessage = function(message) {
					callback(message.data);
				};
			} else {
				socket.onerror = _.noop();
			}
		},

		scheduleDrawEvent: function(drawEvent) {
			eventQueue.push(drawEvent);
		},

		close: function() {
			if (queueDrainer) {
				$interval.cancel(queueDrainer);
				queueDrainer = null;
			}
			if (socket && socket.readyState === WebSocket.OPEN) {
				socket.close();
			}
		}

	};
}]);
;

angular.module('whiteboard').factory('whiteboardsService', ['$http', '$q', function($http, $q) {
	'use strict';

	return {

		findRegisteredWhiteboards: function() {
			var deferred = $q.defer();
			$http.get('/backend/rest/whiteboards/registered').success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		},

		createWhiteboards: function() {
			var deferred = $q.defer();
			$http.post('/backend/rest/whiteboards', {}).success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		}

	};
}]);
