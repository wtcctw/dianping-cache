module.controller('ClusterDashBoardController', [ '$scope', '$http',
		'$timeout', function($scope, $http,$timeout) {

			$scope.dashboard = [];

			$scope.transport = function(cacheKey) {
				window.localStorage.cacheKey = cacheKey;
			}

			$scope.initDashBoard = function() {
				$http.get(window.contextPath + '/monitor/dashboardinfo', {
					params : {},
				}).success(function(response) {
					$scope.dashboard = [];
					response.forEach(function(item) {
						$scope.dashboard.push(item);
					});
					$timeout(function() {
						$scope.initDashBoard();
					}, 30000);
				}).error(function(response) {

				});

			}
			$scope.initDashBoard();
		} ]);