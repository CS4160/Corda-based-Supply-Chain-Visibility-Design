"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellerOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orderState, transState) {
    const sellerOrderModel = this;

    sellerOrderModel.peers = peers;
    sellerOrderModel.form= {};
    sellerOrderModel.formError = false;


    sellerOrderModel.orderState = orderState;
    sellerOrderModel.transState = transState;





    sellerOrderModel.create = (index) => {
        if (invalidFormInput()) {
            sellerOrderModel.formError = true;
        } else {
            sellerOrderModel.formError = false;

            // const id = sellerOrderModel.id;
            const id = sellerOrderModel.orderState[index].linearId.id;
            const driver = sellerOrderModel.form.driver[index];
            $uibModalInstance.close();
            // for (var j=0; j<id.length;j++){
            //     const issueIOUEndpoint =
            //         apiBaseURL +
            //         `notice-order?id=${id[j]}&driver=${driver[j]}`;
            //     $http.put(issueIOUEndpoint).then(
            //         (result) => sellerOrderModel.displayMessage(result),
            //         (result) => sellerOrderModel.displayMessage(result)
            //     );
            // }
            const issueIOUEndpoint =
                apiBaseURL +
                `notice-order?id=${id}&driver=${driver}`;
            $http.put(issueIOUEndpoint).then(
                (result) => sellerOrderModel.displayMessage(result),
                (result) => sellerOrderModel.displayMessage(result));
        }
    };

    // sellerOrderModel.update = () => {
    //     const issueIOUEndpoint =
    //         apiBaseURL +
    //         `update-order?id=${id}`;
    //     $http.put(issueIOUEndpoint).then(
    //         (result) => sellerOrderModel.displayMessage(result),
    //         (result) => sellerOrderModel.displayMessage(result));
    // }

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