module.controller('ClusterDashBoardController', [ '$scope', '$http',
		'$timeout', function($scope, $http,$timeout) {

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

			$scope.initDashBoard();
		} ]);