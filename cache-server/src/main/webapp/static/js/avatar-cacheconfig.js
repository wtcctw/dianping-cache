module.factory('Paginator',function() {
					return function(fetchFunction) {
						var paginator = {
							_load : function() {
								var self = this; // must use self
								fetchFunction(function(response) {
											items = response.entitys;
											self.currentPageItems = items;
										});
							},
							currentPageItems : []
						};
						paginator._load();
						return paginator;
					};
				});

module.controller('ConfigController', [
        '$rootScope',
		'$scope',
		'$http',
		'Paginator','ngDialog',
		function($rootScope,$scope, $http, Paginator,ngDialog) {
			var fetchFunction = function(callback) {
				$http.get(window.contextPath + $scope.suburl, {
					params : {
					}
				}).success(callback);
			};
			
			$scope.mCacheKey = "";
			$scope.mClientClazz = "";
			$scope.mServers = "";
			$scope.mTranscoderClazz = "";
			$scope.suburl = "/cache/config/findAll";
			$scope.query = function() {

				$scope.searchPaginator = Paginator(fetchFunction);
			}
			
			$scope.setModalInput = function(key,clientClazz,servers,transcoderClazz){
				$scope.mCacheKey = key;
				$scope.mClientClazz = clientClazz;
				$scope.mServers = servers;
				$scope.mTranscoderClazz = transcoderClazz;
			}
			
			$scope.transportToEdit = function(cacheKey, clientClazz, servers, transcoderClazz){
				window.localStorage.cacheKey = cacheKey;
				window.localStorage.clientClazz = clientClazz;
				window.localStorage.servers = servers;
				window.localStorage.transcoderClazz = transcoderClazz;
			}
			
			$scope.refreshpage = function(myForm){
				
	        	$('#myModal').modal('hide');
	        	
		        $http.post(window.contextPath + '/cache/config/update',
		        		{"key":$scope.mCacheKey,"clientClazz":$scope.mClientClazz,
		        		"servers":$scope.mServers,"transcoderClazz":$scope.mTranscoderClazz}).success(function(response) {
						$scope.searchPaginator = Paginator(fetchFunction);
		        });
	        	
	        }
			
			$scope.configEdit = function(key,clientClazz,servers,transcoderClazz){
				
				$http.get(window.contextPath + '/cache/config/edit',
		        	{"key":key,"clientClazz":clientClazz,
	        		"servers":servers,"transcoderClazz":transcoderClazz});
				
			}
			
			$scope.creatNew = function(myForm){

				$('#myModal2').modal('hide');
				
				$http.post(window.contextPath + '/cache/config/create',
	        		{"key":$scope.mCacheKey,"clientClazz":$scope.mClientClazz,
	        		"servers":$scope.mServers,"transcoderClazz":$scope.mTranscoderClazz}).success(function(response) {
					$scope.searchPaginator = Paginator(fetchFunction);
	        	});
			}
			
			$rootScope.deleteConfig = function(key){
				$http.post(window.contextPath + '/cache/config/delete',
	        			{"key":key}).success(function(response) {
					$scope.searchPaginator = Paginator(fetchFunction);
	        	});
				return true;
			}
			
			$scope.dialog = function(key) {
				
				$rootScope.mCacheKey = key;
				ngDialog.open({
							template : '\
							<div class="widget-box">\
							<div class="widget-header">\
								<h4 class="widget-title">确认删除</h4>\
							</div>\
							<div class="widget-body">\
								<div class="widget-main">\
										<h3 style="color:red">{{mCacheKey}}</h3>\
								</div>\
								<div class="modal-footer">\
									<button type="button" class="btn btn-default" ng-click="closeThisDialog()">取消</button>\
									<button type="button" class="btn btn-primary" ng-click="deleteConfig(mCacheKey)&&closeThisDialog()">确定</button>\
								</div>\
							</div>\
						</div>',
						plain : true,
						className : 'ngdialog-theme-default'
				});
			};
			
			$scope.ipKey="";
			$scope.clearConfig = function(myForm3){
				$('#configModal3').modal('hide');
				$scope.ipKey = $("#ipKey").val().replace(/\,/g,"@|$");
				$http.post(window.contextPath + '/cache/config/clear',
	        			{"cacheKey":$scope.mCacheKey,"ipKey":$scope.ipKey}).success(function(response) {
					$scope.searchPaginator = Paginator(fetchFunction);
	        	});
				return true;
			}

			
			$scope.implItems = ["com.dianping.cache.memcached.MemcachedClientImpl",
			                    "com.dianping.cache.redis.RedisClusterClientImpl",
			                    "com.dianping.cache.dcache.DCacheClientImpl",
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
			
			$scope.query();
		} ]);