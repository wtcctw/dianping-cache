
module.controller('ConfigNewController', [
		'$scope',
		'$http',
		'$timeout',
		function($scope, $http,$timeout) {
			
			$scope.mCacheKey = "";
			$scope.mClientClazz = "";
			$scope.mServers = "";
			$scope.servers = [];
			
			$scope.mTranscoderClazz = "";
			
			
			$scope.ip = "";
			$scope.port = "11211";
			
			$scope.message1 = ""; //自动模态框信息显示
			$scope.message2 = ""; //手动模态框信息显示
			$scope.message3 = ""; //新建配置模态框信息显示
			$scope.btndisable1 = false;
			$scope.btndisable2 = false;
			$scope.submiticon = false;
			//模态框状态图片
			$scope.process = true;
			$scope.success = false;
			$scope.fail = false;
			
			//自动模态框阶段状态
			$scope.isStep1 = false;
			$scope.isStep2 = false;
			$scope.isStep3 = false;
			$scope.isStep4 = false;
			
			///手动模态框阶段状态
			$scope.isStep21 = false;
			$scope.isStep22 = false;
			$scope.isStep23 = false;
			
			
			$scope.manualAddNode = function(){

				$scope.process = true;
				$scope.success = false;
				$scope.fail = false;
				$scope.btndisable2 = true;
				$scope.isStep21 = true;
				$scope.isStep22 = false;
				$scope.isStep23 = false;
				
				$('#modal-wizard2').modal('show');
				$scope.message2 = "验证服务器端口是否开启服务……";
				$http.get(window.contextPath + '/cache/config/validate_port',{
					params : {
			        	"ip":$scope.ip,
			        	"port":$scope.port
			        	}
				}).success(function(response){
					var flag = response.flag;
					if(flag == true){
						$scope.message2 = "端口验证成功!(提交后更新到数据库配置)";
						$scope.isStep22 = true;
						$scope.isStep23 = true;
						$scope.btndisable2 = false;
						$scope.process = false;
						$scope.success = true;

						$scope.servers.push($scope.ip + ":" + $scope.port);
						if($scope.mServers == ""){
							$scope.mServers = $scope.ip + ":" + $scope.port;
						}else{
							$scope.mServers = $scope.mServers+";~;"+$scope.ip + ":" + $scope.port;
						}
						$timeout(function() {
							$('#modal-wizard2').modal('hide');
						}, 3000);
					}else{
						$scope.message2 = "端口验证失败!";
						$scope.isStep22 = false;
						$scope.btndisable2 = false;
						$scope.process = false;
						$scope.fail = true;

						$timeout(function(){
							$('#modal-wizard2').modal('hide');
						},3000);
					}

				}).error(function(response){
					$scope.message2 = "端口验证失败!";
					$scope.isStep22 = false;
					$scope.btndisable2 = false;
					$scope.process = false;
					$scope.fail = true;

					$timeout(function(){
						$('#modal-wizard2').modal('hide');
					},3000);
				});
			}
			
			$scope.validate = function(address){
				var add = address.split(":");
				$http.get(window.contextPath + '/cache/config/validate_port',{
					params : {
			        	"ip":add[0],
			        	"port":11211
			        	}
				}).success(function(response){
					var flag = response.flag;
					return flag;
				});
			}
			
			$scope.reset = function(){
				$scope.mCacheKey = "";
				$scope.mClientClazz = "";
				$scope.mServers = "";
				$scope.servers = [];
				$scope.mTranscoderClazz = "";
			}

			$scope.creatNew = function(myForm) {
				$scope.process = true;
				$scope.success = false;
				$scope.fail = false;
				$('#modal-wizard3').modal('show');
				$http.post(window.contextPath + '/cache/config/create', {
					"key" : $scope.mCacheKey,
					"clientClazz" : $scope.mClientClazz,
					"servers" : $scope.mServers,
					"transcoderClazz" : $scope.mTranscoderClazz
				}).success(function(response) {
					var flag = response.flag;
					if(flag == true){
						$scope.message3="新建配置成功";
						$scope.process = false;
						$scope.success = true;
					}else{
						$scope.message3="新建配置失败";
						$scope.process = false;
						$scope.success = false;
						$scope.fail = true;
					}
					
				}).error(function(response){
					$scope.message3="新建配置失败";
					$scope.process = false;
					$scope.success = false;
					$scope.fail = true;
				});
			}
			
			$scope.deleteServer = function(info){
				$scope.servers.splice($.inArray(info,$scope.servers),1);
				$scope.mServers = "";
				for(var i = 0; i < $scope.servers.length; i++){
					if(i > 0){
						$scope.mServers += ";~;";
					}
					$scope.mServers += $scope.servers[i]; 
				}
			}
			
			
			
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝Redis＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
			$scope.redisServerIp;
			$scope.redisServerPort = "7000";
			$scope.redisServer = [];
			$scope.serverTable = ["127.0.0.1:7000","127.0.0.1:7001","127.0.0.1:7002",
			                    "127.0.0.1:7003","127.0.0.1:7004","127.0.0.1:7005"];
			
			$scope.addRedisServer = function(){
				$scope.serverTable.push($scope.redisServerIp + ":" + $scope.redisServerPort);
			}
			
			
			$scope.implItems = ["com.dianping.cache.memcached.MemcachedClientImpl",
			                    "com.dianping.cache.ehcache.EhcacheClientImpl"];
			
			$scope.coderItems = ["com.dianping.cache.memcached.HessianTranscoder",
			     				"com.dianping.cache.memcached.KvdbTranscoder"];
			
			$scope.openModalForCreat = function(){
				$http.get(window.contextPath + '/cache/config/baseInfo', {
					params : {
					}
				}).success(function(response) {
					$scope.implItems = response.impl;
					$scope.coderItems = response.coder;
				});
			}
			
			$scope.initarray = function(){
				$scope.redisServer.push([1,2]);
				$scope.redisServer.push([2,2]);
				$scope.redisServer.push([3,2]);
			}
			$scope.initarray();
			
		} ]);