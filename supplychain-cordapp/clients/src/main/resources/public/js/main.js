"use strict";

// Define your backend here.
angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal,$scope) {
    const demoApp = this;
    const apiBaseURL = "/api/iou/";

    // Retrieves the identity of this and other nodes.
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);
    $http.get(apiBaseURL + "orders").then((response) =>demoApp.orders = Object.keys(response.data).map((key) => response.data[key].state.data));




    /** Displays the Buyer creation modal. */
    demoApp.openCreateOrderModel = () => {
        const createOrderModel = $uibModal.open({
            templateUrl: 'createOrderModel.html',
            controller: 'createOrderModelCtrl',
            controllerAs: 'createOrderModel',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        // Ignores the modal result events.
        createOrderModel.result.then(() => {}, () => {});
    };


    /** Displays the  issuance modal.
    **/
    demoApp.openSellerOrderModel = () => {
        const sellerOrderModel = $uibModal.open({
            templateUrl: 'sellerOrderModel.html',
            controller: 'sellerOrderModelCtrl',
            controllerAs: 'sellerOrderModel',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers,
                orders: () => demoApp.orders
                // id: () => id
          }
        });

        sellerOrderModel.result.then(() => {}, () => {});
    };

    /** Displays the IOU transfer modal. **/
    demoApp.openDriverOrderModel = () => {
        const driverOrderModel = $uibModal.open({
            templateUrl: 'driverOrderModel.html',
            controller: 'driverOrderModelCtrl',
            controllerAs: 'driverOrderModel',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers,
                orders:() => demoApp.orders
                //id: () => id
            }
        });

        driverOrderModel.result.then(() => {}, () => {});
    };


    demoApp.refresh = () => {
        $http.get(apiBaseURL + "orders").then((response) => demoApp.orders =
            Object.keys(response.data).map((key) => response.data[key].state.data));

    }

    demoApp.refresh();
});

// Causes the webapp to ignore unhandled modal dismissals.
angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);