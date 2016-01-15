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
                    format: '<div style="text-align:center"><span style="font-size:20px;color:' +
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
        $scope.mCacheKey;
        $scope.mClientClazz;
        $scope.mServers;
        $scope.mSwimLane;
        $scope.mTranscoderClazz;
        $scope.configuration;
        $scope.servers;
        $scope.scaleType="auto";
        $scope.nodes = [];
        $scope.categoryList = [];
        $scope.configParas = [];
        $scope.categoryEntity = {
        };
        $scope.configurationParams = {};
        $scope.wrapperParams = function () {
            $scope.configurationParams = {};
            $scope.configurationParams.cacheKey = $scope.mCacheKey;
            $scope.configurationParams.clientClazz = $scope.mClientClazz;
            $scope.configurationParams.servers = $scope.mServers;
            $scope.configurationParams.swimlane = $scope.mSwimLane;
            $scope.configurationParams.transcoderClazz = $scope.mTranscoderClazz;
        }
        $scope.initPage = function(){
            $scope.mCacheKey = window.localStorage.cacheKey;
            $scope.mSwimLane = window.localStorage.swimlane;
            $scope.wrapperParams();

            $http.post(window.contextPath + '/memcached/'+$scope.mCacheKey+'/detaildata'
            ).success(function(response){
                var used = response.memoryUsage;
                var qps = response.qps;
                makeGraph("container-memory",100,"内存使用率",used);
                makeGraph("container-qps",140000,"QPS",qps);
            });



            $http.post(window.contextPath + '/config/configuration/1',$scope.configurationParams
            ).success(function(response){
                $scope.initValue(response);
                $scope.servers = [];
                if($scope.mServers != null){
                    $scope.nodes = [];
                    $scope.servers = $scope.mServers.split(/;~;|\|/);
                    var serverArr = $scope.mServers.split(/;~;|\|/);
                    var len = serverArr.length;
                    for(var i=0;i < len;i++){
                        var node = {};
                        node.IP = serverArr[i].split(/:/)[0];
                        node.Port = serverArr[i].split(/:/)[1];
                        node.status = "在线";
                        $scope.nodes.push(node);
                    }
                }
                iniTable("node");
            }).error(function(){
            });
            $http.get(window.contextPath + '/config/category/findbycluster',{
                params: {
                    cluster : $scope.mCacheKey,
                }
            }).success(function(response){
                $scope.categoryList = response;
                initCategory();
            }).error(function(){

            });
        };

        $scope.initValue = function (data) {
            $scope.configuration = data;
            $scope.mCacheKey = data.cacheKey;
            $scope.mClientClazz = data.clientClazz;
            $scope.mServers = data.servers;
            $scope.mSwimLane = data.swimlane;
            $scope.mTranscoderClazz = data.transcoderClazz;
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
                        "bPaginate": true, //翻页功能
                        "bFilter": true, //过滤功能
                        "bLengthChange": true,
                        "bInfo": false,//页脚信息
                        "bStateSave": false,
                        "aaSorting": [],
                        "aoColumns": [
                            {"bSortable": false},{"bSortable": false}, {"bSortable": false},
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                    var obj = document.getElementById("node_length");
                    obj.innerHTML = '<div ng-switch="scaleType">'+
                            '<label class="col-sm-3 no-padding-right">扩容</label>'+
                            '</div>';

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
                        "bInfo": true,//页脚信息
                        "iDisplayLength": 50,
                        "aaSorting": [],
                        "aoColumns": [
                            {"bSortable": false},{"bSortable": false}, {"bSortable": false}, {"bSortable": false}, null,
                            {"bSortable": false}, {"bSortable": false}
                        ],
                    });
                }, 0);
            });
        };
    } ]);