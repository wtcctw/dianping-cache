module
    .factory(
    'Paginator',
    function () {
        return function (fetchFunction, pageSize) {
            var paginator = {
                hasNextVar: false,
                fetch: function (page) {
                    this.currentOffset = (page - 1) * pageSize;
                    this._load();
                },
                next: function () {
                    if (this.hasNextVar) {
                        this.currentOffset += pageSize;
                        this._load();
                    }
                },
                _load: function () {
                    var self = this;//must use self
                    self.currentPage = Math.floor(self.currentOffset / pageSize) + 1;
                    fetchFunction(
                        this.currentOffset,
                        pageSize + 1,
                        function (data) {
                            failDetails = data.failDetails;
                            delayDetails= data.delayDetails;
                            length = data.size;
                            self.totalPage = Math.ceil(length / pageSize);
                            self.endPage = self.totalPage;
                            //生成链接
                            if (self.currentPage > 1 && self.currentPage < self.totalPage) {
                                self.pages = [
                                    self.currentPage - 1,
                                    self.currentPage,
                                    self.currentPage + 1
                                ];
                            } else if (self.currentPage == 1 && self.totalPage > 1) {
                                self.pages = [
                                    self.currentPage,
                                    self.currentPage + 1
                                ];
                            } else if (self.currentPage == self.totalPage && self.totalPage > 1) {
                                self.pages = [
                                    self.currentPage - 1,
                                    self.currentPage
                                ];
                            }
                            self.currentPagefailDetails = failDetails.slice(0, pageSize);
                            self.currentPagedelayDetails = delayDetails.slice(0, pageSize);
                        }
                    );
                },
                hasNext: function () {
                    return this.hasNextVar;
                },
                previous: function () {
                    if (this.hasPrevious()) {
                        this.currentOffset -= pageSize;
                        this._load();
                    }
                },
                hasPrevious: function () {
                    return this.currentOffset !== 0;
                },
                totalPage: 1,
                pages: [],
                lastpage: 0,
                currentPage: 1,
                endPage: 1,

                currentPageItems: [],
                currentOffset: 0
            };

            //加载第一页
            paginator._load();
            return paginator;
        };
    }
);

module
    .controller(
    'AlarmReportController',
    [
        '$rootScope',
        '$scope',
        '$http',
        'Paginator',
        'ngDialog',
        '$interval',
        function ($rootScope, $scope, $http, Paginator, ngDialog, $interval) {
            var fetchFunction = function (offset, limit, callback) {
                $scope.searchEntity = {
                    offset: offset,
                    limit: limit
                };
                $http.get(window.contextPath + $scope.suburl, {
                    params: $scope.searchEntity
                }).success(callback);
            };

            var searchFunction = function (offset, limit, callback){
                $scope.searchParam = {
                    createTime:$scope.createTime
                };

                $http.get(window.contextPath + '/report/search',
                    {
                        params:$scope.searchParam
            }).success(callback);
            };

            $scope.suburl = "/report/list";
            $scope.PageSize = 300;
            $scope.queryCount = 0;

            $scope.query = function () {
                $scope.searchRecordEntity = {};
                $scope.searchPaginator = Paginator(
                    fetchFunction, $scope.PageSize
                );
            }
            $scope.query();


            $scope.search = function (createTime) {
                $scope.createTime = createTime;
                $scope.searchPaginator = Paginator(
                    searchFunction,
                    $scope.PageSize
                );
            };

            $scope.scanjob = function () {
                $http
                    .post(
                    window.contextPath + "/report/scanjob"
                )
                    .success(
                    function (data) {
                        alert("扫描任务已启动……")
                    }
                );
            };


            $http(
                {
                    method: "GET",
                    url: window.contextPath + '/report/getWeekList'
                }
            ).success(
                function (datas, status, headers, config) {
                    $scope.weekList = datas;
                    $scope.history=datas[0];
                }
            ).error(
                function (datas, status, headers, config) {
                    console.log("weekList读取错误")
                }
            );


        }
    ]
);