"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellerOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const sellerOrderModel = this;

    sellerOrderModel.peers = peers;
    sellerOrderModel.orders = orders;
    sellerOrderModel.form = {};
    sellerOrderModel.formError = false;
    let id = [];

    if (sellerOrderModel.orders !== undefined) {
        for(var j =0; j<sellerOrderModel.orders.length;j++){
            id.push(sellerOrderModel.orders[j].linearId.id)
        }
    }

    sellerOrderModel.transfer = () => {
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
                    `notice-order?id=${id[j]}&driver=${driver}`;
                $http.get(issueIOUEndpoint).then(
                    (result) => sellerOrderModel.displayMessage(result),
                    (result) => sellerOrderModel.displayMessage(result)
                );
            }
        }
    };

    sellerOrderModel.displayMessage = (message) => {
        const sellerMsgModel = $uibModal.open({
            templateUrl: 'sellerOrderModel.html',
            controller: 'sellerOrderCtrl',
            controllerAs: 'SellerMsgModal',
            resolve: { message: () => message }
        });

        sellerMsgModel.result.then(() => {}, () => {});
    };

    sellerOrderModel.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return sellerOrderModel.form.driver === undefined;
    }
});

angular.module('demoAppModule').controller('sellerOrderCtrl', function ($uibModalInstance, message) {
    const sellerMsgModel = this;
    sellerMsgModel.message = message.data;
});