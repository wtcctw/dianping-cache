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
    'AlarmRuleController',
    [
        '$rootScope',
        '$scope',
        '$http',
        'Paginator',
        'ngDialog',
        '$interval',
        function ($rootScope, $scope, $http, Paginator, ngDialog, $interval) {

            $scope.memcacheClusters;


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
            $scope.suburl = "/setting/memcachetemplate/list";
            $scope.pageSize = 30000;
            $scope.queryCount = 0;

            $scope.query = function () {
                $scope.searchPaginator = Paginator(
                    fetchFunction, $scope.pageSize
                );
            }

            $scope.refreshpage = function (myForm) {
                $('#myModal').modal('hide');
                console.log($scope.alarmConfigEntity);
                $http
                    .post(
                    window.contextPath + '/setting/memcachetemplate/create',
                    $scope.alarmConfigEntity
                )
                    .success(
                    function (data) {
                        $scope.searchPaginator = Paginator(
                            fetchFunction,
                            $scope.pageSize
                        );
                    }
                );
            }

            $scope.clearModal = function () {
                $scope.alarmConfigEntity = {};
                $scope.alarmConfigEntity.isUpdate = false;
            }

            $scope.setModalInput = function (index) {
                $scope.alarmConfigEntity.id = $scope.searchPaginator.currentPageItems[index].id;
                $scope.alarmConfigEntity.clusterName = $scope.searchPaginator.currentPageItems[index].clusterName;
                $scope.alarmConfigEntity.isDown = $scope.searchPaginator.currentPageItems[index].isDown;
                $scope.alarmConfigEntity.memThreshold = $scope.searchPaginator.currentPageItems[index].memThreshold;
                $scope.alarmConfigEntity.qpsThreshold = $scope.searchPaginator.currentPageItems[index].qpsThreshold;
                $scope.alarmConfigEntity.connThreshold = $scope.searchPaginator.currentPageItems[index].connThreshold;
                $scope.alarmConfigEntity.isUpdate = true;
            }

            $rootScope.removerecord = function (cid) {
                console.log(cid);
                $http
                    .get(
                    window.contextPath + "/setting/memcachetemplate/remove",
                    {
                        params: {
                            id: cid
                        }
                    }
                )
                    .success(
                    function (data) {
                        $scope.searchPaginator = Paginator(
                            fetchFunction,
                            $scope.pageSize
                        );
                    }
                );
                return true;
            }

            $scope.dialog = function (cid) {
                $rootScope.cid = cid;
                ngDialog
                    .open({
                        template: '\
                        <div class = "widget-box">\
                        <div class="widget-header">\
                            <h4 class="widget-title">警告</h4>\
                        </div>\
                        <div class="widget-body">\
                            <div class="widget-main">\
                                <p class="alert alert-info">\
                                    您确认要删除吗？\
                                </p>\
                            </div>\
                             <div class="modal-footer">\
                                <button type="button" class="btn btn-default" ng-click="closeThisDialog()">取消</button>\
                                <button type="button" class="btn btn-primary" ng-click="removerecord(cid)&&closeThisDialog()">确定</button>\
                             </div>\
                        </div>\
                        </div>',
                        plain: true,
                        className: 'ngdialog-theme-default'
                    });
            };

            $scope.query();
            $scope.clearModal();

            $http(
                {
                    method:"GET",
                    url:window.contextPath + '/setting/memcachetemplate/query/memcacheclusters'
                }
            ).success(
                function(datas,status,headers,config){
                    $scope.memcacheClusters = datas;
                }
            ).error(
                function(datas,status,headers,config){
                    console.log("memcacheclusters读取错误")
                }
            );

        }
    ]
);