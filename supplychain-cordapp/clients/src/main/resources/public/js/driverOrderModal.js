"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('driverOrderModalCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, orders) {
    const driverOrderModal = this;

    driverOrderModal.peers = peers;
    driverOrderModal.form = {};
    driverOrderModal.formError = false;
    driverOrderModal.orders = orders




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
    driverOrderModal.accept = (index) => {
        if (invalidFormInput()) {
            driverOrderModal.formError = true;
        } else {
            driverOrderModal.formError = false;
            let id = driverOrderModal.orders[index].linearId.id


            var expectedtime = new Date(driverOrderModal.form.expectedtime[index]);
            expectedtime = expectedtime.format('yyyy-MM-dd hh:mm:ss')
            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `driver-add?id=${id}&expectedtime=${expectedtime}`;

            $http.put(issueIOUEndpoint).then(
                (result) => driverOrderModal.displayMessage(result),
                (result) => driverOrderModal.displayMessage(result));

        }
    };

    // driverOrderModal.displayMessage = (message) => {
    //     const driverOrderMsgModal = $uibModal.open({
    //         templateUrl: 'driverOrderMsgModal.html',
    //         controller: 'driverOrderMsgModalCtrl',
    //         controllerAs: 'driverOrderMsgModal',
    //         resolve: { message: () => message }
    //     });
    //
    //     driverOrderMsgModal.result.then(() => {}, () => {});
    // };

    driverOrderModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return (driverOrderModal.form.expectedtime === undefined) && (driverOrderModal.form.arrivaltime === undefined) ;
    }
});

angular.module('demoAppModule').controller('driverOrderMsgModalCtrl', function ($uibModalInstance, message) {
    const driverOrderMsgModal = this;
    driverOrderMsgModal.message = message.data;
});