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
module.controller('MemcachedController', [
    '$scope',
    '$http',
    function($scope, $http) {

        $scope.categoryList = [];
        $scope.configParas = [];
        $scope.categoryEntity = {
        }

        $scope.initPage = function(){
            $http.get(window.contextPath + '/memcached/detail/1',{
                params : {}
            }).success(function(response){

            }).error(function(){
            });
        };
        $scope.initPage();


        $scope.setParams = function(cluster,address){
            $scope.cluster = cluster;
            $scope.address = address;
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
    } ]);