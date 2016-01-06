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
        $scope.categoryList = [];
        $scope.avgsrc = [];
        $scope.exportsrc = [];
        $scope.exportdes = [];
        $scope.reshardType;
        $scope.initPage = function(){
            $http.get(window.contextPath + '/redis/detail',{
                params : {}
            }).success(function(response){
                $scope.redisData = response.data.redisCluster;
                $scope.categoryList = response.categoryList;
                var usage = response.data.memoryUsage;
                var qps = response.data.qps;
                makeGraph("container-memory",100,"内存使用率",usage);
                makeGraph("container-qps",140000,"QPS",qps);
            }).error(function(){
            });
        };
        $scope.initPage();


        $scope.reshard = function(){
            if($scope.reshardType = "average"){

                $scope.avgsrc.push();
            }else if($scope.reshardType = "export"){
                $scope.exportsrc.push();
                $scope.exportdes.push();
            }
        }

    } ]);