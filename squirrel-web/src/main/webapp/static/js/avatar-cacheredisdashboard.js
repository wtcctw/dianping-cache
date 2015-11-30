module.controller('ClusterDashBoardController', [ '$scope', '$http',
		'$timeout', function($scope, $http,$timeout) {

			$scope.redisdata=[];
			
			$scope.transport = function(address) {
				window.localStorage.address = address;
			};
			$scope.initDashBoard = function() {
				$http.get(window.contextPath + '/redis/dashboardinfo', {
					params : {}
				}).success(function(response) {
					$scope.redisdata = [];
					response.forEach(function(item) {
						$scope.redisdata.push(item);
					});
				}).error(function(response) {
				});

			};

		$scope.addSlave = function(cluster,address){
			$http.get(window.contextPath + '/redis/autoaddslave', {
				params : {
					"cluster":cluster,
					"address":address
				}
			}).success(function(response) {

			}).error(function(response) {
			});
		};

		$scope.deleteSlave = function(cluster,address){
			$http.get(window.contextPath + '/redis/delslave', {
				params : {
					"cluster":cluster,
					"address":address
				}
			}).success(function(response) {

			}).error(function(response) {
			});
		};

			$scope.initDashBoard();
		} ]);