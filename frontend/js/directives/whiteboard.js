/**
 * <canvas whiteboard-event-callback="updateShape(shape)" whiteboard-event-fps="10" whiteboard-render-fps="30" whiteboard-shapes="myShapes" whiteboard-shape-type="'LINE'|'RECT'|'TEXT'"/>
 */
angular.module('whiteboard').directive('whiteboardShapes', ['$interval', function($interval) {
  'use strict';

  var generateUUID = function() {
    // taken from http://guid.us/GUID/JavaScript
    function S4() {
      return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    return (S4() + S4() + "-" + S4() + "-4" + S4().substr(0,3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
  };

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
        if (!shape || !shape.shape) {
          return;
        }
        switch(shape.shape) {
          case 'LINE':
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
          $scope.callback({shape: transientShape});
        }
      };

      $element.on('mousedown', function(e) {
        switch($scope.shapeType) {
          case 'LINE':
            transientShape = {shape: 'LINE', uuid: generateUUID(), points: [{x: e.offsetX, y: e.offsetY}]};
            break;
          case 'RECT':
            transientShape = {shape: 'RECT', uuid: generateUUID(), p1: {x: e.offsetX, y: e.offsetY}, p2: {x: e.offsetX, y: e.offsetY}};
            break;
          case 'TEXT':
            transientShape = {shape: 'TEXT', uuid: generateUUID(), p1: {x: e.offsetX, y: e.offsetY}};
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
        // intermediate update:
        switch(transientShape.shape) {
          case 'LINE':
            transientShape.points.push({x: e.offsetX, y: e.offsetY});
            break;
          case 'RECT':
            transientShape.p2 = {x: e.offsetX, y: e.offsetY};
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
        // final update:
        switch(transientShape.shape) {
          case 'LINE':
            transientShape.points.push({x: e.offsetX, y: e.offsetY});
            break;
          case 'RECT':
            transientShape.p2 = {x: e.offsetX, y: e.offsetY};
            break;
          default:
            //no-op
        }
        // callback and cleanup:
        sendTransientShapeEvents(transientShape);
        transientShape = null;
      });

      $element.on('mouseout', function() {
        if (transientShape === null) {
          // noop
          return;
        }
        // final update:
        switch(transientShape.shape) {
          case 'LINE':
            // line without points:
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
