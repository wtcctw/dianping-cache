

module.controller('CacheQueryController', [
		'$scope',
		'$http',
		'$rootScope',
		function($scope, $http, $rootScope) {
			$scope.finalkey = "";
			$scope.category = "";
			$scope.hasTemplate = false;
			$scope.indexTemplate = "";
			$scope.indexDesc = "";
			$scope.version = "";
			$scope.categorySet = [];
			$scope.params = [];
			$scope.value = "";//设置 keyvalue
			$scope.status = "";
			$scope.result = ""; //查询结果值
			$scope.tagsinput = "";
			$scope.address = "";
			$scope.initpage = function(){
				$http.get(window.contextPath + '/cache/key/findAllCategory',{
					params : {
					},
					cache: true
					}).success(function(response) {
						
						$scope.categorySet = response.categorySet;
						$('.typeahead').typeahead({
							items: 16, 
							source : $scope.categorySet,
							updater : function(c) {
								$scope.finalkey = c;
								$scope.$apply();
								return c;
							}
						})
						
					}).error(function(response) {
						$scope.categorySet = "null";
						
					});
					
			};
			
		
			
			
			$scope.getTemplateParas = function(){
				$http.get(window.contextPath + '/cache/key/findByCategory',{
					params : {
						"category" : $scope.category
					}
					}).success(function(response) {
						if(response != null && response.indexTemplate != ""){
							$scope.hasTemplate = true;
							$scope.indexTemplate = response.indexTemplate;
							$scope.indexDesc = response.indexDesc;
							$scope.version = response.version;
							var paranum = 0;
							for(var i = 0; i < $scope.indexTemplate.length; i++){
								if( $scope.indexTemplate[i] == "{"){
									paranum++;
								}
							}
							var desc = $scope.indexDesc.split(/,|\|/);
							var from = 0;
							var to = 0;
							$scope.params = [];
							for(var i = 0; i < paranum; i++){
								to = $scope.indexTemplate.indexOf("{",from);
								var pname = $scope.indexTemplate.substring(from,to);
								
								from = $scope.indexTemplate.indexOf("}",to)+1;
								$scope.params.push([pname,desc[i]]);
							}
							
							paranum = 0;
							
							
						}else{
							$scope.hasTemplate = false;
						}
						
					}).error(function(response) {
					});
				
			};
			
			
			
			$scope.query = function(){
				$http.get(window.contextPath + '/cache/query/getKeyValue',{
					params : {
						"finalKey": $scope.finalkey
					}
					}).success(function(response){
						$scope.result = response.result;
						$scope.address = response.address;
					});
				
			};
			
			
			$scope.setKeyValue = function(){
				var tmpParams = [];
				for(var i = 0; i < $scope.params.length; i++){
					tmpParams.push($scope.params[i][1]);
				}
				$http.get(window.contextPath + '/cache/query/setKeyValue',{
					params : {
						"category" : $scope.category,
						"params" : tmpParams,
						"value" : $scope.value
					}
					}).success(function(response){
						$scope.status = response;
					});
			};
			
			$scope.$watch("params",function(newValue,oldValue, $scope){
				var key = $scope.category+".";
				for(var i = 0; i < $scope.params.length; i++){
					key = key + $scope.params[i][0] + $scope.params[i][1];
				}
				key = key+"_"+$scope.version;
				$scope.finalkey = key;
			},true);
			$scope.initpage();
} ]);
