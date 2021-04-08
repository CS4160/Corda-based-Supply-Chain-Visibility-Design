"use strict";

// Define your backend here.
angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal,$scope,$interval,$rootScope) {
    const demoApp = this;
    const apiBaseURL = "/api/iou/";

    // Retrieves the identity of this and other nodes.
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => demoApp.peers = response.data.peers);
    $http.get(apiBaseURL + "identity").then((response) => demoApp.identity = response.data.identity);

    $http.get(apiBaseURL + "orders" + "?state_type=orderState").then((response) =>demoApp.orderState = Object.keys(response.data).map((key) => response.data[key].state.data));
    $http.get(apiBaseURL + "orders" + "?state_type=transState").then((response) =>demoApp.transState = Object.keys(response.data).map((key) => response.data[key].state.data));


    Date.prototype.format = function(format)
    {
        var o = {
            "M+" : this.getMonth()+1, //month
            "d+" : this.getDate(),    //day
            "h+" : this.getHours(),   //hour
            "m+" : this.getMinutes(), //minute
            "s+" : this.getSeconds(), //second
            "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
            "S" : this.getMilliseconds() //millisecond
        }
        if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
            (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)if(new RegExp("("+ k +")").test(format))
            format = format.replace(RegExp.$1,
                RegExp.$1.length==1 ? o[k] :
                    ("00"+ o[k]).substr((""+ o[k]).length));
        return format;
    }

    /** Displays the Buyer creation modal. */
    demoApp.openBuyerOrderModal = () => {
        const buyerOrderModal = $uibModal.open({
            templateUrl: 'buyerOrderModal.html',
            controller: 'buyerOrderModalCtrl',
            controllerAs: 'buyerOrderModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => demoApp.peers,
                orders: ()=> demoApp.orderState
            }
        });

        // Ignores the modal result events.
        buyerOrderModal.result.then(() => {}, () => {});
    };


    /** Displays the  issuance modal.
    **/
    demoApp.openSellerOrderModal = () => {
        const sellerOrderModal = $uibModal.open({
            templateUrl: 'sellerOrderModal.html',
            controller: 'sellerOrderModalCtrl',
            controllerAs: 'sellerOrderModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => demoApp.peers,
                orderState: () => demoApp.orderState,
                transState: () => demoApp.transState
                // id: () => id
          }
        });

        sellerOrderModal.result.then(() => {}, () => {});
    };

    /** Displays the IOU transfer modal. **/
    demoApp.openDriverOrderModal = () => {
        const driverOrderModal = $uibModal.open({
            templateUrl: 'driverOrderModal.html',
            controller: 'driverOrderModalCtrl',
            controllerAs: 'driverOrderModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => demoApp.peers,
                orders:() => demoApp.transState
                //id: () => id
            }
        });

        driverOrderModal.result.then(() => {}, () => {});
    };

    demoApp.arrive = (id) => {
        var arrivaltime = new Date();
        arrivaltime = arrivaltime.format('yyyy-MM-dd hh:mm:ss')
        const issueDriverEndpoint=
            apiBaseURL + `driver-arrival?id=${id}&arrivaltime=${arrivaltime}`;
        $http.put(issueDriverEndpoint)
    }



    demoApp.refresh = () => {
        $http.get(apiBaseURL + "orders" + "?state_type=orderState").then((response) =>demoApp.orderState = Object.keys(response.data).map((key) => response.data[key].state.data));
        $http.get(apiBaseURL + "orders" + "?state_type=transState").then((response) =>demoApp.transState = Object.keys(response.data).map((key) => response.data[key].state.data));
    }

    $interval(function () {
        $http.get(apiBaseURL + "orders" + "?state_type=orderState").then((response) =>demoApp.orderState = Object.keys(response.data).map((key) => response.data[key].state.data));
        $http.get(apiBaseURL + "orders" + "?state_type=transState").then((response) =>demoApp.transState = Object.keys(response.data).map((key) => response.data[key].state.data));
        for (var j = 0; j < demoApp.orderState.length; j++) {
            if (demoApp.transState.length!==0 && demoApp.orderState[j].status === "Initial" && demoApp.transState[j].status === "AddItinerary") {
                const issueIOUEndpoint =
                    apiBaseURL +
                    `update-order?id=${demoApp.orderState[j].linearId.id}`;
                $http.put(issueIOUEndpoint)
            }
            else if (demoApp.transState.length!==0 && demoApp.orderState[j].status === "Updated" && demoApp.transState[j].status === "Arrival") {
                const issueIOUEndpoint =
                    apiBaseURL +
                    `complete-order?id=${demoApp.orderState[j].linearId.id}`;
                $http.put(issueIOUEndpoint)
            }

        }
    },5000);
});

// Causes the webapp to ignore unhandled modal dismissals.
angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);