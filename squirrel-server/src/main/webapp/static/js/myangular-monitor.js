function renderGraph(url, divName, http) {
	http({
		method : 'POST',
		url : window.contextpath + url
	})
			.success(
					function(data, status, headers, config) {

						var parent = $('#' + divName);
						var count = 0;
						data
								.forEach(function(item) {
									count++;
									var childdiv = $('<div></div>');
									childdiv.appendTo(parent);
									$(function() {
										childdiv
												.highcharts({
													title : {
														text : item.title,
														x : 0
													// center
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
														}
													},
													series : item.series
												});
									});
								});
					}).error(function(data, status, headers, config) {

				alert("响应错误" + data);
				// app.appError("响应错误", data);
			});
}

module.controller('ProducerServerQpsController', function($scope, $http) {

	$scope.getProducerServerQps = function() {
		renderGraph("/console/monitor/producerserver/qps/get", "container",
				$http);
	};

});

module.controller('ConsumerServerQpsController', function($scope, $http) {

	$scope.getConsumerServerQps = function() {
		renderGraph("/console/monitor/consumerserver/qps/get", "container",
				$http);
	};
});

module.controller('ConsumerQpsController', function($scope, $http) {
	$http({
		method : 'POST',
		url : window.contextpath + '/console/monitor/topiclist/get'
	}).success(
			function(topicList, status, headers, config) {

				$("#consumer-div").typeahead(
						{
							source : topicList,
							updater : function(c) {
								window.location = window.contextpath
										+ "/console/monitor/consumer/" + c
										+ "/qps";
								return c;
							}
						})
			}).error(function(data, status, headers, config) {
		app.appError("响应错误", data);
	});

	$scope.getConsumerQps = function(topicName) {
		renderGraph("/console/monitor/consumer/" + topicName + "/qps/get",
				"container", $http);
	};
});

module.controller('ConsumerAccuController', function($scope, $http) {

	$http({
		method : 'POST',
		url : window.contextpath + '/console/monitor/topiclist/get'
	}).success(
			function(topicList, status, headers, config) {

				$("#consumer-div").typeahead(
						{
							source : topicList,
							updater : function(c) {
								window.location = window.contextpath
										+ "/console/monitor/consumer/" + c
										+ "/accu";
								return c;
							}
						})
			}).error(function(data, status, headers, config) {
		app.appError("响应错误", data);
	});

	$scope.getAccu = function(topicName) {
		renderGraph("/console/monitor/consumer/" + topicName + "/accu/get",
				"container", $http);
	};
});

module.controller('ConsumerDelayController', function($scope, $http) {

	// search
	$http({
		method : 'POST',
		url : window.contextpath + '/console/monitor/topiclist/get'
	}).success(
			function(topicList, status, headers, config) {

				$("#consumer-div").typeahead(
						{
							source : topicList,
							updater : function(c) {
								window.location = window.contextpath
										+ "/console/monitor/consumer/" + c
										+ "/delay";
								return c;
							}
						})
			}).error(function(data, status, headers, config) {
		app.appError("响应错误", data);
	});

	$scope.getDelay = function(topicName) {
		renderGraph('/console/monitor/consumer/' + topicName + '/delay/get',
				"container", $http);
	};
});

module.controller('ConsumerDashboardController', function($scope, $http) {
	$("#sidebar").addClass("responsive menu-min");

	$scope.starttime = "";
	$scope.stoptime = "";
	$scope.currentMin = -1;  //当前分钟，第一次时设置
	$scope.hour = 0;
	$scope.firstaccess = true;
	$scope.currentRed = -1;
	$scope.hourchange = false;
	$scope.whatClassIsIt = function(index) {
		if (index == $scope.currentRed)
			return "red-num"
		else if (index > $scope.currentMin)
			return "not-active";
		else
			return "";
	}
	
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
	
	$scope.getEntry = function(delayEntry){
		defaultSize = 12;
		var size = delayEntry.size > defaultSize ? defaultSize : delayEntry.size;
		var emp = delayEntry.Heap;
		var entrys = delayEntry.Heap.slice(0, size)
		return entrys;
	}
	
	$scope.getDashboardDelay = function(index) {
		$scope.minuteEntrys = [];
		var date = new Date();
		if(index != -1){
			date.setMinutes(index,59,999);
		}else{
			date.setSeconds(59,999);
		}
		date.setHours(date.getHours() + 8);
		$http({
			method : 'GET',
			params : {date: date},
			url : window.contextPath + '/console/monitor/dashboard/delay/minute'
		}).success(function(data, status, headers, config) {
			$scope.minuteEntrys = data;
			var len = data.length;
			for(var i = 0; i < len; ++i){
				var time = $scope.minuteEntrys[i].time;
				var date = new Date(time);
				var hour = date.getHours(); 
				var min = date.getMinutes();
				var minString = min.toString();
				minString = (minString.length == 1) ? "0"+minString : minString;
				var timeString = hour.toString() + ":" + minString;
				$scope.minuteEntrys[i].time = timeString;
			}
			if($scope.firstaccess){
				$scope.currentMin = Number($scope.minuteEntrys[0].time
						.split(":")[1]);
				$scope.firstaccess = false;
			}
			$scope.currentRed = index == -1 ? $scope.currentMin : Number($scope.minuteEntrys[0].time
					.split(":")[1]);
		});
		
	};
	
	$scope.pages = [ "00", "01", "02", "03", "04", "05", "06", "07", "08",
			"09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
			"31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41",
			"42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52",
			"53", "54", "55", "56", "57", "58", "59" ];


	$scope.clicked = [];
	$scope.items = [];
	$scope.table = function(parentindex, index) {
		$scope.clicked = $scope.minuteEntrys[parentindex].delayEntry.Heap[index];
		
		$scope.items = [{ "senddelay" : $scope.minuteEntrys[parentindex].delayEntry.Heap[index].senddelay,
		                  "ackdelay"  : $scope.minuteEntrys[parentindex].delayEntry.Heap[index].ackdelay, 
		                  "accu"      : $scope.minuteEntrys[parentindex].delayEntry.Heap[index].accu,
		                  "topic"     : $scope.minuteEntrys[parentindex].delayEntry.Heap[index].topic} ];
	}
	
});
