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
    'Paginator', 'ngDialog',
    function ($rootScope, $scope, $http, Paginator, ngDialog) {
        var fetchFunction = function (callback) {
            $http.get(window.contextPath + $scope.suburl, {
                params: {}
            }).success(callback);
        };

        $scope.mCacheKey;
        $scope.mClientClazz;
        $scope.mServers;
        $scope.mTranscoderClazz;
        $scope.mSwimLane;
        $scope.configurationParams;
        $scope.suburl = "/cache/config/findAll";
        $scope.query = function () {
            $scope.searchPaginator = Paginator(fetchFunction);
        }

        $scope.setModalInput = function (key, clientClazz, servers, transcoderClazz) {
            $scope.mCacheKey = key;
            $scope.mClientClazz = clientClazz;
            $scope.mServers = servers;
            $scope.mTranscoderClazz = transcoderClazz;
        }

        $scope.wrapperParams = function(){
            $scope.configurationParams = {};
            $scope.configurationParams.cacheKey = $scope.mCacheKey;
            $scope.configurationParams.clientClazz =  $scope.mClientClazz;
            $scope.configurationParams.servers = $scope.mServers;
            $scope.configurationParams.swimlane = $scope.mSwimLane;
            $scope.configurationParams.transcoderClazz = $scope.mTranscoderClazz;
        }
        $scope.transportToEdit = function (cacheKey, clientClazz, swimlane, servers, transcoderClazz) {
            window.localStorage.cacheKey = cacheKey;
            window.localStorage.clientClazz = clientClazz;
            window.localStorage.swimlane = swimlane;
            window.localStorage.servers = servers;
            window.localStorage.transcoderClazz = transcoderClazz;
        }

        $rootScope.deleteConfig = function (key, swimlane) {
            $scope.mCacheKey = key;
            $scope.mSwimlane  = swimlane;
            $scope.wrapperParams();
            $http.post(window.contextPath + '/cache/config/delete',$scope.configurationParams
            ).success(function () {
                $scope.searchPaginator = Paginator(fetchFunction);
            });
            return true;
        }

        $scope.dialog = function (key, swimlane) {

            $rootScope.mCacheKey = key;
            $rootScope.mSwimLane = swimlane;
            ngDialog.open({
                template: '\
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
									<button type="button" class="btn btn-primary" ng-click="deleteConfig(mCacheKey,mSwimLane)&&closeThisDialog()">确定</button>\
								</div>\
							</div>\
						</div>',
                plain: true,
                className: 'ngdialog-theme-default'
            });
        };

        $scope.ipKey = "";
        $scope.clearConfig = function () {
            $('#configModal3').modal('hide');
            $scope.ipKey = $("#ipKey").val().replace(/\,/g, "@|$");
            $http.get(window.contextPath + '/cache/config/clear', {
                    params: {
                        cacheKey: $scope.mCacheKey,
                        ipKey: $scope.ipKey
                    }
                }
            ).success(function () {
                $scope.searchPaginator = Paginator(fetchFunction);
            });
            return true;
        }
        $scope.query();
    }]);