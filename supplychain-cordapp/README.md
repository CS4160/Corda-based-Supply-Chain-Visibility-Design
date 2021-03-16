    command for Alice:
    flow start OrderFlow$Initiator isBuyer: true, good: 'Macbook', location: 'Hague', counterparty: 'O=Apple,L=Rotterdam,C=NL'
    
    Command for Bob:
    flow start OrderFlow$Initiator isBuyer: true, good: 'Airpods', location: 'Delft', counterparty: 'O=Apple,L=Rotterdam,C=NL'
    
    command for Apple:
    flow start NoticeFlow$Initiator orderId: c4ecffc3-6e8a-40b3-9752-de2b2f5c9b3c, deliver: 'O=Tom,L=Rotterdam,C=NL'
    flow start NoticeFlow$Initiator orderId: [Bob's OrderId], deliver: 'O=Tom,L=Rotterdam,C=NL'
    
    command for Tom
    flow start AddItineraryFlow$Initiator OrderId: c4ecffc3-6e8a-40b3-9752-de2b2f5c9b3c, expectedTime: '2017-06-08T10:41:06.261+0800'
    flow start AddItineraryFlow$Initiator OrderId: [Bob's OrderId], expectedTime: '2017-06-08T10:52:06.261+0800'
    
    command for Apple:
    flow start UpdateFlow$Initiator orderId: c4ecffc3-6e8a-40b3-9752-de2b2f5c9b3c
    flow start UpdateFlow$Initiator orderId: [Bob's OrderId]
    
    command for Tom:
    flow start ArrivalFlow$Initiator orderId: c4ecffc3-6e8a-40b3-9752-de2b2f5c9b3c, actualTime: '2017-06-08T10:45:06.261+0800'
    flow start ArrivalFlow$Initiator orderId: [Bob's OrderId], actualTime: '2017-06-08T10:55:06.261+0800'
    
    command for Apple:
    flow start CompleteFlow$Initiator orderId: [Alice's OrderId]
    flow start CompleteFlow$Initiator orderId: [Bob's OrderId]
    
    run vaultQuery contractStateType: net.corda.samples.supplychain.states.TradeState

