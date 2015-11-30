
module.controller('ConfigEditController', [
		'$scope',
		'$http',
		'$timeout',
		function($scope, $http,$timeout) {
			
			$scope.mCacheKey = "";
			$scope.mClientClazz = "";
			$scope.mServers = "";
			$scope.servers = [];
			$scope.dropservers = [];
			$scope.redisnodes = [];
			$scope.tempServers = "";
			$scope.mTranscoderClazz = "";
			$scope.locator = "";
			$scope.module = "";
			$scope.proxy = "";
			
			$scope.capacity = "memcached8";
			$scope.num = 1;
			$scope.ip = "ip";
			$scope.port = "port";//默认11211端口
			
			$scope.message1 = ""; //自动模态框信息显示
			$scope.message2 = ""; //手动模态框信息显示
			$scope.message4 = "";
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
			
			
			$scope.showMemcached = true;
			$scope.showDcache = false;
			$scope.showRedis = false;
			$scope.showWeb = false;
			
			$scope.initpage = function(){
				var localstore = window.localStorage;
				$scope.mCacheKey = localstore.cacheKey;
				
				
				$http.get(window.contextPath + '/cache/config/find',{
					params : {
			        	"cacheKey":$scope.mCacheKey,
			        	}
				}).success(function(response){
					$scope.mClientClazz = response.config.clientClazz;
					$scope.mServers = response.config.servers;
					$scope.tempServers = response.config.servers;
					$scope.mTranscoderClazz = response.config.transcoderClazz;
					if($scope.mCacheKey.indexOf("dcache") != -1){
						$scope.showMemcached = false;
						$scope.showDcache = true;
						$scope.showRedis = false;
						$scope.showWeb = false;
						var tmp = $scope.mServers.split(";");
						for(var i = 0; i < tmp.length; i++){
							if(tmp[i] != "" && tmp[i].indexOf("=") != -1){
								var tmp1 = tmp[i].split("=");
								if(tmp1[0].indexOf("module") != -1){
									$scope.module = tmp1[1].substring(1,tmp1[1].length-1);
								}else if(tmp1[0].indexOf("locator") != -1){
									$scope.locator = tmp1[1].substring(1,tmp1[1].length-1);
								}else if(tmp1[0].indexOf("proxy") != -1){
									$scope.proxy = tmp1[1].substring(1,tmp1[1].length-1);
									
								}
							}
						}
						
					}else if($scope.mCacheKey.indexOf("redis") != -1){
						$scope.showMemcached = false;
						$scope.showDcache = false;
						$scope.showRedis = true;
						$scope.showWeb = false;
						$http.get(window.contextPath + '/redis/clusterinfo',{
							params : {
					        	"cacheKey":$scope.mCacheKey,
					        	}
						}).success(function(response){
							if(response != null){
								$scope.redisnodes = response;
							}
						});
						
						
					}else if($scope.mCacheKey.indexOf("web") != -1 ){
						$scope.showMemcached = false;
						$scope.showDcache = false;
						$scope.showRedis = false;
						$scope.showWeb = true;
					}else{
						$scope.servers = $scope.mServers.split(/;~;|\|/);
					}
				}).error(function(){
					
				});
			};
			
			$scope.manualAddNode = function(){
				
				$('#modal-wizard2').modal('show');
				$scope.btndisable2 = true;
				$scope.isStep21 = true;
				$scope.isStep22 = false;
				$scope.isStep23 = false;
				
				$scope.message2 = "验证服务器是否开启服务……";
				$http.get(window.contextPath + '/cache/config/validate_port',{
					params : {
			        	"ip":$scope.ip,
			        	"port":$scope.port
			        	}
				}).success(function(response){
					var flag = response.flag;
					if(flag == true){
						$scope.message2 = "服务验证成功!";
						$scope.isStep22 = true;
						if($scope.mServers != ""){
							$scope.tempServers = $scope.mServers+";~;"+$scope.ip + ":" + $scope.port;
						}else{
							$scope.tempServers = $scope.ip + ":" + $scope.port;
						}
						
						//$scope.updateConfigServers(); // 添加服务器，更新服务器列表
						$scope.addServer($scope.ip+":"+$scope.port);
					}else{
						$scope.message2 = "服务验证失败!";
						$scope.isStep22 = false;
						$scope.btndisable2 = false;
						
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
					}

				}).error(function(response){
					$scope.message2 = "服务验证失败!";
					$scope.isStep22 = false;
					$scope.btndisable2 = false;
					
					// 模态框图片切换
        			$scope.process = false;
        			$scope.success = false;
        			$scope.fail = true;
					
				});
			};
			
			$scope.autoAddNode = function(){
				$('#modal-wizard1').modal('show');
				$scope.btndisable1 = true;
				$scope.isStep1 = true;
				$scope.isStep2 = false;
				$scope.isStep3 = false;
				$scope.isStep4 = false;
				
				// 模态框图片切换
    			$scope.process = true;
    			$scope.success = false;
    			$scope.fail = false;
    			
				$scope.message1 = "申请扩容";
				$scope.app_id = $scope.capacity; 
				$http.get(window.contextPath + '/cache/config/scale',{
					params : {
						"app_id" : $scope.app_id,
						"cacheKey": $scope.mCacheKey
					}
					}).success(function(response){
						$scope.isStep2 = true;
						var operationId = response.operationId;
						$scope.message1 = "申请被接受 流水号" + operationId;
						//轮询
						$scope.isOperationDone(operationId);
						
					}).error(function(response){
						$scope.message1 = "申请失败";
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
	        			
	        			$scope.btndisable1 = false;
					});
				
			};
			
			$scope.isOperationDone = function(operationId){
				$http.get(window.contextPath + '/cache/config/operateResult',{
					params : {
						"operationId" : operationId,
					}	
				}).success(function(response){
					$('#modal-wizard1').modal('show');
					$scope.isStep3 = true;
					$scope.message1 = "paas扩容处理";
					var operationStatus = response.status;
					if(operationStatus == "200"){//success
						$scope.isStep4 = true;
						$scope.message1 = "扩容成功" + response.ip + ":" +response.port;
						$scope.initpage();	
						$scope.btndisable1 = false;
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = true;
	        			$scope.fail = false;
					}else if(operationStatus == "100"){//doing
						
						$timeout(function() {
							$scope.isOperationDone(operationId);
						}, 1000);
						
					}else{//fail
						$scope.message1 = "paas扩容失败";
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
	        			
	        			$scope.btndisable1 = false;
					}
						
				}).error(function(response){
					$scope.message1 = "失败";
					// 模态框图片切换
        			$scope.process = false;
        			$scope.success = false;
        			$scope.fail = true;
        			
        			$scope.btndisable1 = false;
				});
			};
			
			$scope.validate = function(address){
				var add = address.split(":");
				$http.get(window.contextPath + '/cache/config/validate_port',{
					params : {
			        	"ip":add[0],
			        	"port":add[1]
			        	}
				}).success(function(response){
					var flag = response.flag;
					return flag;
				});
			};
			
			$scope.addServer = function(server){
				 $scope.message2 = "添加服务器……";
				 $scope.process = true;
				 $scope.success = false;
				 $scope.fail = false;
				 if($scope.mServers == null){
					 $scope.mServers = "";
				 }
				$http.post(window.contextPath + '/cache/config/addServer',
		        		{"key":$scope.mCacheKey,"oldservers":$scope.mServers,
		        		"server":server
		        		}).success(function(response){
		        			var flag = response.flag;
		        			if(flag == true){//成功
		        				
		        				$scope.message2 = "添加服务器成功,无需再次提交";
		        				$scope.isStep23 = true;
		        				$scope.initpage();
		        				
		        				// 模态框图片切换
		        				$scope.process = false;
		        				$scope.success = true;
		        				$scope.fail = false;
		        				
		        			}else{//失败
		        				$scope.message2 = "数据冲突，请刷新页面";
			        			$scope.isStep23 = false;
			        			
			        			// 模态框图片切换
			        			$scope.process = false;
			        			$scope.success = false;
			        			$scope.fail = true;
		        			}
		        		}).error(function(response){
		        			$scope.message2 = "添加服务器失败";
		        			$scope.isStep23 = false;
		        			
		        			// 模态框图片切换
		        			$scope.process = false;
		        			$scope.success = false;
		        			$scope.fail = true;
		        		});
		        		$scope.btndisable2 = false; //释放添加按钮
			};
			
			
			
			
			//更新服务器列表
			$scope.updateConfigServers = function(){
				 $scope.message2 = "添加服务器……";
				 $scope.process = true;
				 $scope.success = false;
				 $scope.fail = false;
				 $http.post(window.contextPath + '/cache/config/updateServers',
			        		{"key":$scope.mCacheKey,"oldservers":$scope.mServers,
			        		"newservers":$scope.tempServers
			        		}).success(function(response){
			        			var flag = response.flag;
			        			if(flag == true){//成功
			        				
			        				$scope.message2 = "添加服务器成功,无需再次提交";
			        				$scope.isStep23 = true;
			        				$scope.mServers = $scope.tempServers; 
			        				$scope.servers = $scope.mServers.split(/;~;|\|/);
			        				//window.localStorage.servers = $scope.mServers;
			        				
			        				// 模态框图片切换
			        				$scope.process = false;
			        				$scope.success = true;
			        				$scope.fail = false;
			        				
			        			}else{//失败
			        				$scope.message2 = "数据冲突，请刷新页面";
				        			$scope.isStep23 = false;
				        			
				        			// 模态框图片切换
				        			$scope.process = false;
				        			$scope.success = false;
				        			$scope.fail = true;
			        			}
			    				
			        			
			        		}).error(function(response){
			        			$scope.message2 = "添加服务器失败";
			        			$scope.isStep23 = false;
			        			
			        			// 模态框图片切换
			        			$scope.process = false;
			        			$scope.success = false;
			        			$scope.fail = true;
			        			
			        		});
					$scope.btndisable2 = false; //释放添加按钮
			};
			
			//
			$scope.updateConfig = function(){
				
				 $scope.submiticon = true;
				 $http.post(window.contextPath + '/cache/config/update',
			        		{"key":$scope.mCacheKey,"clientClazz":$scope.mClientClazz,
			        		"servers":$scope.mServers,"transcoderClazz":$scope.mTranscoderClazz
			        		}).success(function(response){
			    				
								$timeout(function() {
									 $scope.submiticon = false;
								}, 1000);
			        		});

			};
			
			$scope.updateDcacheConfig = function(){
				 $scope.submiticon = true;
				 $scope.tempServers = "module=\""+ $scope.module + "\";" +
				 					  "proxy=\"" + $scope.proxy  + "\";" +
				 					  "locator=\"" + $scope.locator + "\"";
				
				 $http.post(window.contextPath + '/cache/config/updateServers',
			        		{"key":$scope.mCacheKey,"oldservers":$scope.mServers,
			        		"newservers":$scope.tempServers
			        		}).success(function(response){
			        			var flag = response.flag;
			        			if(flag == true){//成功
			        				$scope.initpage();			        				
			        			}
								$timeout(function() {
									 $scope.submiticon = false;
								}, 1000);
			        		});
			};
			
			
			$scope.preDeleteServer = function(index){
				$scope.serverToDelete = $scope.servers[index];
				$scope.serverToDeleteIndex = index;
				$('#modal-wizard3').modal('show');
				
			};
			
			//delete server 2.0
			$scope.deleteServer = function(){
				$('#modal-wizard3').modal('hide');
				//删除数组中该ip
				$scope.servers.splice($scope.serverToDeleteIndex,1);
				//获取最新的服务集群ip
				$scope.tempServers = "";
				for(var i = 0; i < $scope.servers.length; i++){
					if(i > 0){
						$scope.tempServers += ";~;";
					}
					$scope.tempServers += $scope.servers[i]; 
				}
				
				$http.post(window.contextPath + '/cache/config/updateServers',
		        		{"key":$scope.mCacheKey,"oldservers":$scope.mServers,
		        		"newservers":$scope.tempServers
		        		}).success(function(response){
		        			var flag = response.flag;
		        			if(flag == true){//成功
		        				$scope.mServers = $scope.tempServers; 
		        				$scope.initpage();
		        			}else{//失败
		        				$scope.servers = $scope.mServers.split(/;~;|\|/);
		        			}
		    				
		        		}).error(function(response){
		        			$scope.servers = $scope.mServers.split(/;~;|\|/);
		        		});
				
			};
			
			
			// delete server 3.0
			$scope.reduceServer = function(){
				$('#modal-wizard3').modal('hide');
				//$('#modal-wizard4').modal('show');
				$http.post(window.contextPath + '/cache/config/deleteServer',
		        		{"key":$scope.mCacheKey,"server":$scope.serverToDelete,
		        		"oldservers":$scope.mServers
		        		}).success(function(response){
		        			var flag = response.flag;
		        			if(flag == true){//docker
		        				var operationId = response.operationId;
								//轮询
								$scope.isShutDownDone(operationId);
		        			}else{//old 
		        				$scope.initpage();
		        			}
		        		}).error(function(response){
		        			$scope.initpage();
		        		});
				
				
				
			};
			$scope.isShutDownDone = function(operationId){
				$http.get(window.contextPath + '/cache/config/operateResult',{
					params : {
						"operationId" : operationId,
					}	
				}).success(function(response){
					$('#modal-wizard4').modal('show');
					$scope.message4 = "ShutDown";
					var operationStatus = response.status;
					if(operationStatus == "200"){//success
						$scope.message4 = "关闭 成功";
						var operationId = response.operationId;
						$scope.isReduceDone(operationId);
						
					}else if(operationStatus == "100"){//doing
						
						$timeout(function() {
							$scope.isReduceDone(operationId);
						}, 1000);
						
					}else{//fail
						$scope.message4 = "关闭失败";
						$scope.initpage();
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
	        			
					}
						
				}).error(function(response){
					$scope.message4 = "缩容失败";
					$scope.initpage();
					// 模态框图片切换
        			$scope.process = false;
        			$scope.success = false;
        			$scope.fail = true;
        			
				});
			};
			
			$scope.isReduceDone = function(operationId){
				$http.get(window.contextPath + '/cache/config/operateResult',{
					params : {
						"operationId" : operationId,
					}	
				}).success(function(response){
					$('#modal-wizard4').modal('show');
					$scope.message4 = "销毁实例";
					var operationStatus = response.status;
					if(operationStatus == "200"){//success
						$scope.message4 = "缩容成功";
						$scope.initpage();	
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = true;
	        			$scope.fail = false;
					}else if(operationStatus == "100"){//doing
						
						$timeout(function() {
							$scope.isReduceDone(operationId);
						}, 1000);
						
					}else{//fail
						$scope.message4 = "缩容失败";
						$scope.initpage();
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
	        			
					}
						
				}).error(function(response){
					$scope.message4 = "缩容失败";
					$scope.initpage();
					// 模态框图片切换
        			$scope.process = false;
        			$scope.success = false;
        			$scope.fail = true;
        			
				});
			};
			//临时摘除 2.0
			$scope.dropServer = function(index){
				
				$scope.tmp = $scope.servers[index];
				
				$scope.servers.splice(index,1);//从运行队列中移除该机器
				
				$scope.tempServers = "";
				for(var i = 0; i < $scope.servers.length; i++){
					if(i > 0){
						$scope.tempServers += ";~;";
					}
					$scope.tempServers += $scope.servers[i]; 
				}
				
				$http.post(window.contextPath + '/cache/config/updateServers',
		        		{"key":$scope.mCacheKey,"oldservers":$scope.mServers,
		        		"newservers":$scope.tempServers
		        		}).success(function(response){
		        			var flag = response.flag;
		        			if(flag == true){//成功
		        				$scope.mServers = $scope.tempServers; 
		        				//$scope.servers = $scope.mServers.split(/;~;|\|/);
		        				$scope.dropservers.push($scope.tmp);//加入摘除队列
		        			}else{//失败
		        				$scope.servers = $scope.mServers.split(/;~;|\|/);
		        			}
		    				
		        		}).error(function(response){
		        			$scope.servers = $scope.mServers.split(/;~;|\|/);
		        		});
				
			};
/*****************************    Redis	  **************************************************/
			$scope.instances = 1;
			$scope.appid = "redis10";
			
			$scope.redisAddMasterNode = function(){
				$http.post(window.contextPath + '/redis/addmaster',
		        	{"cluster":$scope.mCacheKey,
						"ip":$scope.ip,
	        			"port":$scope.port
	        		}).success();
			};
			
			$scope.redisAutoScaleNode = function(){
				$http.post(window.contextPath + '/redis/autoscaleup',
		        	{"cluster":$scope.mCacheKey,
					"instances":$scope.instances,
	        			"appid":$scope.appid
	        		}).success();
			};
			
			$scope.delRedisMasterNode = function(address){
				$http.post(window.contextPath + '/redis/delmaster',
			        	{"cluster":$scope.mCacheKey,
						"address":address
		        		}).success();
			};
			
			//重新上线机器
			$scope.resetServer = function(index){
				var add = $scope.dropservers[index].split(":");
				var ipT = add[0];
				var portT = add[1];
				
				//验证该机器是否恢复服务
				$('#modal-wizard2').modal('show');
				$scope.isStep21 = true;
				$scope.isStep22 = false;
				$scope.isStep23 = false;
				
				$scope.message2 = "验证服务器是否开启服务……";
				$http.get(window.contextPath + '/cache/config/validate_port',{
					params : {
			        	"ip":ipT,
			        	"port":portT
			        	}
				}).success(function(response){
					var flag = response.flag;
					if(flag == true){
						$scope.message2 = "服务验证成功!";
						$scope.isStep22 = true;
						if($scope.mServers != ""){
							$scope.tempServers = $scope.mServers+";~;"+ipT + ":" + portT;
						}else{
							$scope.tempServers =ipT + ":" + portT;
						}
						$scope.updateConfigServers(); // 添加服务器，更新服务器列表
						$scope.dropservers.splice(index,1);//从摘除队列中移除
						
						
					}else{
						$scope.message2 = "服务验证失败!";
						$scope.isStep22 = false;
						// 模态框图片切换
	        			$scope.process = false;
	        			$scope.success = false;
	        			$scope.fail = true;
					}

				}).error(function(response){
					$scope.message2 = "服务验证失败!";
					$scope.isStep22 = false;
					// 模态框图片切换
        			$scope.process = false;
        			$scope.success = false;
        			$scope.fail = true;
				});				
				
			};
			
			
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
			};
			
			$scope.initpage();
		} ]);