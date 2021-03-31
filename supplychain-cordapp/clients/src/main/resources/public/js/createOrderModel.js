"use strict";

angular.module('demoAppModule').controller('createOrderModelCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const createOrderModel = this;

    createOrderModel.peers = peers;
    createOrderModel.form = {};
    createOrderModel.formError = false;
    createOrderModel.orders = orders;



    /** Validate and create an IOU. */
    createOrderModel.create = () => {
        if (invalidFormInput()) {
            createOrderModel.formError = true;
        } else {
            createOrderModel.formError = false;

            const location = createOrderModel.form.location;
            const good = createOrderModel.form.good;
            const company = createOrderModel.form.counterparty;

            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const issueIOUEndpoint =
                apiBaseURL +
                `create-order?location=${location}&good=${good}&company=${company}`;

            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.put(issueIOUEndpoint).then(
                (result) => createOrderModel.displayMessage(result),
                (result) => createOrderModel.displayMessage(result)
            );
        }
    };

    /** Displays the success/failure response from attempting to create an IOU. */
    createOrderModel.displayMessage = (message) => {
        const createOrderMsgModel = $uibModal.open({
            templateUrl: 'createOrderMsgModel.html',
            controller: 'createOrderMsgModelCtrl',
            controllerAs: 'createOrderMsgModel',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        createOrderMsgModel.result.then(() => {}, () => {});
    };

    /** Closes the IOU creation modal. */
    createOrderModel.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function invalidFormInput() {
        var a = (createOrderModel.form.location === undefined)
        var b = (createOrderModel.form.good === undefined)
        var c = (createOrderModel.form.counterparty === undefined)

        // return isNaN(createOrderModel.form.location) || isNaN(createOrderModel.form.good) || (createOrderModel.form.counterparty === undefined);
        return a || b || c
    }

});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('createOrderMsgModelCtrl', function($uibModalInstance, message) {
    const createOrderMsgModel = this;
    createOrderMsgModel.message = message.data;
});