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
											// 生成链接
//											if (self.currentPage > 1
//													&& self.currentPage < self.totalPage) {
//												self.pages = [
//														self.currentPage - 1,
//														self.currentPage,
//														self.currentPage + 1 ];
//											} else if (self.currentPage == 1
//													&& self.totalPage > 1) {
//												self.pages = [
//														self.currentPage,
//														self.currentPage + 1 ];
//											} else if (self.currentPage == self.totalPage
//													&& self.totalPage > 1) {
//												self.pages = [
//														self.currentPage - 1,
//														self.currentPage ];
//											}
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

				$http.get(window.contextPath + $scope.suburl, {
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
			$scope.category2 = "";
			$scope.categoryForClear = ";"
			$scope.cacheType2 = "";
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
			$scope.suburl = "/cache/key/search";
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
			$scope.setConfigParas = function(){
				$http.get(window.contextPath + '/cache/config/findAll', {
					params : {
					}
				}).success(function(response){
					for (var i=0,len=response.entitys.length; i<len; i++)
						{
						$scope.configParas[i] = response.entitys[i].cacheKey;
						}
				});
				
				$http.get(window.contextPath + '/cache/key/findAllCategory',{
					params : {
					},
					cache: true
					}).success(function(response) {
						$scope.categorySet = response.categorySet;
						$scope.cacheTypeSet = response.cacheTypeSet;
						$('#category').typeahead({
							items: 16, 
							source : $scope.categorySet,
							updater : function(c) {
								$scope.category = c;
								$scope.$apply();
								return c;
							}
						});
						$('#cacheType').typeahead({
							items: 16, 
							source : $scope.cacheTypeSet,
							updater : function(c) {
								$scope.cacheType = c;
								$scope.$apply();
								return c;
							}
						});
						
					}).error(function(response) {
						$scope.categorySet = "null";
						
					});
				
			}

			$scope.setModalInput = function(category , duration, indexTemplate, cacheType,indexDesc,version,hot,sync2Dnet,extension){
				$scope.category2 = category;
				$scope.duration = duration;
				$scope.indexTemplate = indexTemplate;
				$scope.indexDesc = indexDesc;
				$scope.cacheType2 = cacheType;
				$scope.version = version;
				$scope.hot = hot;
				$scope.sync2Dnet = sync2Dnet;
				$scope.extension = extension;
			}
			$scope.creatCategory = function(myForm){
				if(angular.isUndefined($scope.hot)){
					$scope.hot="false";
				}		
				if(angular.isUndefined($scope.sync2Dnet)){
					$scope.sync2Dnet="false";
				}
				if(angular.isUndefined($scope.version)){
					$scope.version="0";
				}
				if(angular.isUndefined($scope.extension)){
					$scope.extension="";
				}
				$('#keyModal').modal('hide');
				
				$http.post(window.contextPath + '/cache/key/create',
	        		{"category":$scope.category1,"duration":$scope.duration,
	        		"indexTemplate":$scope.indexTemplate,"indexDesc":$scope.indexDesc,
	        		"cacheType":$scope.cacheType1,"version":$scope.version,
	        		"hot":$scope.hot,"sync2Dnet":$scope.sync2Dnet,
	        		"extension":$scope.extension}
				).success(function(response) {
					$scope.query();
	        	});
			}

			$scope.updateCategory = function(myForm){

				$('#keyModal2').modal('hide');
				
				if($scope.indexDesc == null){
					$scope.indexDesc = "";
				}
				if($scope.indexTemplate == null){
					$scope.indexTemplate = "";
				}
				if($scope.duration == null){
					$scope.duration = "";
				}
				if($scope.extension == null){
					$scope.extension = "";
				}
				
				$http.post(window.contextPath + '/cache/key/update',
	        		{"category":$scope.category2,"duration":$scope.duration,
	        		"indexTemplate":$scope.indexTemplate,"indexDesc":$scope.indexDesc,
	        		"cacheType":$scope.cacheType2,"version":$scope.version,
	        		"hot":$scope.hot,"sync2Dnet":$scope.sync2Dnet,
	        		"extension":$scope.extension}
				).success(function(response) {
					$scope.query();
	        	});
			}
			
			$rootScope.deleteCacheKeyByCategory = function(category){
				$http.post(window.contextPath + '/cache/key/delete',
	        			{"category":category}).success(function(response) {
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
			}
			
			$scope.clearCache = function(myForm){
				$('#keyModal3').modal('hide');
				$http.post(window.contextPath + '/cache/key/clear',
	        			{"category":$scope.categoryForClear}).success(function(response) {
	        				$scope.query();
	        	});
			}
			$scope.query();
			$scope.setConfigParas();
		} ]);