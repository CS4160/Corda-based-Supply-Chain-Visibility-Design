"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('driverOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, id) {
    const driverOrderModel = this;

    driverOrderModel.peers = peers;
    driverOrderModel.id = id;
    driverOrderModel.form = {};
    driverOrderModel.formError = false;

    driverOrderModel.transfer = () => {
        if (invalidFormInput()) {
            driverOrderModel.formError = true;
        } else {
            driverOrderModel.formError = false;

            const id = driverOrderModel.id;
            const expectedtime = driverOrderModel.form.expectedtime;
            const arrivaltime = driverOrderModel.form.arrivaltime;
            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `driver?id=${id}&expectedtime=${expectedtime}&&arrivaltime=${arrivaltime}`;

            $http.get(issueIOUEndpoint).then(
                (result) => driverOrderModel.displayMessage(result),
                (result) => driverOrderModel.displayMessage(result)
            );
        }
    };

    driverOrderModel.displayMessage = (message) => {
        const driverMsgModel = $uibModal.open({
            templateUrl: 'driverOrderModel.html',
            controller: 'driverOrderMsgModelCtrl',
            controllerAs: 'driverOrderMsgModel',
            resolve: { message: () => message }
        });

        driverOrderMsgModel.result.then(() => {}, () => {});
    };

    driverOrderModel.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return isNaN(driverModel.form.expectedtime) || isNaN(driverModel.form.arrivaltime) ;
    }
});

angular.module('demoAppModule').controller('driverOrderMsgModelCtrl', function ($uibModalInstance, message) {
    const driverOrderMsgModel = this;
    driverOrderMsgModel.message = message.data;
});