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
    '$timeout',
    function($scope, $http,$timeout) {
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

            $http.get(window.location.pathname+'/detail',{
                params : {}
            }).success(function(response){
                $scope.redisData = response.redisCluster;
                var usage = response.memoryUsage;
                var qps = response.qps;
                makeGraph("container-memory",100,"内存使用率",usage);
                makeGraph("container-qps",140000,"QPS",qps);
                iniTable("node");
                piechart("container-rate",response.rate);
            }).error(function(){
            });

            $http.get(window.contextPath + '/config/category/findbycluster/rdb',{
                params: {
                    cluster : window.localStorage.cluster
                }
            }).success(function(response){
                $scope.categoryList = response;
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
            $('#mask').modal('show');
            $scope.redisScaleParams = {};
            $scope.redisScaleParams.cluster = cluster;
            $scope.redisScaleParams.masterAddress = address;
            $http.post(window.contextPath + '/redis/addslave', $scope.redisScaleParams
            ).success(function(response) {
            }).error(function(response) {
            });
            $timeout(function() {
                $('#mask').modal('hide');
            }, 15000);
        };

        $scope.setFailover = function(failover){
            $scope.failover = failover;
        }

        $scope.execFailover = function(){
            $('#mask').modal('show');
            $scope.redisScaleParams.cluster = $scope.redisData.clusterName;
            $scope.redisScaleParams.slaveAddress = $scope.failover;
            $http.post(window.contextPath + '/redis/failover',$scope.redisScaleParams)
                .success(function(response){
                    if(response == true){
                    }else{
                    }
                });
            $timeout(function() {
                $('#mask').modal('hide');
            }, 5000);
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
                            {"bSortable": false},null,null, {"bSortable": false}
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

        $scope.initCharts = function() {
            $http.get(window.location.pathname+'/period',{
                params : {}
            }).success(function(response){
                cloumnchart("container-incr",response);
            }).error(function(){
            });
        }
        $scope.initCharts();

        var piechart = function (divName,seriesdata) {
            $(function () {
                $('#'+divName).highcharts({
                    chart: {
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false
                    },
                    title: {
                        text: ''
                    },
                    tooltip: {
                        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b><b>{point.address}</b>'
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            }
                        }
                    },
                    series: [{
                        type: 'pie',
                        name: '实例占比',
                        data: seriesdata,
                        address: 'dsdd'
                    }]
                });
            });
        }
        var cloumnchart = function (divName,item) {
            $(function () {
                $('#'+divName).highcharts({
                    chart : {
                        type : "column",
                    },
                    title : {
                        text : item.title,
                        x : 0
                        //center
                    },
                    subtitle : {
                        text : item.subTitle,
                        x : 0
                    },
                    xAxis : {
                        type : 'datetime'
                    },
                    yAxis : {
                        title : {
                            text : item.yAxisTitle
                        },
                        min : 0,
                        plotLines : [ {
                            value : 0,
                            width : 10,
                            color : '#808080'
                        } ]
                    },
                    tooltip : {
                        valueSuffix : ''
                    },
                    legend : {
                        layout : 'vertical',
                        align : 'right',
                        verticalAlign : 'middle',
                        borderWidth : 0
                    },
                    plotOptions : {
                        series : {
                            pointStart : item.plotOption.series.pointStart + 8 * 3600 * 1000,
                            pointInterval : item.plotOption.series.pointInterval
                            // one day
                        },
                        spline : {
                            marker : {
                                enabled : false
                            }
                        }
                    },
                    series : item.series
                });
            });
        }

    } ]);