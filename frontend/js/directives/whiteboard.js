/**
 * <canvas whiteboard-event-callback="updateShape(shape)" whiteboard-track-interval="100" whiteboard-shapes="myShapes" whiteboard-shape-type="'line'|'rect'|'text'"/>
 */
angular.module('whiteboard').directive('whiteboardShapes', ['$interval', function($interval) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      callback: '&whiteboardEventCallback',
      interval: '=whiteboardTrackInterval',
      shapes: '=whiteboardShapes',
      shapeType: '=whiteboardShapeType'
    },
    link: function ($scope, $element, $attrs) {
      var shape = null;
      var ctx = $element[0].getContext('2d');

      var updateShape = function() {
        if (shape !== null) {
          $scope.callback({shape: shape});
        }
      };

      $element.on('mousedown', function(e) {
        switch($scope.shapeType) {
          case 'LINE':
            shape = {shape: 'LINE', points: [{x: e.offsetX, y: e.offsetY}]};
            ctx.beginPath();
            ctx.moveTo(e.offsetX, e.offsetY);
            break;
          case 'RECT':
            ctx.save();
            shape = {shape: 'RECT', p1: {x: e.offsetX, y: e.offsetY}};
            break;
          case 'TEXT':
            shape = {shape: 'TEXT', p1: {x: e.offsetX, y: e.offsetY}};
            break;
          default:
            console.warn('Unsupported shape ', $scope.shape);
        }
      });

      $element.on('mousemove', function(e) {
        if (shape === null) {
          // noop
          return;
        }
        // intermediate update:
        switch(shape.shape) {
          case 'LINE':
            shape.points.push({x: e.offsetX, y: e.offsetY});
            ctx.lineTo(e.offsetX, e.offsetY);
            ctx.stroke();
            break;
          case 'RECT':
            shape.p2 = {x: e.offsetX, y: e.offsetY};
            ctx.restore();
            ctx.fillRect(Math.min(shape.p1.x, shape.p2.x), Math.min(shape.p1.y, shape.p2.y), Math.abs(shape.p1.x - shape.p2.x), Math.abs(shape.p1.y - shape.p2.y));
            break;
          default:
            //no-op
        }
      });

      $element.on('mouseup', function(e) {
        if (shape === null) {
          // noop
          return;
        }
        // final update:
        switch(shape.shape) {
          case 'LINE':
            shape.points.push({x: e.offsetX, y: e.offsetY});
            ctx.lineTo(e.offsetX, e.offsetY);
            ctx.stroke();
            break;
          case 'RECT':
            shape.p2 = {x: e.offsetX, y: e.offsetY};
            break;
          default:
            //no-op
        }
        // callback and cleanup:
        updateShape(shape);
        shape = null;
      });

      $element.on('mouseout', function() {
        if (shape !== null) {
          // TODO: cancel
          console.info('shape canceled');
          shape = null;
        }
      });

      // interval based updates:
      var timer = $interval(updateShape, $scope.interval);
      $element.on('$destroy', function() {
        $interval.cancel(timer);
      });
    }
  };
}]);
