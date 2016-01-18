function makeGraph(divName,max,title,currentValue) {
        var gaugeOptions = {
            chart: {
                type: 'solidgauge'
            },
            title: null,
            pane: {
                center: ['50%', '85%'],
                size: '100%',
                startAngle: -90,
                endAngle: 90,
                background: {
                    backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                    innerRadius: '60%',
                    outerRadius: '100%',
                    shape: 'arc'
                }
            },

            tooltip: {
                enabled: false
            },

            // the value axis
            yAxis: {
                stops: [
                    [0.1, '#55BF3B'], // green
                    [0.5, '#DDDF0D'], // yellow
                    [0.7, '#DF5353'] // red
                ],
                lineWidth: 0,
                minorTickInterval: null,
                tickPixelInterval: 400,
                tickWidth: 0,
                title: {
                    y: -70
                },
                labels: {
                    y: 16
                }
            },

            plotOptions: {
                solidgauge: {
                    dataLabels: {
                        y: 5,
                        borderWidth: 0,
                        useHTML: true
                    }
                }
            }
        };

        // The speed gauge
        $('#'+divName).highcharts(Highcharts.merge(gaugeOptions, {
            yAxis: {
                min: 0,
                max: max,
                title: {
                    text: title+ ':' + currentValue,
                }
            },

            credits: {
                enabled: false
            },

            series: [{
                name: title + ':' + currentValue,
                data: [currentValue],
                dataLabels: {
                    format: '<div style="text-align:center"><span style="font-size:25px;color:' +
                    ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y}</span></div>'
                },
                tooltip: {
                    valueSuffix: ' %'
                }
            }]
        }))
}
module.controller('RedisController', [
    '$scope',
    '$http',
    function($scope, $http) {
        $scope.redisData = {};
        $scope.reshardParams = {};
        $scope.redisScaleParams = {};
        $scope.categoryList = [];
        $scope.avgsrc = [];
        $scope.exportsrc = [];
        $scope.exportdes = [];
        $scope.reshardType;
        $scope.failover;
        $scope.configParas = [];
        $scope.categoryEntity = {
        }



        $scope.initPage = function(){
            $http.get(window.contextPath + '/redis/detail',{
                params : {}
            }).success(function(response){
                $scope.redisData = response.data.redisCluster;
                $scope.categoryList = response.categorys;
                var usage = response.data.memoryUsage;
                var qps = response.data.qps;
                makeGraph("container-memory",100,"内存使用率",usage);
                makeGraph("container-qps",140000,"QPS",qps);
                iniTable("node");
                initCategory();
            }).error(function(){
            });
        };
        $scope.initPage();


        $scope.reshard = function(){
            $('#reshardModal').modal('hide');

            $scope.avgsrc = [];
            $scope.exportsrc = [];
            $scope.exportdes = [];
            $scope.reshardParams = {};
            $scope.reshardParams.cluster = $scope.redisData.clusterName;
            if($scope.reshardType === "average"){
                var avgTemp = $('.avg');
                for(var i = 0;i<avgTemp.length;i++){
                    if(avgTemp[i].checked){
                        $scope.avgsrc.push($scope.redisData.nodes[i].master.address);
                    }
                }

                $scope.reshardParams.isAverage = true;
                $scope.reshardParams.srcNodes = $scope.avgsrc;
            }else if($scope.reshardType === "export"){
                var src = $('.exsrc');
                var des = $('.exdes');
                for(var i = 0;i<src.length;i++){
                    if(src[i].checked){
                        $scope.exportsrc.push($scope.redisData.nodes[i].master.address);
                    }
                }
                for(var i = 0;i<des.length;i++){
                    if(des[i].checked){
                        $scope.exportdes.push($scope.redisData.nodes[i].master.address);
                    }
                }

                $scope.reshardParams.isAverage = false;
                $scope.reshardParams.srcNodes = $scope.exportsrc;
                $scope.reshardParams.desNodes = $scope.exportdes;
            }

            $http.post(window.contextPath + '/redis/reshard',$scope.reshardParams)
                .success();
        }

        $scope.setParams = function(cluster,address){
            $scope.cluster = cluster;
            $scope.address = address;
        }

        $scope.transport = function(address){
            window.localStorage.address = address;
        }

        $scope.transportEntity = function(config){
            $scope.configParas = [];
            $http.get(window.contextPath + '/config/cluster/findAll', {params: {}}
            ).success(function (data) {
                for (var i = 0, len = data.length; i < len; i++) {
                    $scope.configParas[i] = data[i].cacheKey;
                }
            });
            $scope.categoryEntity = config;
        }



        $scope.deleteSlave = function(cluster,address){
            $scope.redisScaleParams = {};
            $scope.redisScaleParams.cluster = cluster;
            $scope.redisScaleParams.slaveAddress = address;
            $http.post(window.contextPath + '/redis/deleteslave', $scope.redisScaleParams
            ).success(function(response) {
            }).error(function(response) {
            });
        };

        $scope.addSlave = function(cluster,address){
            $scope.redisScaleParams = {};
            $scope.redisScaleParams.cluster = cluster;
            $scope.redisScaleParams.masterAddress = address;
            $http.post(window.contextPath + '/redis/addslave', $scope.redisScaleParams
            ).success(function(response) {
            }).error(function(response) {
            });
        };

        $scope.setFailover = function(failover){
            $scope.failover = failover;
        }

        $scope.execFailover = function(){
            loadmask();
            $scope.redisScaleParams.cluster = $scope.redisData.clusterName;
            $scope.redisScaleParams.slaveAddress = $scope.failover;
            $http.post(window.contextPath + '/redis/failover',$scope.redisScaleParams)
                .success(function(response){
                    if(response == true){
                        hidemask();
                    }else{
                        hidemask();
                    }
                });
        }
        var iniTable = function (table) {
            $(document).ready(function () {
                setTimeout(function () {
                    $('#'+table).dataTable({
                        "bAutoWidth": true,
                        "bPaginate": false, //翻页功能
                        "bFilter": true, //过滤功能
                        "bInfo": false,//页脚信息
                        "bStateSave": false,
                        "aaSorting": [],
                        "aoColumns": [
                            {"bSortable": false},{"bSortable": false}, {"bSortable": false}, {"bSortable": false}, {"bSortable": false},
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                }, 0);
            });
        };
        var initCategory = function () {
            $(document).ready(function () {
                setTimeout(function () {
                    $('#category').dataTable({
                        "bAutoWidth": true,
                        "bPaginate": true, //翻页功能
                        "bFilter": true, //过滤功能
                        "bStateSave": false,
                        "bInfo": false,//页脚信息
                        "iDisplayLength": 50,
                        "aaSorting": [],
                        "aoColumns": [
                            {"bSortable": false},{"bSortable": false}, {"bSortable": false}, {"bSortable": false}, {"bSortable": false},
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                }, 0);
            });
        };

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


    } ]);