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
    'AlarmConfigController',
    [
        '$rootScope',
        '$scope',
        '$http',
        'Paginator',
        'ngDialog',
        '$interval',
        function ($rootScope, $scope, $http, Paginator, ngDialog, $interval) {

            $scope.clusterTypes = ["Memcache", "Redis"];
            $scope.alarmTypes = ["Memcache宕机","Memcache内存", "MemcacheQPS","Memcache连接数","Redis宕机","Redis内存","RedisQPS"];
            $scope.memcacheAlarmTemplates;
            $scope.redisAlarmTemplates;
            $scope.alarmTemplates;
            $scope.thresholdTypes = ["上阈值", "下阈值"];
            $scope.memcacheClusters;
            $scope.redisCluters;


            var fetchFunction = function (offset, limit, callback) {
                $scope.alarmRuleSearchEntity = {
                    offset: offset,
                    limit: limit
                };
                console.log($scope.alarmRuleSearchEntity);
                $http.get(window.contextPath + $scope.alarmRuleSuburl, {
                    params: $scope.alarmRuleSearchEntity
                }).success(callback);
            };
            $scope.alarmRuleSuburl = "/config/alarm/list";
            $scope.PageSize = 30000;
            $scope.queryCount = 0;

            $scope.query = function () {
                $scope.alarmRuleSearchPaginator = Paginator(
                    fetchFunction, $scope.PageSize
                );
            }

            $scope.refreshpage = function (configForm) {
                $('#configModal').modal('hide');
                console.log($scope.alarmConfigEntity);
                $http
                    .post(
                    window.contextPath + '/config/alarm/create',
                    $scope.alarmConfigEntity
                )
                    .success(
                    function (data) {
                        $scope.alarmRuleSearchPaginator = Paginator(
                            fetchFunction,
                            $scope.PageSize
                        );
                    }
                );
            }

            $scope.clearModal = function () {
                $scope.alarmConfigEntity = {};
                $scope.alarmConfigEntity.isUpdate = false;
            }

            $scope.setModalInput = function (index) {
                $scope.alarmConfigEntity.id = $scope.alarmRuleSearchPaginator.currentPageItems[index].id;
                $scope.alarmConfigEntity.clusterType = $scope.alarmRuleSearchPaginator.currentPageItems[index].clusterType;
                $scope.alarmConfigEntity.clusterName = $scope.alarmRuleSearchPaginator.currentPageItems[index].clusterName;
                $scope.alarmConfigEntity.alarmTemplate = $scope.alarmRuleSearchPaginator.currentPageItems[index].alarmTemplate;
                $scope.alarmConfigEntity.receiver = $scope.alarmRuleSearchPaginator.currentPageItems[index].receiver;
                $scope.alarmConfigEntity.toBusiness = $scope.alarmRuleSearchPaginator.currentPageItems[index].toBusiness;
                $scope.alarmConfigEntity.isUpdate = true;
            }

            $rootScope.removerecord = function (cid) {
                console.log(cid);
                $http
                    .get(
                    window.contextPath + "/config/alarm/remove",
                    {
                        params: {
                            id: cid
                        }
                    }
                )
                    .success(
                    function (data) {
                        $scope.alarmRuleSearchPaginator = Paginator(
                            fetchFunction,
                            $scope.PageSize
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
            }


            $scope.query();
            $scope.clearModal();

            $http(
                {
                    method: "GET",
                    url: window.contextPath + '/config/alarm/query/memcacheclusters'
                }
            ).success(
                function (datas, status, headers, config) {
                    $scope.memcacheClusters = datas;
                }
            ).error(
                function (datas, status, headers, config) {
                    console.log("memcacheclusters读取错误")
                }
            );

            $http(
                {
                    method: "GET",
                    url: window.contextPath + '/config/alarm/query/redisclusters'
                }
            ).success(
                function (datas, status, headers, config) {
                    $scope.redisClusters = datas;
                }
            ).error(
                function (datas, status, headers, config) {
                    console.log("redisclusters读取错误")
                }
            );

            $http(
                {
                    method: "GET",
                    url: window.contextPath + '/config/alarm/query/memcachetemplates'
                }
            ).success(
                function (datas, status, headers, config) {
                    $scope.memcacheAlarmTemplates = datas;
                }
            ).error(
                function (datas, status, headers, config) {
                    console.log("memcacheAlarmTemplates")
                }
            );

            $http(
                {
                    method: "GET",
                    url: window.contextPath + '/config/alarm/query/redistemplates'
                }
            ).success(
                function (datas, status, headers, config) {
                    $scope.redisAlarmTemplates = datas;
                }
            ).error(
                function (datas, status, headers, config) {
                    console.log("redisAlarmTemplates读取错误")
                }
            );

            //memcacheTemplate
            $scope.memcacheClusters;

            var memcachefetchFunction = function (offset, limit, callback) {
                $scope.memcachesearchEntity = {
                    offset: offset,
                    limit: limit
                };
                console.log($scope.memcachesearchEntity);
                $http.get(window.contextPath + $scope.memcachesuburl, {
                    params: $scope.memcachesearchEntity
                }).success(callback);
            };
            $scope.memcachesuburl = "/setting/memcachetemplate/list";
            $scope.PageSize = 30000;
            $scope.queryCount = 0;

            $scope.memcachequery = function () {
                $scope.memcacheTemplateSearchPaginator = Paginator(
                    memcachefetchFunction, $scope.PageSize
                );
            }

            $scope.memcacherefreshpage = function (memcacheForm) {
                $('#memcacheTemplateModal').modal('hide');
                $http
                    .post(
                    window.contextPath + '/setting/memcachetemplate/create',
                    $scope.memcacheTemplateConfigEntity
                )
                    .success(
                    function (data) {
                        $scope.memcacheTemplateSearchPaginator = Paginator(
                            memcachefetchFunction,
                            $scope.PageSize
                        );
                    }
                );
            }

            $scope.memcacheclearModal = function () {
                $scope.memcacheTemplateConfigEntity = {};
                $scope.memcacheTemplateConfigEntity.isUpdate = false;
            }

            $scope.memcachesetModalInput = function (index) {
                $scope.memcacheTemplateConfigEntity.id = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].id;
                $scope.memcacheTemplateConfigEntity.templateName = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].templateName;
                $scope.memcacheTemplateConfigEntity.mailMode = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].mailMode;
                $scope.memcacheTemplateConfigEntity.smsMode = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].smsMode;
                $scope.memcacheTemplateConfigEntity.weixinMode = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].weixinMode;
                $scope.memcacheTemplateConfigEntity.isDown = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].isDown;
                $scope.memcacheTemplateConfigEntity.checkHistory = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].checkHistory;
                $scope.memcacheTemplateConfigEntity.memSwitch = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].memSwitch;
                $scope.memcacheTemplateConfigEntity.memThreshold = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].memThreshold;
                $scope.memcacheTemplateConfigEntity.memBase = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].memBase;
                $scope.memcacheTemplateConfigEntity.memFluc = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].memFluc;
                $scope.memcacheTemplateConfigEntity.memInterval = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].memInterval;
                $scope.memcacheTemplateConfigEntity.qpsSwitch = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].qpsSwitch;
                $scope.memcacheTemplateConfigEntity.qpsThreshold = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].qpsThreshold;
                $scope.memcacheTemplateConfigEntity.qpsBase = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].qpsBase;
                $scope.memcacheTemplateConfigEntity.qpsFluc = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].qpsFluc;
                $scope.memcacheTemplateConfigEntity.qpsInterval = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].qpsInterval;
                $scope.memcacheTemplateConfigEntity.connSwitch = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].connSwitch;
                $scope.memcacheTemplateConfigEntity.connThreshold = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].connThreshold;
                $scope.memcacheTemplateConfigEntity.connBase = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].connBase;
                $scope.memcacheTemplateConfigEntity.connFluc = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].connFluc;
                $scope.memcacheTemplateConfigEntity.connInterval = $scope.memcacheTemplateSearchPaginator.currentPageItems[index].connInterval;
                $scope.memcacheTemplateConfigEntity.isUpdate = true;
            }

            $rootScope.memcacheremoverecord = function (cid) {
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
                        $scope.memcacheTemplateSearchPaginator = Paginator(
                            memcachefetchFunction,
                            $scope.PageSize
                        );
                    }
                );
                return true;
            }

            $scope.memcachedialog = function (cid) {
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
                                <button type="button" class="btn btn-primary" ng-click="memcacheremoverecord(cid)&&closeThisDialog()">确定</button>\
                             </div>\
                        </div>\
                        </div>',
                        plain: true,
                        className: 'ngdialog-theme-default'
                    });
            };

            $scope.memcachequery();
            $scope.memcacheclearModal();



            //redisTemplate
            $scope.redisClusters;


            var redisfetchFunction = function (offset, limit, callback) {
                $scope.redisSearchEntity = {
                    offset: offset,
                    limit: limit
                };
                console.log($scope.redisSearchEntity);
                $http.get(window.contextPath + $scope.redissuburl, {
                    params: $scope.redisSearchEntity
                }).success(callback);
            };
            $scope.redissuburl = "/setting/redistemplate/list";
            $scope.PageSize = 30000;
            $scope.queryCount = 0;

            $scope.redisQuery = function () {
                $scope.redisTemplateSearchPaginator = Paginator(
                    redisfetchFunction, $scope.PageSize
                );
            }

            $scope.redisrefreshpage = function (redisForm) {
                $('#redisTemplateModal').modal('hide');
                console.log($scope.redisTemplateConfigEntity);
                $http
                    .post(
                    window.contextPath + '/setting/redistemplate/create',
                    $scope.redisTemplateConfigEntity
                )
                    .success(
                    function (data) {
                        $scope.redisTemplateSearchPaginator = Paginator(
                            redisfetchFunction,
                            $scope.PageSize
                        );
                    }
                );
            }

            $scope.redisClearModal = function () {
                $scope.redisTemplateConfigEntity = {};
                $scope.redisTemplateConfigEntity.isUpdate = false;
            }

            $scope.redissetModalInput = function (index) {
                $scope.redisTemplateConfigEntity.id = $scope.redisTemplateSearchPaginator.currentPageItems[index].id;
                $scope.redisTemplateConfigEntity.templateName = $scope.redisTemplateSearchPaginator.currentPageItems[index].templateName;
                $scope.redisTemplateConfigEntity.mailMode = $scope.redisTemplateSearchPaginator.currentPageItems[index].mailMode;
                $scope.redisTemplateConfigEntity.smsMode = $scope.redisTemplateSearchPaginator.currentPageItems[index].smsMode;
                $scope.redisTemplateConfigEntity.weixinMode = $scope.redisTemplateSearchPaginator.currentPageItems[index].weixinMode;
                $scope.redisTemplateConfigEntity.isDown = $scope.redisTemplateSearchPaginator.currentPageItems[index].isDown;
                $scope.redisTemplateConfigEntity.checkHistory = $scope.redisTemplateSearchPaginator.currentPageItems[index].checkHistory;
                $scope.redisTemplateConfigEntity.memSwitch = $scope.redisTemplateSearchPaginator.currentPageItems[index].memSwitch;
                $scope.redisTemplateConfigEntity.memThreshold = $scope.redisTemplateSearchPaginator.currentPageItems[index].memThreshold;
                $scope.redisTemplateConfigEntity.memBase = $scope.redisTemplateSearchPaginator.currentPageItems[index].memBase;
                $scope.redisTemplateConfigEntity.memFluc = $scope.redisTemplateSearchPaginator.currentPageItems[index].memFluc;
                $scope.redisTemplateConfigEntity.memInterval = $scope.redisTemplateSearchPaginator.currentPageItems[index].memInterval;
                $scope.redisTemplateConfigEntity.qpsSwitch = $scope.redisTemplateSearchPaginator.currentPageItems[index].qpsSwitch;
                $scope.redisTemplateConfigEntity.qpsThreshold = $scope.redisTemplateSearchPaginator.currentPageItems[index].qpsThreshold;
                $scope.redisTemplateConfigEntity.qpsBase = $scope.redisTemplateSearchPaginator.currentPageItems[index].qpsBase;
                $scope.redisTemplateConfigEntity.qpsFluc = $scope.redisTemplateSearchPaginator.currentPageItems[index].qpsFluc;
                $scope.redisTemplateConfigEntity.qpsInterval = $scope.redisTemplateSearchPaginator.currentPageItems[index].qpsInterval;
                $scope.redisTemplateConfigEntity.isUpdate = true;
            }

            $rootScope.redisremoverecord = function (cid) {
                console.log(cid);
                $http
                    .get(
                    window.contextPath + "/setting/redistemplate/remove",
                    {
                        params: {
                            id: cid
                        }
                    }
                )
                    .success(
                    function (data) {
                        $scope.redisTemplateSearchPaginator = Paginator(
                            redisfetchFunction,
                            $scope.PageSize
                        );
                    }
                );
                return true;
            }

            $scope.redisDialog = function (cid) {
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
                                <button type="button" class="btn btn-primary" ng-click="redisremoverecord(cid)&&closeThisDialog()">确定</button>\
                             </div>\
                        </div>\
                        </div>',
                        plain: true,
                        className: 'ngdialog-theme-default'
                    });
            };

            $scope.redisQuery();
            $scope.redisClearModal();





            //alarmTemplate

            var alarmfetchFunction = function (offset, limit, callback) {
                $scope.alarmSearchEntity = {
                    offset: offset,
                    limit: limit
                };
                console.log($scope.alarmSearchEntity);
                $http.get(window.contextPath + $scope.alarmsuburl, {
                    params: $scope.alarmSearchEntity
                }).success(callback);
            };
            $scope.alarmsuburl = "/setting/alarmtemplate/list";
            $scope.PageSize = 30000;
            $scope.queryCount = 0;

            $scope.alarmQuery = function () {
                $scope.alarmTemplateSearchPaginator = Paginator(
                    alarmfetchFunction, $scope.PageSize
                );
            }

            $scope.alarmrefreshpage = function (alarmForm) {
                $('#alarmTemplateModal').modal('hide');
                console.log($scope.alarmTemplateConfigEntity);
                $http
                    .post(
                    window.contextPath + '/setting/alarmtemplate/create',
                    $scope.alarmTemplateConfigEntity
                )
                    .success(
                    function (data) {
                        $scope.alarmTemplateSearchPaginator = Paginator(
                            alarmfetchFunction,
                            $scope.PageSize
                        );
                    }
                );
            }

            $scope.alarmClearModal = function () {
                $scope.alarmTemplateConfigEntity = {};
                $scope.alarmTemplateConfigEntity.isUpdate = false;
            }

            $scope.alarmsetModalInput = function (index) {
                $scope.alarmTemplateConfigEntity.id = $scope.alarmTemplateSearchPaginator.currentPageItems[index].id;
                $scope.alarmTemplateConfigEntity.templateName = $scope.alarmTemplateSearchPaginator.currentPageItems[index].templateName;
                $scope.alarmTemplateConfigEntity.alarmType = $scope.alarmTemplateSearchPaginator.currentPageItems[index].alarmType;
                $scope.alarmTemplateConfigEntity.alarmSwitch = $scope.alarmTemplateSearchPaginator.currentPageItems[index].alarmSwitch;
                $scope.alarmTemplateConfigEntity.threshold = $scope.alarmTemplateSearchPaginator.currentPageItems[index].threshold;
                $scope.alarmTemplateConfigEntity.flucSwitch = $scope.alarmTemplateSearchPaginator.currentPageItems[index].flucSwitch;
                $scope.alarmTemplateConfigEntity.fluc = $scope.alarmTemplateSearchPaginator.currentPageItems[index].fluc;
                $scope.alarmTemplateConfigEntity.base = $scope.alarmTemplateSearchPaginator.currentPageItems[index].base;
                $scope.alarmTemplateConfigEntity.alarmInterval = $scope.alarmTemplateSearchPaginator.currentPageItems[index].alarmInterval;
                $scope.alarmTemplateConfigEntity.mailMode = $scope.alarmTemplateSearchPaginator.currentPageItems[index].mailMode;
                $scope.alarmTemplateConfigEntity.smsMode = $scope.alarmTemplateSearchPaginator.currentPageItems[index].smsMode;
                $scope.alarmTemplateConfigEntity.weixinMode = $scope.alarmTemplateSearchPaginator.currentPageItems[index].weixinMode;
                $scope.alarmTemplateConfigEntity.isUpdate = true;
            }

            $rootScope.alarmremoverecord = function (cid) {
                console.log(cid);
                $http
                    .get(
                    window.contextPath + "/setting/alarmtemplate/remove",
                    {
                        params: {
                            id: cid
                        }
                    }
                )
                    .success(
                    function (data) {
                        $scope.alarmTemplateSearchPaginator = Paginator(
                            alarmfetchFunction,
                            $scope.PageSize
                        );
                    }
                );
                return true;
            }

            $scope.alarmDialog = function (cid) {
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
                                <button type="button" class="btn btn-primary" ng-click="alarmremoverecord(cid)&&closeThisDialog()">确定</button>\
                             </div>\
                        </div>\
                        </div>',
                        plain: true,
                        className: 'ngdialog-theme-default'
                    });
            };

            $scope.alarmQuery();
            $scope.alarmClearModal();



        }
    ]
);







