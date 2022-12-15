var wsUri = "ws://" + document.location.host + "/openSchedule-war/trafficnotificationendpoint";
if (window.websocket === undefined)
    window.websocket = new WebSocket(wsUri);

var notificationTypes = ["unknown", "alarm", "warning", "notification"];

websocket.onopen = function(evt) {
    onOpen(evt);
};
websocket.onerror = function(evt) {
    onError(evt);
};
websocket.onmessage = function(evt) {
    onMessage(evt);
};

websocket.onclose = function() {
    //alert("close");
};


window.onbeforeunload = function() {
    websocket.close();
};

function sendText(json) {
    console.log("sending text: " + json);
    websocket.send(json);
}

// For testing purposes
var output = document.getElementById("test");
function writeToScreen(message) {
    output.innerHTML += message + "<br>";
}

function onOpen(evt) {

}
function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function onMessage(evt) {
    var msg = JSON.parse(evt.data);
    handleNotificationMsg(msg.dataItems);
    /*var msgType = msg.msgType;
     
     if (msgType === msgTypes[2]) {//KPI_INSTANT_DATA
     handleInstantDataMsg(msg.kpiDataItems);
     
     } else if (msgType === msgTypes[3]) { //KPI_HISTORY_DATA
     handleHistoryDataMsg(msg.kpiDataItems);
     }*/
}

function handleNotificationMsg(dataItems) {
    for (var i = 0; i < dataItems.length; i++) {
        var dataItem = dataItems[i];
        if ($("#trafficNotificationTable").length > 0/*.is(':visible')*/) {
            //build table rows
            //if ($("#trafficNotificationTable thead").children().length === 0) {
            $("#trafficNotificationTable  tbody").append("<tr></tr>");
            //}

            //create new columns
            //if setting = use table to show instant data
            var img = "../resources/img/";
            switch (dataItem.typeclass)
            {
                case notificationTypes[1]:
                    img += "alarm.bmp";
                    break;
                case notificationTypes[2]:
                    img += "warning.bmp";
                    break;
                case notificationTypes[3]:
                    img += "notification.bmp";
                    break;
            }
            var lastRow = $("#trafficNotificationTable tbody tr:last");
            lastRow.append("<td><img src='" + img +"' alt='unknown'></img></td>");
            lastRow.append("<td>" + dataItem.service + "</td>");
            lastRow.append("<td>" + dataItem.lineId + "</td>");
            lastRow.append("<td>" + dataItem.atrNodeId + "</td>");
            lastRow.append("<td>" + dataItem.time + "</td>");
            lastRow.append("<td>" + dataItem.text + "</td>");
        }
    }
}