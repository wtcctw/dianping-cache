//var module = angular.module('SwallowModule', ['ngResource','isteven-multi-select']);
//Your app's root module...
var module = angular.module('CacheModule', ['ngResource', 'ngDialog'], function($httpProvider) {
  // Use x-www-form-urlencoded Content-Type
  $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';

  /**
   * The workhorse; converts an object to x-www-form-urlencoded serialization.
   * @param {Object} obj
   * @return {String}
   */ 
  var param = function(obj) {
    var query = '', name, value, fullSubName, subName, subValue, innerObj, i;
      
    for(name in obj) {
      value = obj[name];
        
      if(value instanceof Array) {
        for(i=0; i<value.length; ++i) {
          subValue = value[i];
          fullSubName = name + '[' + i + ']';
          innerObj = {};
          innerObj[fullSubName] = subValue;
          query += param(innerObj) + '&';
        }
      }
      else if(value instanceof Object) {
        for(subName in value) {
          subValue = value[subName];
          fullSubName = name + '[' + subName + ']';
          innerObj = {};
          innerObj[fullSubName] = subValue;
          query += param(innerObj) + '&';
        }
      }
      else if(value !== undefined && value !== null)
        query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
    }
      
    return query.length ? query.substr(0, query.length - 1) : query;
  };

  // Override $http service's default transformRequest
  $httpProvider.defaults.transformRequest = [function(data) {
    return angular.isObject(data) && String(data) !== '[object File]' ? param(data) : data;
  }];
});

module.config(function($locationProvider, $resourceProvider) {
	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(true);
});

module.filter('strreplace', function() {
    return function(input) {
        return input.replace(/\\/g, "")
    };
});
module.filter('getLastDotStr',function(){
	return function(input){
		if(input==null){
			return "";
		}
		return input.substring(input.lastIndexOf("\.")+1);
	};
});
module.filter('getClient',function(){
	return function(input){
		if(input==null){
			return "";
		}
		return input.substring(input.lastIndexOf("\.")+1,input.indexOf("Client"));
	};
});

module.filter('ipfilter',function(){
	return function(input){
		var i =input.indexOf("proxy");
		if(i == -1){
			return input;
		}
		return null;
	};
});
module.filter('split',function(){
	return function(input){
		if(input==null){
			return "";
		}
		return input.split(/;~;|\|/);
	};
});
module.config(function($locationProvider, $resourceProvider) {
	// configure html5 to get links working on jsfiddle
	$locationProvider.html5Mode(true);
});

	
module.directive('onFinishRenderFilters', function ($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function() {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    };
});




module.filter('reverse', function() {
  return function(items) {
    return items.slice().reverse();
  };
});

module.filter('notblank', function() {
	  return function(items) {
		  if(items != null){
			  for(var i = 0; i < items.length; ++i){
				  if(typeof(items[i].name) == "undefined"){
					  items.splice(i, 1);
				  }
			  }
		  }
		  return items;
	  };
	});



