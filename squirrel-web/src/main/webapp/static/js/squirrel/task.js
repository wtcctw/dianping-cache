module.controller('TaskController', [ '$scope', '$http','$document', function($scope, $http) {
    Date.prototype.Format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }


    $(document).ready(function () {
        var quorumTable = $('#taskTable').DataTable( {
            responsive: true,
            "ajax": "/task/list",
            "columns": [
                { "data": "id" },
                { "data": "typeDescription" },
                { "data": "description" },
                { "data": "stat" },
                { "data": "commitTime" },
                { "data": "commiter" }
            ],
            "columnDefs": [ {
                "targets": 3,
                "render": function ( data, type, full, meta ) {
                    var data = full;
                    var percent = Math.floor((data.stat / (data.statMax - data.statMin)) * 10000);
                    console.log(data.stat);
                    console.log(data.statMax);
                    if(isNaN(percent) || percent > 100)
                        percent = 0;
                    var percentPieChart = '<div class="infobox infobox-green infobox-small infobox-dark"><div class="infobox-progress"> <div class="easy-pie-chart percentage" data-percent="' +
                        percent +
                        '" data-size="39" style="height: 39px; width: 39px; line-height: 38px;"> <span class="percent"></span>' +
                        percent +
                        '% <canvas height="39" width="39"></canvas></div> </div> <div class="infobox-data"> <div class="infobox-content">Task</div> <div class="infobox-content">Completion</div> </div> </div>'
                    return percentPieChart;
                }
            }
                ,
                {
                    "targets": 4,
                    "render": function ( data, type, full, meta ) {
                        var commitTime = full.commitTime;
                        return new Date(commitTime).Format("yyyy-MM-dd hh:mm:ss");
                    }
                }
                ,
                {
                    "targets": 6,
                    "render": function ( data, type, full, meta ) {
                        var data = full;
                        var percent = Math.floor((data.stat / (data.statMax - data.statMin)) * 10000);
                        if(isNaN(percent) || percent > 100)
                            percent = 0;
                        var id = data.id;
//                        if(percent >= 0 && percent <= 100)
                        return "<button class=\"btn btn-sm btn-primary\" onclick='cancelTask(" +
                            id + ")'>Cancel</button>";
                    }
                }
            ],
            "drawCallback" : function(setting) {
                drowPercent();
            }
        });
    });


} ]);