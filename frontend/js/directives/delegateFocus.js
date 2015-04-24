angular.module('whiteboard').directive('delegateFocus', [function () {
  'use strict';

  return {
      restrict: 'A',
      scope: {
        delegateFocus: '@',
      },
      link: function ($scope, $element, $attrs) {
        $element.on('focus', function() {
          var target = angular.element('#' + $attrs.delegateFocus);
          if (target) {
            target.focus();
          }
        });
      }
  };
}]);
