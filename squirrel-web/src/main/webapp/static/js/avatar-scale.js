
module.controller('ScaleController', [
		'$scope',
		'$http',
		'$timeout',
		function($scope, $http,$timeout) {
			$scope.docker = [];
			$scope.nodes = [];
			$scope.instances = [];
			$scope.items = [];
			$scope.number = "";
			$scope.applyNodes = function(){
				$http.get(window.contextPath + '/redis/applynodes',{
					params : {
						"cluster":$scope.cluster,
						"number":$scope.number
					}
					}).success(function(){
						$scope.isOperationDone(operationId);
					});
			}
			
			$scope.isOperationDone = function(operationId){
				$http.get(window.contextPath + '/redis/getresult',{
					params : {
						"operateId":operationId
					}
					}).success(function(response){
						if(response.status == 100){
							$timeout(function() {
								$scope.isOperationDone(operationId);
							}, 1000);
						}else{
							response.instances.forEach(function(data){
								$scope.items.push(data);
							});
						}
					});
			}
			
			
			$scope.destroy = function(address){
				$http.get(window.contextPath + '/redis/destroy',{
					params : {
						"address":address+":6379"
					}
					});
			}
			
			
			
			$scope.getDockerInfo = function(){
				$http.get(window.contextPath + '/dockerinfo',{
					params : {}
					}).success(function(response){
						$scope.docker = response;
					});
			}
			
			$scope.getDockerInfo();

		} ]);