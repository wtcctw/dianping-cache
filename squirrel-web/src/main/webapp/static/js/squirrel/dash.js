module.controller('IndexController', [ '$scope', '$http','$document', function($scope, $http) {


    $scope.indexData;

    $scope.init = function(){
        $http.get("/dashdata",{params : {}
        }).success(function(data){
            $scope.indexData =data;
        });
    }

    $scope.init();

} ]);