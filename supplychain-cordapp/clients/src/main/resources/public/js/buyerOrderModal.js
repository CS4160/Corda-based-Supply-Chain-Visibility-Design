"use strict";

angular.module('demoAppModule').controller('buyerOrderModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const buyerOrderModal = this;

    buyerOrderModal.peers = [];
    buyerOrderModal.form = {};
    buyerOrderModal.formError = false;
    buyerOrderModal.orders = orders;


    for (var j=0; j<peers.length; j++){
        var start = peers[j].indexOf("OU=") + 3;
        var end = peers[j].length;
        var identity = peers[j].substr(start,end);
        if ( identity === 'Seller'){
            buyerOrderModal.peers.push(peers[j]);
        }
    }


    /** Validate and create an IOU. */
    buyerOrderModal.create = () => {
        if (invalidFormInput()) {
            buyerOrderModal.formError = true;
        } else {
            buyerOrderModal.formError = false;

            const location = buyerOrderModal.form.location;
            const good = buyerOrderModal.form.good;
            const company = buyerOrderModal.form.counterparty;

            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const issueIOUEndpoint =
                apiBaseURL +
                `create-order?location=${location}&good=${good}&company=${company}`;

            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.put(issueIOUEndpoint).then(
                (result) => buyerOrderModal.displayMessage(result),
                (result) => buyerOrderModal.displayMessage(result)
            );
        }
    };

    /** Displays the success/failure response from attempting to create an IOU. */
    buyerOrderModal.displayMessage = (message) => {
        const buyerOrderMsgModal = $uibModal.open({
            templateUrl: 'buyerOrderMsgModal.html',
            controller: 'buyerOrderMsgModalCtrl',
            controllerAs: 'buyerOrderMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        buyerOrderMsgModal.result.then(() => {}, () => {});
    };

    /** Closes the IOU creation modal. */
    buyerOrderModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function invalidFormInput() {
        var a = (buyerOrderModal.form.location === undefined)
        var b = (buyerOrderModal.form.good === undefined)
        var c = (buyerOrderModal.form.counterparty === undefined)

        // return isNaN(buyerOrderModal.form.location) || isNaN(buyerOrderModal.form.good) || (buyerOrderModal.form.counterparty === undefined);
        return a || b || c
    }

});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('buyerOrderMsgModalCtrl', function($uibModalInstance, message) {
    const buyerOrderMsgModal = this;
    buyerOrderMsgModal.message = message.data;
});