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
                            items = data.entities;
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
                            self.currentPageItems = items.slice(0, pageSize);
                            self.hasNextVar = items.length === pageSize + 1;
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
    'AlarmRecordController',
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
                console.log($scope.searchEntity);
                $http.get(window.contextPath + $scope.suburl, {
                    params: $scope.searchEntity
                }).success(callback);
            };
            $scope.suburl = "/setting/alarmrecord/list";
            $scope.pageSize = 30;
            $scope.queryCount = 0;

            $scope.query = function () {
                $scope.searchPaginator = Paginator(
                    fetchFunction, $scope.pageSize
                );
            }

            $scope.query();
        }
    ]
);