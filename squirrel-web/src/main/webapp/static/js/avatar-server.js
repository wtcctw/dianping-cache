
module.controller('ServerController', [
		'$scope',
		'$http',
		'$timeout',
		function($scope, $http,$timeout) {

			$scope.servers="";
			
			$scope.getServers = function(){
				$http.get(window.contextPath + '/monitor/servers',{
					params : {}
					}).success(function(response){
						$scope.servers = response;
					});
			};
			
			$scope.getServers();
		} ]);