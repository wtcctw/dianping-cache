module.controller('MemcachedDashBoardController', [ '$scope', '$http','$document',
    '$timeout', function($scope, $http) {
        $scope.data = [];
        $scope.initDashBoard = function () {
            $http.get(window.contextPath + '/memcached/dashboard/data', {
                params: {},
            }).success(function (response) {
                response.datas.forEach(function (item) {
                    $scope.data.push(item);
                });
                var num1 = response.totalNum;
                var num2 = response.dangerNum;
                var width2 = num2 * 100 / num1;
                var width1 = 100 - width2;
                init(num1,num2,width1,width2);
            }).error(function () {
            });
        }
        $scope.initDashBoard();
        var init = function (num1,num2,width1,width2) {
            $(document).ready(function () {
                setTimeout(function () {
                    $('#memcachedTable').dataTable({
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
                            {"bSortable": false}, {"bSortable": false},{"bSortable": false}
                        ],
                    });
                    var obj = document.getElementById("memcachedTable_length");
                    obj.innerHTML='<div style="margin-top: 5px"> <div class="col-sm-2">总览</div> '+
                        '</div><div class="col-sm-7" style="margin-top: 3px"><div class="progress progress-small"> <div class="progress-bar progress-bar-success" style="width:'+width1+'%;background-color:#68ff5c;position: relative;"></div>' +
                        '<div class="progress-bar progress-bar-danger" style="width: '+width2+'%;background-color:#ff6a6a;position: relative;"></div></div></div>' +
                        '<div class="col-sm-3">共'+num1+'个,告警'+num2+'个</div> </div>';
                }, 0);
            });
        };
    } ]);