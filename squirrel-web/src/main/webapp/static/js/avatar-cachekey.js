module.factory('Paginator',function() {
					return function(fetchFunction, pageId,searchBy, category,cacheType) {
						var paginator = {
							hasNextVar : false,
							fetch : function(pageId) {
								this.currentPage = pageId;
								this._load();
							},
							next : function() {
								if (this.hasNextVar) {
									this.currentPage += 1;
									this._load();
								}
							},
							_load : function() {
								var self = this; // must use self
								fetchFunction(
										this.currentPage,
										searchBy,
										category,
										cacheType,
										function(response) {
											items = response.entitys;
											self.totalPage = response.totalpage;
											self.endPage = self.totalPage;
											self.pages = [self.currentPage];
											self.currentPageItems = items;
											self.hasNextVar = self.currentPage < self.totalPage;
										});
							},
							hasNext : function() {
								return this.hasNextVar;
							},
							previous : function() {
								if (this.hasPrevious()) {
									this.currentPage -= 1;
									this._load();
								}
							},
							hasPrevious : function() {
								return this.currentPage !== 1;
							},
							totalPage : 1,
							pages : [],
							lastpage : 0,
							currentPage : 1,
							endPage : 1,

							currentPageItems : [],
							currentOffset : 1
						};

						// 加载第一页
						paginator._load();
						return paginator;
					};
				});

module.controller('KeyController', [
		'$scope',
		'$http',
		'$rootScope',
		'Paginator',
		'ngDialog',
		function($scope, $http, $rootScope,Paginator,ngDialog) {
			var fetchFunction = function(pageId,searchBy,category, cacheType, callback) {
				$http.get(window.contextPath + '/cache/key/search', {
					params : {
						pageId : pageId,
						searchBy : searchBy,
						category : category,
						cacheType : cacheType
					}
				}).success(callback);
			};

			$scope.category = "";
			$scope.category1 = "";
			$scope.categoryForClear = ";"
			$scope.cacheType1 = "";
			$scope.cacheType = "";
			$scope.duration="";
			$scope.indexTemplate="";
			$scope.indexDesc = "";
			$scope.version = "";
			$scope.hot = "";
			$scope.sync2Dnet = "";
			$scope.indexDesc = "";
			$scope.extension = "";
			$scope.pageId = 1;
			$scope.searchBy = "Category";
			$scope.queryCount = 0;
			$scope.configParas=[];
			$scope.cacheTypeSet=[];
			$scope.categorySet=[];
			$scope.query = function() {
				$scope.queryCount = $scope.queryCount + 1;
				if(angular.isUndefined($scope.category)){
					$scope.category="";
				}
				if(angular.isUndefined($scope.cacheType)){
					$scope.cacheType="";
				}
				$scope.searchPaginator = Paginator(fetchFunction,
						$scope.pageId, $scope.searchBy, $scope.category, $scope.cacheType);
			}
			$scope.setConfigParas = function () {
				$http.get(window.contextPath + '/cache/config/findAll', {params: {}}
				).success(function (response) {
					for (var i = 0, len = response.entitys.length; i < len; i++) {
						$scope.configParas[i] = response.entitys[i].cacheKey;
					}
				});
				$http.get(window.contextPath + '/cache/key/findAllCategory', {
					params: {},
					cache: true
				}).success(function (response) {
					$scope.categorySet = response.categorySet;
					$scope.cacheTypeSet = response.cacheTypeSet;
					$('#category').typeahead({
						items: 16,
						source: $scope.categorySet,
						updater: function (c) {
							$scope.category = c;
							$scope.$apply();
							return c;
						}
					});
					$('#cacheType').typeahead({
						items: 16,
						source: $scope.cacheTypeSet,
						updater: function (c) {
							$scope.cacheType = c;
							$scope.$apply();
							return c;
						}
					});

				}).error(function (response) {
					$scope.categorySet = "null";
				});

			}
			$scope.categoryParams;
			$scope.setModalInput = function(category , duration, indexTemplate, cacheType,indexDesc,version,hot,sync2Dnet,extension){
				$scope.category1 = category;
				$scope.duration = duration;
				$scope.indexTemplate = indexTemplate;
				$scope.indexDesc = indexDesc;
				$scope.cacheType1 = cacheType;
				$scope.version = version;
				$scope.hot = hot;
				$scope.sync2Dnet = sync2Dnet;
				$scope.extension = extension;
			}

			$scope.wrapperParams = function () {
				$scope.categoryParams = {};
				$scope.categoryParams.category = $scope.category1;
				$scope.categoryParams.duration = $scope.duration;
				$scope.categoryParams.indexTemplate = $scope.indexTemplate;
				$scope.categoryParams.indexDesc = $scope.indexDesc;
				$scope.categoryParams.cacheType = $scope.cacheType1;
				$scope.categoryParams.version = $scope.version;
				$scope.categoryParams.hot = $scope.hot;
				$scope.categoryParams.sync2Dnet = $scope.sync2Dnet;
				$scope.categoryParams.extension = $scope.extension;

			}
			$scope.creatCategory = function(){
				$('#keyModal').modal('hide');
				$scope.wrapperParams();
				$http.post(window.contextPath + '/cache/key/create',$scope.categoryParams
				).success(function() {
					$scope.query();
	        	});
			}

			$scope.updateCategory = function(){
				$('#keyModal2').modal('hide');
				$scope.wrapperParams();
                $http.post(window.contextPath + '/cache/key/update',$scope.categoryParams
                ).success(function() {
					$scope.query();
	        	});
			}
			
			$rootScope.deleteCacheKeyByCategory = function(category){
				$scope.wrapperParams();
				$http.post(window.contextPath + '/cache/key/delete',$scope.categoryParams
				).success(function() {
					$scope.query();
	        	});
				return true;
			}
			
			$scope.dialog = function(category) {
				$rootScope.mCategory = category;
				ngDialog.open({
							template : '\
							<div class="widget-box">\
							<div class="widget-header">\
								<h4 class="widget-title">警告</h4>\
							</div>\
							<div class="widget-body">\
								<div class="widget-main">\
									<p class="alert alert-info">\
										确认删除 {{mCategory}} ?\
									</p>\
								</div>\
								<div class="modal-footer">\
									<button type="button" class="btn btn-default" ng-click="closeThisDialog()">取消</button>\
									<button type="button" class="btn btn-primary" ng-click="deleteCacheKeyByCategory(mCategory)&&closeThisDialog()">确定</button>\
								</div>\
							</div>\
						</div>',
						plain : true,
						className : 'ngdialog-theme-default'
				});
			};
			
			$scope.setClearCategory = function(category){
				$scope.categoryForClear = category;
				$scope.category1 = category;
			}
			
			$scope.applist=[];
			$scope.getAppList = function(category){
				$scope.category1 = category;
				$scope.wrapperParams();
				$http.post(window.contextPath + '/cache/key/applist',$scope.categoryParams
				).success(function(response) {
					if(response != null){
						$scope.applist = [];
						response.forEach(function(item) {
							$scope.applist.push(item.application);
						});
					}
	        	});
			}

			$scope.clearCache = function(){
				$('#keyModal3').modal('hide');
				$scope.wrapperParams();
				$http.post(window.contextPath + '/cache/key/clear',$scope.categoryParams
				).success(function() {
					$scope.query();
	        	});
			}
			$scope.query();
			$scope.setConfigParas();
		} ]);