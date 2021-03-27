"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellerOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const sellerOrderModel = this;

    sellerOrderModel.peers = peers;
    sellerOrderModel.orders = orders;
    sellerOrderModel.form= {};
    sellerOrderModel.formError = false;
    let id = [];

    if (sellerOrderModel.orders !== undefined) {
        for(var j =0; j<sellerOrderModel.orders.length;j++){
            id.push(sellerOrderModel.orders[j].linearId.id)
        }
    }

    sellerOrderModel.create = () => {
        if (invalidFormInput()) {
            sellerOrderModel.formError = true;
        } else {
            sellerOrderModel.formError = false;

            const id = sellerOrderModel.id;
            const driver = sellerOrderModel.form.driver;
            $uibModalInstance.close();
            for (var j=0; j<id.length;j++){
                const issueIOUEndpoint =
                    apiBaseURL +
                    `notice-order?id=${id[j]}&driver=${driver[j]}`;
                $http.put(issueIOUEndpoint).then(
                    (result) => sellerOrderModel.displayMessage(result),
                    (result) => sellerOrderModel.displayMessage(result)
                );
            }
        }
    };

    sellerOrderModel.displayMessage = (message) => {
        const sellerOrderMsgModel = $uibModal.open({
            templateUrl: 'sellerOrderMsgModel.html',
            controller: 'sellerOrderMsgModelCtrl',
            controllerAs: 'sellerOrderMsgModel',
            resolve: { message: () => message }
        });

        sellerOrderMsgModel.result.then(() => {}, () => {});
    };

    sellerOrderModel.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        var a = (sellerOrderModel.form.driver === undefined)
        return a;
    }
});

angular.module('demoAppModule').controller('sellerOrderMsgModelCtrl', function ($uibModalInstance, message) {
    const sellerOrderMsgModel = this;
    sellerOrderMsgModel.message = message.data;
});