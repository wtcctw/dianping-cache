function renderGraph(url, divName, endTime, http) {
	http({
		method : 'GET',
		url : window.contextpath + url,
		params : {
			"endTime" : endTime
		}
	})
			.success(
					function(data, status, headers, config) {
						var children = document.getElementById(divName).childNodes;
						if (children != null) {
							var len = children.length;
							for (i = 0; i < len; i++) {
								document.getElementById(divName).removeChild(
										children[children.length - 1]);
							}
						}

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
													chart : {
														type : 'spline'
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
								});
					}).error(function(data, status, headers, config) {
				alert("响应错误" + data);
			});
}

function renderGraph2(url, address, divName,endTime, http) {
	http({
		method : 'GET',
		url : window.contextpath + url,
		params : {
			"address" : address,
			"endTime" : endTime
		}
	})
			.success(
					function(data, status, headers, config) {
						var children = document.getElementById(divName).childNodes;
						if (children != null) {
							var len = children.length;
							for (i = 0; i < len; i++) {
								document.getElementById(divName).removeChild(
										children[children.length - 1]);
							}
						}

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
													chart : {
														type : 'spline'
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
								});
					}).error(function(data, status, headers, config) {
				alert("响应错误" + data);
			});
}
module.controller('ClusterQpsController', function($scope, $http) {

	$scope.key = "";
	$scope.endTime = new Date().getTime();

	$scope.getProducerServerQps = function() {
		$scope.key = window.localStorage.cacheKey;
		renderGraph("/monitor/cluster/" + $scope.key, "container",
				$scope.endTime, $http);
	};

	$scope.setTime = function(val) {
		$scope.endTime = $scope.endTime + val * 3600 * 1000;
		if ($scope.endTime > new Date().getTime())
			$scope.endTime = new Date().getTime();
		renderGraph("/monitor/cluster/" + $scope.key, "container",
				$scope.endTime, $http);
	}

	$scope.setNow = function() {
		$scope.endTime = new Date().getTime();
		renderGraph("/monitor/cluster/" + $scope.key, "container",
				$scope.endTime, $http);
	}

	$scope.getProducerServerQps();

});

module.controller('RedisServerController', [ '$scope', '$http', '$timeout',
		function($scope, $http, $timeout) {

			$scope.address = "";
			$scope.infodata = "";
			
			
			$scope.endTime = new Date().getTime();

			$scope.getRedisHistoryData = function() {
				$scope.address = window.localStorage.address;
				renderGraph2("/redis/historydata",$scope.address, "container",
						$scope.endTime, $http);
			};
			
			$scope.refresh = function() {
				$scope.address = window.localStorage.address;
				$http.get(window.contextPath + '/redis/serverinfodata', {
					params : {
						"address" : $scope.address
					},
				}).success(function(response) {
					$scope.infodata = response;
					$timeout(function() {
						$scope.refresh();
					}, 3000);
				}).error(function(response) {
				});
			};
			$scope.refresh();

			$scope.setTime = function(val) {
				$scope.endTime = $scope.endTime + val * 3600 * 1000;
				if ($scope.endTime > new Date().getTime())
					$scope.endTime = new Date().getTime();
				renderGraph2("/redis/historydata",$scope.address, "container",
						$scope.endTime, $http);
			}

			$scope.setNow = function() {
				$scope.endTime = new Date().getTime();
				renderGraph2("/redis/historydata",$scope.address, "container",
						$scope.endTime, $http);
			}
			$scope.getRedisHistoryData();

		} ]);