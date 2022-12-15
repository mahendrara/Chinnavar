/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var wsUri = "ws://" + document.location.host + "/openSchedule-war/dashboardendpoint/groupByKPI";
if(window.websocket === undefined) 
    window.websocket = new WebSocket(wsUri);

window.onbeforeunload = function() {
    websocket.close();
};


var historyData = {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []};
var historyLabels = {};
function sendText(json) {
    console.log("sending text: " + json);
    websocket.send(json);
}

// For testing purposes
var output = document.getElementById("test");
function writeToScreen(message) {
    output.innerHTML += message + "<br>";
}

websocket.onerror = function(evt) {
    onError(evt);
};
function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

websocket.onopen = function(evt) {
    onOpen(evt);
};
function onOpen() {
}

websocket.onmessage = function(evt) {
    onMessage(evt);
};

function onMessage(evt) {
    var msg = JSON.parse(evt.data);
    var msgType = msg.msgType;

    if (msgType === msgTypes[2]) {//KPI_INSTANT_DATA
        handleInstantDataMsg(msg.kpiDataItems);

    } else if (msgType === msgTypes[3]) { //KPI_HISTORY_DATA
        handleHistoryDataMsg(msg.kpiDataItems);
    }
}

function handleInstantDataMsg(kpiDataItems)
{
    var kpiType = null;
    var instantTableIdSelector = null;
    var locationId = null;

    for (var i = 0; i < kpiDataItems.length; i++) {
        var kpiDataItem = kpiDataItems[i];
        kpiType = kpiDataItem.kpiType;
        //var date = new Date(kpiDataItems[i].time);
        instantTableIdSelector = "#" + kpiSettings[kpiType].prefix + "Table";
        locationId = kpiDataItem.locationId;
        if ($(instantTableIdSelector).length === 0/*.is(':visible')*/) {
             $("#body").append("<div class='row'>\n\
                <h4>" + kpiSettings[kpiType].text + "</h4>" +
                    "<div class='col-lg-7 widget'>\n\
                    <h5>Instant Data</h5>\n\
                    <table id = '"+ kpiSettings[kpiType].prefix +"Table'class='table table-striped table-bordered table-hover'>\n\
                        <thead></thead>\n\
                        <tbody></tbody>\n\
                    </table>\n\
                    <canvas></canvas>\n\
                </div>\n\
                <div id = '" + kpiSettings[kpiType].prefix + "Chart' class='col-lg-5'>\n\
                    <h5 style='text-align:center;'>History of " + kpiSettings[kpiType].text + "</h5>\n\
                </div>\n\
            </div>");
        }
        {
            //build table rows
            if ($(instantTableIdSelector + " thead").children().length === 0) {
                //if setting = use table to show instant data
                $(instantTableIdSelector + " tbody").append("<tr id='datatr" + kpiType + "'></tr>");
                //if setting = use gauge to show instant data
                $(instantTableIdSelector + " tbody").append("<tr id='gaugetr" + kpiType + "'></tr>");
            }

            //fill table cells
            if ($(instantTableIdSelector + " thead #th" + locationId).length > 0) {
                //if setting = use table to show instant data
                if ($(instantTableIdSelector + " tbody #datatr" + kpiType + " #datatd" + locationId).length > 0)
                    $(instantTableIdSelector + " tbody #datatr" + kpiType + " #datatd" + locationId).text(kpiDataItem.data.toFixed(2));

            }
            else { //create new columns
                $(instantTableIdSelector + " thead").append("<th id='th" + locationId + "' style='background-color:" + locationSettings[kpiDataItem.locationTxt].background + "; color:" + locationSettings[kpiDataItem.locationTxt].font + "'>" + kpiDataItem.locationTxt + "</th>");
                //if setting = use table to show instant data
                $(instantTableIdSelector + " tbody tr[id$='datatr" + kpiType + "']").append("<td id='datatd" + locationId + "'>" + kpiDataItem.data.toFixed(2) + "</td>");
                //if setting = use gauge to show instant data
                $(instantTableIdSelector + " tbody tr[id$='gaugetr" + kpiType + "']").append("<td id='gaugetd" + locationId + "'></td>");
            }
            //temp code for testing gauge
            //if setting = use gauge to show instant data
            if (kpiType !== kpiTypes[2]/*&& kpiDataItem.locationTxt !== 'ALL_LINES'*/) {
                var gaugeSelector = instantTableIdSelector + " tbody tr[id$='gaugetr" + kpiType + "'] td[id$='gaugetd" + locationId + "']";
                showGauge(gaugeSelector, kpiType + "_" + locationId, kpiDataItem.data, kpiType);
            }
        }
    }
};


function handleHistoryDataMsg(kpiDataItems) {
    var kpiType = null;
    //var direction;
    var historyChartSelector = null;
    //var data = null;
    for (var i = 0; i < kpiDataItems.length; i++) {
        var kpiDataItem = kpiDataItems[i];
        kpiType = kpiDataItem.kpiType;
        historyChartSelector = "#" + kpiSettings[kpiType].prefix + "Chart";

        if ($(historyChartSelector).length > 0) {
            //data = historyData[kpiType];
            var date = new Date(kpiDataItems[i].time);

            if (historyData[kpiType].length === maxDataPoint)
                historyData[kpiType] = [];

            var tmpData = [];
            tmpData.push(date);

            if (historyLabels[kpiType]) {
                for (var j = 0; j < kpiDataItem.dataPoints.length; j++) {
                    //if(historyLabels[kpiType].locationMap[j].key)
                    //To do: check dataPoints' location = exited labels
                    tmpData.push(kpiDataItem.dataPoints[j].data.toFixed(2));
                }

            } else {
                var labelTxt = [];
                labelTxt.push("Time");
                var labelMap = [];
                for (var j = 0; j < kpiDataItem.dataPoints.length; j++) {
                    var temp = kpiDataItem.dataPoints[j].locationId;
                    labelTxt.push(kpiDataItem.dataPoints[j].locationTxt);
                    //create Y axis mapping: location and index in chart
                    labelMap.push(temp, kpiDataItem.dataPoints[j].locationTxt);
                    tmpData.push(kpiDataItem.dataPoints[j].data.toFixed(2));
                }
                if (historyChartSettings[kpiType].boundaryLines === true) {
                    labelTxt.push("Lower Amber");
                    labelTxt.push("Lower Red");
                }
                historyLabels[kpiType] = {"locationMap": labelMap, "labelTxt": labelTxt};
            }
            //boundary lines
            if (historyChartSettings[kpiType].boundaryLines === true) {
                tmpData.push(gaugeSettings[kpiType].highlights.amber_min);
                tmpData.push(gaugeSettings[kpiType].highlights.red_min);
            }
            historyData[kpiType].push(tmpData);
        }
    }

    $.each(historyData, function(key, value) {
        if ($(historyChartSelector).find("Div").length === 0)
            drawLineGraph("#" + kpiSettings[key].prefix + "Chart", key);
        else if (graphs[key])
            graphs[key].updateOptions({'file': value});
    });
};

/**
 * 
 * @param {String} containerId
 * @param {Array} data: array that contains new data points
 * @param {String} title: e.g. all areas, circle lines
 * @param {String} ylabel
 * @returns {undefined}
 */
function drawLineGraph(containerSelector, /*data,*/ kpiType) {
    //if the chart does not exist, but the labels are ready
    if (graphs[kpiType] === undefined && historyLabels[kpiType])
    {
        $(containerSelector).append("<div id='chartLabel" + kpiType + "' class='chart_Labels'></div>");
        $(containerSelector).append("<div id='chart" + kpiType + "' class='few'></div>");

        var labelColors = [locationSettings["METROPOLITAN"].background, locationSettings["HAMMERSMITH_CITY"].background, locationSettings["CIRCLE"].background,
            locationSettings["DISTRICT"].background, locationSettings["PICCADILLY"].background, locationSettings["ALL_LINES"].background];

        if (historyChartSettings[kpiType].boundaryLines === true) {
            labelColors.push(gaugeColor.AMBER);
            labelColors.push(gaugeColor.RED);
        }

        var g = new Dygraph(document.getElementById("chart" + kpiType), historyData[kpiType],
                {
                    //drawPoints: true,
                    //showRoller: true,
                    //customerBars: true,
                    //errorBars:true,
                    //title: historyChartSettings[kpiType].title,
                    //titleHeight: 23,
                    ylabel: historyChartSettings[kpiType].yLabel,
                    xlabel: "Time",
                    //xLabelHeight:14,
                    legend: 'always',
                    labelsDivStyles: {'textAlign': 'right'},
                    axisLabelFontSize: 12,
                    showRangeSelector: historyChartSettings[kpiType].rangeLines,
                    rangeSelectorHeight: 30,
                    width: 580,
                    height: 320,
                    highlightCircleSize: 2,
                    labels: historyLabels[kpiType].labelTxt, //['Time', 'Metropolitan', 'Hammersmith and City', 'Circle', 'District', 'Piccadilly', 'All Lines'],
                    labelsDiv: /*$("#chartLabel" + kpiType),*/ document.getElementById("chartLabel" + kpiType),
                    colors: labelColors,
                    strokeWidth: 1,
                    strokeBorderWidth: 1,
                    highlightSeriesOpts: {
                        strokeWidth: 1,
                        strokeBorderWidth: 1,
                        highlightCircleSize: 5
                    },
                    underlayCallback: function(canvas, area, g) {
                        if (gaugeSettings[kpiType].visible && historyChartSettings[kpiType].boundarLines) {
                            var splitY1 = g.toDomYCoord(gaugeSettings[kpiType].highlights.amber_min);
                            var splitY2 = g.toDomYCoord(gaugeSettings[kpiType].highlights.red_min);


                            // The drawing area doesn't start at (0, 0), it starts at (area.x, area.y).
                            // That's why we subtract them from splitX and splitY. This gives us the
                            // actual distance from the upper-left hand corder of the graph itself.
                            var topHeight = splitY2 - area.y;
                            var bottomHeight = area.h - topHeight - (splitY1 - splitY2);


                            canvas.fillStyle = RED;
                            canvas.fillRect(area.x, area.y, area.w, topHeight);

                            canvas.fillStyle = AMBER;
                            canvas.fillRect(area.x, splitY1, area.w, splitY2 - splitY1);

                            canvas.fillStyle = GREEN;
                            canvas.fillRect(area.x, splitY1, area.w, bottomHeight);
                        }
                    }
                });
        if (historyChartSettings[kpiType].valueRange)
            g.updateOptions({
                valueRange: historyChartSettings[kpiType].valueRange
            });
        if (historyChartSettings[kpiType].boundaryLines === true)
            g.updateOptions({
                'Lower Red': {
                    strokePattern: [3, 4],
                    strokeWidth: 2,
                    highlightCircleSize: 0
                    /*drawPoints: true,
                    pointSize: 2*/
                },
                'Lower Amber': {strokePattern: [3, 4],
                    strokeWidth: 2,
                    highlightCircleSize: 0
                }
            });

        graphs[kpiType] = g;
        // It sucks that these things aren't objects, and we need to store state in window.
        //window.intervalId = setInterval(function() {
        //    g.updateOptions({'file': historyData[kpiType]});
        //}, 5000);
    }

    if (historyChartSettings[kpiType].rangeSelector === true)
        g.updateOptions({
            showRangeSelector: true,
            valueRange: null
        });
}

function showGauge(selector, id, value, kpiType) {
    var container = $(selector);//document.getElementById(containerId);
    var color = getGaugeStatus(kpiType, value);//getGaugeStatus(gaugeSettings[kpiType].highlights.amber_min, gaugeSettings[kpiType].highlights.red_min, gaugeSettings[kpiType].highlights.red_max, value);
    var gaugeId = "gauge_" + id;
    if (container.children().length === 0) {
        container.append("<canvas id='canvas_" + id + "'></canvas>");
        var min, mid1, mid2, max, minColor, midColor, maxColor;
        if (kpiType === kpiTypes[1]) {
            min = gaugeSettings[kpiType].highlights.red_min;
            mid1 = gaugeSettings[kpiType].highlights.amber_min;
            mid2 = gaugeSettings[kpiType].highlights.green_min;
            max = gaugeSettings[kpiType].highlights.green_max;
            minColor = gaugeColor.RED;
            midColor = gaugeColor.AMBER;
            maxColor = gaugeColor.GREEN;
        } else {
            min = gaugeSettings[kpiType].highlights.green_min;
            mid1 = gaugeSettings[kpiType].highlights.amber_min;
            mid2 = gaugeSettings[kpiType].highlights.red_min;
            max = gaugeSettings[kpiType].highlights.red_max;
            minColor = gaugeColor.GREEN;
            midColor = gaugeColor.AMBER;
            maxColor = gaugeColor.RED;
        }
        var gauge = new Gauge({
            renderTo: 'canvas_' + id,
            width: 120,
            height: 120,
            glow: true,
            units: gaugeSettings[kpiType].units,
            title: false,
            minValue: min,
            maxValue: max,
            majorTicks: gaugeSettings[kpiType].majorTicks,
            minorTicks: 2,
            strokeTicks: false,
            valueFormat: {
                int: 1,
                dec: 2
            },
            highlights: [
                /*{from: 0, to: 6, color: 'rgba(0,   128, 0, .50)'},
                 {from: 6, to: 10, color: 'rgba(255, 255, 0, .50)'},
                 {from: 10, to: 20, color: 'rgba(255, 0,  0, .50)'}*/
                {from: min, to: mid1, color: minColor},
                {from: mid1, to: mid2, color: midColor},
                {from: mid2, to: max, color: maxColor}
            ],
            animation: {
                delay: 0,
                duration: 250,
                fn: 'cycle'
            },
            colors: {
                plate: color, //'#ffffff',
                majorTicks: '#f5f5f0',
                minorTicks: '#ddd',
                title: '#fff',
                units: '#ffffff',
                numbers: '#000000',
                needle: {start: 'rgba(240, 128, 128, 1)', end: 'rgba(255, 160, 122, .9)'}
            }/*,
            underlayCallback: function(canvas, area, g) {
                // Selecting a date in the middle of the graph. 
                var splitY1 = g.toDomYCoord(gaugeSettings[kpiType].highlights.amber_min);
                var splitY2 = g.toDomYCoord(gaugeSettings[kpiType].highlights.red_min);


                // The drawing area doesn't start at (0, 0), it starts at (area.x, area.y).
                // That's why we subtract them from splitX and splitY. This gives us the
                // actual distance from the upper-left hand corder of the graph itself.
                var topHeight = splitY2 - area.y;
                var bottomHeight = area.h - topHeight - (splitY1 - splitY2);


                canvas.fillStyle = gaugeColor.RED;
                canvas.fillRect(area.x, area.y, area.w, topHeight);

                canvas.fillStyle = gaugeColor.AMBER;
                canvas.fillRect(area.x, splitY1, area.w, splitY2 - splitY1);

                canvas.fillStyle = gaugeColor.GREEN;
                canvas.fillRect(area.x, splitY1, area.w, bottomHeight);
            }*/
        });

        gauges[gaugeId] = gauge;
        gauge.setValue(value);
        gauge.draw();
    }
    else {
        var gauge = gauges[gaugeId];
        gauge.setValue(value);

        if (gauge.config.colors.plate !== color) {
            gauge.updateConfig({
                colors: {
                    plate: color
                }
            });
        }
    }
}

function getGaugeStatus(kpiType, value)
{
    var color = "#ffffff";
    var min_threshold;
    var middle_threshold;
    //var max_threshold;
    if ((kpiType === kpiTypes[0]) || (kpiType === kpiTypes[3]) || (kpiType === kpiTypes[4]))
    {
        min_threshold = gaugeSettings[kpiType].highlights.amber_min;
        middle_threshold = gaugeSettings[kpiType].highlights.red_min;
        //max_threshold = gaugeSettings[kpiType].highlights.red_max, value;
        if (value < min_threshold)
            color = GREEN;
        else if (value < middle_threshold)
            color = AMBER;
        else
            color = RED;
    } else if (kpiType === kpiTypes[1])
    {
        min_threshold = gaugeSettings[kpiType].highlights.green_min;
        middle_threshold = gaugeSettings[kpiType].highlights.amber_min;

        if (value > min_threshold)
            color = GREEN;
        else if (value > middle_threshold)
            color = AMBER;
        else
            color = RED;
    }
    return color;
}

function init() {
}

