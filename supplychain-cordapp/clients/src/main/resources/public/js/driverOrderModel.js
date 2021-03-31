"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('driverOrderModelCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const driverOrderModel = this;

    driverOrderModel.peers = peers;
    driverOrderModel.form = {};
    driverOrderModel.formError = false;
    driverOrderModel.orders = orders




    Date.prototype.format = function(format)
    {
        var o = {
            "M+" : this.getMonth()+1, //month
            "d+" : this.getDate(),    //day
            "h+" : this.getHours(),   //hour
            "m+" : this.getMinutes(), //minute
            "s+" : this.getSeconds(), //second
            "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
            "S" : this.getMilliseconds() //millisecond
        }
        if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
            (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)if(new RegExp("("+ k +")").test(format))
            format = format.replace(RegExp.$1,
                RegExp.$1.length==1 ? o[k] :
                    ("00"+ o[k]).substr((""+ o[k]).length));
        return format;
    }

    // var d = new Date();
    // var useDate = d.format('yyyy-MM-dd');
    // var useDate2 = d.format('yyyy-MM-dd hh:mm:ss');
    driverOrderModel.accept = (index) => {
        if (invalidFormInput()) {
            driverOrderModel.formError = true;
        } else {
            driverOrderModel.formError = false;
            let id = driverOrderModel.orders[index].linearId.id
            // let id = [];
            //
            //
            // if (driverOrderModel.orders !== undefined) {
            //     for(var j =0; j<driverOrderModel.orders.length;j++){
            //         id.push(driverOrderModel.orders[j].linearId.id)
            //     }
            // }

            var expectedtime = new Date(driverOrderModel.form.expectedtime[index]);
            expectedtime = expectedtime.format('yyyy-MM-dd hh:mm:ss')
            // var arrivaltime = new Date(driverOrderModel.form.arrivaltime[index]);
            // arrivaltime = arrivaltime.format('yyyy-MM-dd hh:mm:ss')
            $uibModalInstance.close();
            // for (var j=0; j<id.length;j++){
            //     if(expectedtime !== undefined){
            //             const issueIOUEndpoint =
            //                 apiBaseURL +
            //                 `driver-add?id=${id[j]}&expectedtime=${expectedtime[j]}`;
            //
            //                 $http.put(issueIOUEndpoint).then(
            //                  (result) => driverOrderModel.displayMessage(result),
            //                  (result) => driverOrderModel.displayMessage(result)
            // );
            //  }
            //
            //     // if(arrivaltime !==undefined){
            //     //       const issueDriverEndpoint=
            //     //       apiBaseURL + `driver-arrival?id=${id[j]}&arrivaltime=${arrivaltime[j]}`;
            //     //
            //     //       $http.put(issueDriverEndpoint).then(
            //     //       (result) => driverOrderModel.displayMessage(result),
            //     //       (result) => driverOrderModel.displayMessage(result)
            //     //      );
            //     // }
            //
            // }
            const issueIOUEndpoint =
                apiBaseURL +
                `driver-add?id=${id}&expectedtime=${expectedtime}`;

            $http.put(issueIOUEndpoint).then(
                (result) => driverOrderModel.displayMessage(result),
                (result) => driverOrderModel.displayMessage(result));

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