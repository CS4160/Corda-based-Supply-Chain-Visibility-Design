"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellerOrderModalCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orderState, transState, $scope) {
    const sellerOrderModal = this;

    sellerOrderModal.peers = [];
    sellerOrderModal.form= {};
    sellerOrderModal.formError = false;


    sellerOrderModal.orderState = orderState;
    sellerOrderModal.transState = transState;

    for (var j=0; j<peers.length; j++){
        var start = peers[j].indexOf("OU=") + 3;
        var end = peers[j].length;

        if (peers[j].substr(start,end) === 'Trucker'){
            sellerOrderModal.peers.push(peers[j])
        }
    }



    sellerOrderModal.create = (index) => {
        if (invalidFormInput()) {
            sellerOrderModal.formError = true;
        } else {
            sellerOrderModal.formError = false;

            // const id = sellerOrderModal.id;
            const id = sellerOrderModal.orderState[index].linearId.id;
            const driver = sellerOrderModal.form.driver[index];
            $uibModalInstance.close();
            const issueIOUEndpoint =
                apiBaseURL +
                `notice-order?id=${id}&driver=${driver}`;
            $http.put(issueIOUEndpoint).then(
                (result) => sellerOrderModal.displayMessage(result),
                (result) => sellerOrderModal.displayMessage(result));
        }
    };


    sellerOrderModal.displayMessage = (message) => {
        const sellerOrderMsgModal = $uibModal.open({
            templateUrl: 'sellerOrderMsgModal.html',
            controller: 'sellerOrderMsgModalCtrl',
            controllerAs: 'sellerOrderMsgModal',
            resolve: { message: () => message }
        });

        sellerOrderMsgModal.result.then(() => {}, () => {});
    };

    sellerOrderModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        var a = (sellerOrderModal.form.driver === undefined)
        return a;
    }
});

angular.module('demoAppModule').controller('sellerOrderMsgModalCtrl', function ($uibModalInstance, message) {
    const sellerOrderMsgModal = this;
    sellerOrderMsgModal.message = message.data;
});