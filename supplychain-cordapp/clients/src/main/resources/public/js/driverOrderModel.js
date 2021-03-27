"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('driverOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const driverOrderModel = this;

    driverOrderModel.peers = peers;
    driverOrderModel.orders = orders;
    driverOrderModel.form = {};
    driverOrderModel.formError = false;


    let id = [];

    if (driverOrderModel.orders !== undefined) {
        for(var j =0; j<driverOrderModel.orders.length;j++){
            id.push(driverOrderModel.orders[j].linearId.id)
        }
    }


    driverOrderModel.create = () => {
        if (invalidFormInput()) {
            driverOrderModel.formError = true;
        } else {
            driverOrderModel.formError = false;

            const id = driverOrderModel.id;
            const expectedtime = driverOrderModel.form.expectedtime;
            const arrivaltime = driverOrderModel.form.arrivaltime;
            $uibModalInstance.close();
            for (var j=0; j<id.length;j++){
                if(expectedtime !== undefined){
                        const issueIOUEndpoint =
                            apiBaseURL +
                            `driver-add?id=${id[j]}&expectedtime=${expectedtime}`;

                            $http.put(issueIOUEndpoint).then(
                             (result) => driverOrderModel.displayMessage(result),
                             (result) => driverOrderModel.displayMessage(result)
            );
             }

                if(arrivaltime !==undefined){
                      const issueDriverEndpoint=
                      apiBaseURL + `driver-arrival?id=${id[j]}&arrivaltime=${arrivaltime}`;

                      $http.put(issueDriverEndpoint).then(
                      (result) => driverOrderModel.displayMessage(result),
                      (result) => driverOrderModel.displayMessage(result)
                     );
                }

            }


        }
    };

    driverOrderModel.displayMessage = (message) => {
        const driverOrderMsgModel = $uibModal.open({
            templateUrl: 'driverOrderMsgModel.html',
            controller: 'driverOrderMsgModelCtrl',
            controllerAs: 'driverOrderMsgModel',
            resolve: { message: () => message }
        });

        driverOrderMsgModel.result.then(() => {}, () => {});
    };

    driverOrderModel.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return (driverOrderModel.form.expectedtime === undefined) && (driverOrderModel.form.arrivaltime === undefined) ;
    }
});

angular.module('demoAppModule').controller('driverOrderMsgModelCtrl', function ($uibModalInstance, message) {
    const driverOrderMsgModel = this;
    driverOrderMsgModel.message = message.data;
});