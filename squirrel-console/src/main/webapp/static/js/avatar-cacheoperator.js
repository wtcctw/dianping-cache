module.factory('Paginator',function() {
					return function(fetchFunction, pageId, operator,content,
							startTime, endTime) {
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
										operator,
										content,
										startTime,
										endTime,
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

module.controller('OperatorController', [
		'$scope',
		'$http',
		'Paginator',
		function($scope, $http, Paginator) {
			var fetchFunction = function(pageId, operator, content, startTime,
					endTime, callback) {

				$http.get(window.contextPath + $scope.suburl, {
					params : {
						pageId : pageId,
						operator : operator,
						content : content,
						startTime : startTime,
						endTime : endTime
					}
				}).success(callback);
			};

			$scope.operator = "";
			$scope.content = "";
			$scope.modalcontent = "";
			$scope.startTime = "";
			$scope.endTime = "";
			$scope.suburl = "/cache/operator/search";
			$scope.pageId = 1;
			$scope.pageSize = 20;
			$scope.queryCount = 0;
			$scope.query = function() {
				if ($scope.queryCount != 0) {
					$scope.startTime = $("#starttime").val();
					$scope.endTime = $("#stoptime").val();
				}
				$scope.queryCount = $scope.queryCount + 1;
				
				if(angular.isUndefined($scope.operator)){
					$scope.operator="";
				}
				if(angular.isUndefined($scope.content)){
					$scope.content="";
				}

				if ($scope.startTime != null && $scope.endTime != null) {
					startDate = new Date($scope.startTime);
					endDate = new Date($scope.endTime);
					if (endDate <= startDate) {
						alert("结束时间不能小于开始时间");
						return;
					}
				}

				$scope.searchPaginator = Paginator(fetchFunction,
						$scope.pageId, $scope.operator, $scope.content,$scope.startTime,
						$scope.endTime);
			}
			$scope.setContent = function(content){
				$scope.modalcontent = content;
			}

			$scope.query();
		} ]);