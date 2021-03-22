"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellerOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, id) {
    const sellerModel = this;

    sellerModel.peers = peers;
    sellerModel.id = id;
    sellerModel.form = {};
    sellerModel.formError = false;

    sellerModel.transfer = () => {
        if (invalidFormInput()) {
            sellerModel.formError = true;
        } else {
            sellerModel.formError = false;

            const id = sellerModel.id;
            const driver = sellerModel.form.driver;
            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `notice-order?id=${id}&driver=${driver}`;

            $http.get(issueIOUEndpoint).then(
                (result) => sellerModel.displayMessage(result),
                (result) => sellerModel.displayMessage(result)
            );
        }
    };

    sellerModel.displayMessage = (message) => {
        const sellerMsgModel = $uibModal.open({
            templateUrl: 'sellerOrderModel.html',
            controller: 'sellerOrderCtrl',
            controllerAs: 'SellerMsgModal',
            resolve: { message: () => message }
        });

        sellerMsgModel.result.then(() => {}, () => {});
    };

    sellerModel.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return sellerModel.form.driver === undefined;
    }
});

angular.module('demoAppModule').controller('sellerOrderCtrl', function ($uibModalInstance, message) {
    const sellerMsgModel = this;
    sellerMsgModel.message = message.data;
});