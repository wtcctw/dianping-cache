module.controller('ClusterDashBoardController', [ '$scope', '$http','$document',
		'$timeout', function($scope, $http) {
        $scope.redisdata = [];
        $scope.redisScaleParams = {};
        $scope.cluster;
        $scope.address;

        $scope.transport = function(address){
            window.localStorage.address = address;
        }
        $scope.setParams = function(cluster,address){
            $scope.cluster = cluster;
            $scope.address = address;
        }
        $scope.initDashBoard = function () {
            $http.get(window.contextPath + '/redis/dashboardinfo', {
                params: {},
            }).success(function (response) {
                $scope.redisdata = [];
                response.forEach(function (item) {
                    $scope.redisdata.push(item);

                });
                init();
            }).error(function (response) {
            });
        }


        $scope.refresh = function(cluster){
            $scope.cluster = cluster;
            $http.get(window.contextPath + "/redis/refreshcache",{params :{
                "cluster" : $scope.cluster
            }}
            ).success(function(response){
                $scope.redisdata = [];
                response.forEach(function (item) {
                    $scope.redisdata.push(item);

                });

            });
        }


        $scope.addSlave = function(cluster,address){
            $scope.redisScaleParams = {};
            $scope.redisScaleParams.cluster = cluster;
            $scope.redisScaleParams.masterAddress = address;
            $http.post(window.contextPath + '/redis/addslave', $scope.redisScaleParams
            ).success(function(response) {
            }).error(function(response) {
            });
        };

        $scope.deleteSlave = function(cluster,address){
            $scope.redisScaleParams = {};
            $scope.redisScaleParams.cluster = cluster;
            $scope.redisScaleParams.slaveAddress = address;
            $http.post(window.contextPath + '/redis/deleteslave', $scope.redisScaleParams
            ).success(function(response) {
            }).error(function(response) {
            });
        };

        //$scope.redisDetail = function(cluster){
        //    $scope.cluster = cluster;
        //    $http.get(window.contextPath + "/redis/detail",{params :{
        //        "cluster" : $scope.cluster
        //    }});
        //}

			$scope.initDashBoard();

        var init = function () {
            // check if there is query in url
            // and fire search in case its value is not empty
            $(document).ready(function () {
                setTimeout(function () {
                    $('#redisTable').dataTable({
                        "bAutoWidth": true,
                        "bPaginate": true, //翻页功能
                        "bLengthChange": true, //改变每页显示数据数量
                        "bFilter": true, //过滤功能
                        "bSort": true, //排序功能
                        "bInfo": true,//页脚信息
                        "bStateSave": false,
                        "aaSorting": [],
                        "iDisplayLength": 10,
                        "aoColumns": [
                            {"bSortable": false},null, null, null, {"bSortable": false},
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                    var obj = document.getElementById("redisTable_length");
                    obj.innerHTML='<div style="margin-top: 5px"> <div class="col-sm-1">总览</div> '+
                        '</div><div class="col-sm-8"><div class="progress progress-small"> <div class="progress-bar progress-bar-success" style="width: 75%;background-color:#68ff5c;position: relative;"></div>' +
                        '<div class="progress-bar progress-bar-danger" style="width: 25%;background-color:#ff6a6a;position: relative;"></div></div></div>' +
                        '<div class="col-sm-3">共有集群80个</div> </div>';
                }, 0);


            });
        };
        // and fire it after definition

    } ]);