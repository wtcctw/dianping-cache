module.controller('TaskController', [ '$scope', '$http','$document', function($scope, $http) {

    $(document).ready(function () {
        var quorumTable = $('#taskTable').DataTable( {
            responsive: true,
            "ajax": "/task/list",
            "columns": [
                { "data": "id" },
                { "data": "typeDescription" },
                { "data": "description" },
                { "data": "stat" },
                { "data": "startTime" },
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