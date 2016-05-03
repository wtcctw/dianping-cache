module.controller('PhymachineController', ['$scope', '$http', '$document', function ($scope, $http) {
    $scope.load = function() {
        $http.get(window.contextPath + '/config/getMachineInfo', { params: {},}
        ).success(function(data) {
            $scope.data = data;
        });
    };

    $scope.load();
}]);