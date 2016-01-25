module.controller('RedisDashBoardController', [ '$scope', '$http','$document','$rootScope',
    '$timeout', function($scope, $http,$rootScope) {
        $scope.redisdata = [];
        $scope.applications = [];
        $scope.application;
        $scope.logs = [];
        $scope.cluster;
        $scope.password;
        $scope.authEntity = {};
        $scope.clusterEntity = {
            clusterName:"redis-",
            nodesNumber:3,
            readTimeout:1000,
            connTimeout:1000,
            appId:"redis10"
        };
        $scope.initDashBoard = function () {
            $http.get(window.contextPath + '/redis/data/dashboard', {
                params: {},
            }).success(function (response) {
                response.datas.forEach(function (item) {
                    $scope.redisdata.push(item);
                });
                var num1 = response.totalNum;
                var num2 = response.dangerNum;
                var width2 = num2 * 100 / num1;
                var width1 = 100 - width2;
                init(num1,num2,width1,width2);
            }).error(function () {
            });
        }

        $scope.transport = function(cluster){
            window.localStorage.cluster = cluster;
            window.localStorage.swimlane = '';
        }


        $scope.newCluster = function(){
            $http.post('/redis/new',$scope.clusterEntity)
                .success(function(data){

                });
        }



        $scope.cat = function(cluster){
            var domain = "redis";
            var ipAddrs = "";
            var nodes = cluster.nodes;
            for(var i = 0; i < nodes.length; i++){
                if(i == 0){
                    domain += nodes[i].master.info.maxMem;
                }else{
                    ipAddrs += "_";
                }
                ipAddrs += nodes[i].master.address.split(/:/)[0];
            }
            var url = "http://cat.dp/cat/r/system?domain="+domain+"&type=paasSystem&ipAddrs=" + ipAddrs;
            window.open(url);
        }
        $scope.getLogs = function(cluster){
            $http.get(window.contextPath + '/auditlog/search/'+cluster, {params: {
                }}
            ).success(function (data) {
                $scope.logs = data;
            });
        }

        $scope.setContent = function(content){
            $scope.logContent = content;
        }

        $scope.getApplications = function(cluster){
            $scope.cluster = cluster;
            $http.get(window.contextPath + '/redis/data/applications', {params: {
                    cluster:cluster,
                }}
            ).success(function (data) {
                $scope.applications = data;
                //$(document).ready(function () {
                //    setTimeout(function () {
                //       $('#appTable'+cluster).dataTable({
                //            "bDestroy": true,
                //            "bAutoWidth": true,
                //            "bPaginate": true, //翻页功能
                //            "bLengthChange": true, //改变每页显示数据数量
                //            "bFilter": true, //过滤功能
                //            "bSort": true, //排序功能
                //            "bInfo": false,//页脚信息
                //            "bStateSave": false,
                //            "aaSorting": [],
                //            "iDisplayLength": 10,
                //            "aoColumns": [
                //                {"bSortable": false},{"bSortable": false}, {"bSortable": false}, {"bSortable": false}
                //            ],
                //        });
                //        var obj = document.getElementById("appTable_length");
                //        obj.innerHTML=' ';
                //    }, 100);
                //});
            });
        }

        $scope.authorize = function(cluster,application){
            $scope.authEntity.resource = cluster;
            $scope.authEntity.application = application;
            $http.post('/auth/authorize',$scope.authEntity)
                .success(function(data){

                });
        }
        $scope.unauthorize = function(cluster,application){
            $scope.authEntity.resource = cluster;
            $scope.authEntity.application = application;
            $http.post('/auth/unauthorize',$scope.authEntity)
                .success(function(data){

                });
        }

        $scope.getPassword = function(cluster){
            $http.get(window.contextPath + '/redis/data/password', {params: {
                    cluster:cluster,
                }}
            ).success(function (data) {
                $scope.password = data;
            });
        }

        $scope.setStrictAndPassword = function(strict,password){
            $http.get(window.contextPath + '/redis/data/password', {params: {
                    cluster:cluster,
                }}
            ).success(function (data) {
                $scope.password = data;
            });
        }

        $scope.initDashBoard();
        var init = function (num1,num2,width1,width2) {
            $(document).ready(function () {
                setTimeout(function () {
                    $('#redisTable').dataTable({
                        "bAutoWidth": true,
                        "bPaginate": true, //翻页功能
                        "bLengthChange": true, //改变每页显示数据数量
                        "bFilter": true, //过滤功能
                        "bSort": true, //排序功能
                        "bInfo": false,//页脚信息
                        "bStateSave": false,
                        "aaSorting": [],
                        "iDisplayLength": 10,
                        "aoColumns": [
                            {"bSortable": false},null, null, null, {"bSortable": false},
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                    var obj = document.getElementById("redisTable_length");
                    obj.innerHTML='<div style="margin-top: 5px"> <div class="col-sm-2">总览</div> '+
                        '</div><div class="col-sm-7" style="margin-top: 3px"><div class="progress progress-small"> <div class="progress-bar progress-bar-success" style="width:'+width1+'%;background-color:#68ff5c;position: relative;"></div>' +
                        '<div class="progress-bar progress-bar-danger" style="width: '+width2+'%;background-color:#ff6a6a;position: relative;"></div></div></div>' +
                        '<div class="col-sm-3">共'+num1+'个,告警'+num2+'个</div> </div>';
                }, 0);
            });
        };



    } ]);