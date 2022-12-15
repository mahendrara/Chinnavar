/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var wsUri = "ws://" + document.location.host + "/openSchedule-war/dashboardendpoint/groupByLocation";
if (window.websocket === undefined)
    window.websocket = new WebSocket(wsUri);
window.onbeforeunload = function() {
    websocket.close();
};
var view = {"1": "Default View", "2": "Instant Data View"};
/**history data graph
 * 
 * @type Number maxDataPoint: buffer size for each kpi
 * @type Array graphs: references to each kpi's history graph
 */

var historyData = {0: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []},
    1: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []},
    2: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []},
    3: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []},
    4: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []},
    5: {AVERAGE_LATENESS: [], PERCENTAGE_OF_HEADWAYS: [], KILOMETERS_DELIVERED: [], JOURNEY_TIME_STATISTICS: [], PLATFORM_WAIT_TIME: []}};
//mapping between locationId and index in history data, initialize according to filter settings
//var historyYAxisMapping = [];
//var historyLabels = {[]};

function sendText(json) {
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
;

$(".temp").click(function() {
    $("#setting").toggle();
    //$(".tFilter").toggle();
    if ($("#settingDiv").is(":visible"))
    {
        $(".tFilter").text("Settings");
        $("#settingDiv").css("display", "none");
    }
    else
    {
        $(".tFilter").text("Settings");
        $("#settingDiv").css("display", "block");
    }

    $("#dataDiv").toggleClass("col-lg-12");
    $("#dataDiv").toggleClass("col-lg-10");

    $.each(graphs, function(locationId, graph) {
        if (graph !== undefined && graph !== null) {
            graph.resize();
            //$("#chart" + locationId).css({ "right": "10px"});
            //graphs[locationId].updateOptions({'height': 280});
        }
    });
});

$("#submitSettingBtn").click(function() {
    sendText("{'msgType':1}");
    if ($("#combinedView")[0].checked) {
        selectedView = views[0];
        //$("id$=Chart").removeAttr("onSubmit");//.find("*").off();//.unbind("change");
        
    } else if ($("#instantView")[0].checked) {
        selectedView = views[1];
        //$("id$=Chart").removeAttr("onSubmit");//.find("*").off();//.unbind("change");
    } else {
        selectedView = views[2];
    }
    
    if ($("#settingDiv #gauge")[0].checked) {
        presentationSettings.instant.presentation = presentationTypes[0];
    } else if ($("#settingDiv #simpleGauge")[0].checked) {
        presentationSettings.instant.presentation = presentationTypes[1];
    } else {
        presentationSettings.instant.presentation = presentationTypes[2];
    }

    $("#dataDiv").empty();
    return false;
});

$("#resetSettingBtn").click(function() {
    return false;
});
function handleInstantDataMsg(kpiDataItems) {
    var kpiType = null;
    var instantDivSelector = null;
    var locationId = null;
    for (var i = 0; i < kpiDataItems.length; i++) {
        var kpiDataItem = kpiDataItems[i];
        kpiType = kpiDataItem.kpiType;
        //instantTableIdSelector = "#" + kpiSettings[kpiType].prefix + "Table";
        locationId = kpiDataItem.locationId;
        //show instant and history data
        if ($("#combinedView")[0].checked) {
            instantDivSelector = "#" + locationId + "Div";
            if ($(instantDivSelector).length === 0) {
                $("#dataDiv").append("<div class='row' id='" + locationId + "Div'>\n\
                    <h4  style='background-color:" + locationSettings[kpiDataItem.locationTxt].background + "; color:" + locationSettings[kpiDataItem.locationTxt].font + "'>" + kpiDataItem.locationTxt + "</h4>" +
                        "<div id = '" + locationId + "instantDiv' class='col-lg-7 widget'>\n\
                        <h5>Instant Data</h5>\n\
                        <table class='table table-striped table-bordered table-hover'>\n\
                            <thead></thead>\n\
                            <tbody></tbody>\n\
                        </table>\n\
                        <canvas></canvas>\n\
                        </div>\n\
                    <div id = '" + locationId + "Chart' class='col-lg-5'>\n\
                        <h5 style='text-align:center;'>History of\n\
                        <select size='1'>\n\
                            <option value = '" + kpiTypes[0] + "'>" + kpiSettings.AVERAGE_LATENESS.text + "</option>\n\
                            <option value = '" + kpiTypes[1] + "'>" + kpiSettings.PERCENTAGE_OF_HEADWAYS.text + "</option>\n\
                            <option value = '" + kpiTypes[2] + "'>" + kpiSettings.KILOMETERS_DELIVERED.text + " </option>\n\\n\
                            <option value = '" + kpiTypes[3] + "'>" + kpiSettings.JOURNEY_TIME_STATISTICS.text + " </option>\n\
                            <option value = '" + kpiTypes[4] + "'>" + kpiSettings.PLATFORM_WAIT_TIME.text + " </option>\n\
                        </select>\n\
                        </h5>\n\
                    </div>\n\
                </div>");

                $("#" + locationId + "Chart select").change(function() {
                    var location = $(this).parent().parent().attr("id").replace('Chart', '');
                    graphs[location].destroy();
                    graphs[location] = null;
                    $("#chart" + location).find("*").off();
                    $("#chart" + location).empty();
                    drawLineGraph(location, $(this).val());
                });
            }
            fillInstantTable(kpiDataItem);
        } else if ($("#instantView")[0].checked) {//show instant data only
            instantDivSelector = "#dataDiv";
            if ($("#dataDiv table").length === 0) {
                $("#dataDiv").append("<table class='table table-striped table-bordered table-hover'>\n\
                    <thead></thead>\n\
                    <tbody></tbody>\n\
                    </table>\n\
                ");
            }
            fillInstantTable(kpiDataItem);
        } else if ($("#historyView")[0].checked) {//show history data only
            if ($("#dataDiv").length === 0) {

            }
        }

        /*$('# ul.dropdown-menu li a').click(function(e) {
         var $div = $(this).parent().parent().parent().parent();
         var $btn = $div.find('button');
         $btn.html($(this).text() + ' <span class="caret"></span>');
         $btn.attr("value", $(this).attr("name"));
         $div.removeClass('open');
         e.preventDefault();
         return false;
         });*/
        //build table rows

    }
}
;

function fillInstantTable(kpiDataItem) {
    //if($("#combinedView")[0].checked) {
    var locationId = kpiDataItem.locationId;
    var kpiType = kpiDataItem.kpiType;
    if (selectedView === views[0]) {
        var instantDivSelector = "#" + locationId + "Div";
        if ($(instantDivSelector + " thead").children().length === 0) {
            $(instantDivSelector + " tbody").append("<tr id='tr" + locationId + "'></tr>");
        }

        if ($(instantDivSelector + " thead #th" + kpiType).length === 0) {
            var text = kpiSettings[kpiType].text;
            if(presentationSettings.instant.presentation === presentationTypes[1]||presentationSettings.instant.presentation === presentationTypes[2])
                text += " (" + gaugeSettings[kpiType].units + ")";
            $(instantDivSelector + " thead").append("<th id='th" + kpiType + "'>" + text + "</th>");
            $(instantDivSelector + " tbody tr[id$='tr" + locationId + "']").append("<td id='td" + kpiType + "'></td>");
        }

        switch (presentationSettings.instant.presentation) {
            case presentationTypes[2]:
                $(instantDivSelector + " tbody #tr" + locationId + " #td" + kpiType).text(kpiDataItem.data.toFixed(2));
                break;
            case presentationTypes[0]:
                {
                    var gaugeSelector = instantDivSelector + " tbody tr[id$='tr" + locationId + "'] td[id$='td" + kpiType + "']";
                    drawGauge(gaugeSelector, kpiType + "_" + locationId, kpiDataItem.data, kpiType);
                    break;
                }
            case presentationTypes[1]:
                {
                    var gaugeSelector = instantDivSelector + " tbody tr[id$='tr" + locationId + "'] td[id$='td" + kpiType + "']";
                    drawSimpleGauge(gaugeSelector, kpiType + "_" + locationId, kpiDataItem.data, kpiType);
                    break;
                }
        }
    }
    else if (selectedView === views[1]) {
        instantDivSelector = "#dataDiv";
        if ($("#dataDiv thead").children().length === 0) {
            $("#dataDiv thead").append("<th></th>");
        }
        if ($("#dataDiv tbody tr[id$='tr" + locationId + "']").length === 0) {
            $("#dataDiv tbody").append("<tr id='tr" + locationId + "'><td style='background-color:" + locationSettings[kpiDataItem.locationTxt].background
                    + "; color:" + locationSettings[kpiDataItem.locationTxt].font + "'>" + kpiDataItem.locationTxt + "</td></th>");
        }

        if ($("#dataDiv thead #th" + kpiType).length === 0) {
            var text = kpiSettings[kpiType].text;
            if(presentationSettings.instant.presentation === presentationTypes[1]||presentationSettings.instant.presentation === presentationTypes[2])
                text += " (" + gaugeSettings[kpiType].units + ")";
            $("#dataDiv thead").append("<th id='th" + kpiType + "'>" + text + "</th>");
        }

        if ($("#dataDiv tbody tr[id$='tr" + locationId + "']" + " td[id$='td" + kpiType + "']").length === 0)
            $("#dataDiv tbody tr[id$='tr" + locationId + "']").append("<td id='td" + kpiType + "'></td>");

        switch (presentationSettings.instant.presentation) {
            case presentationTypes[2]:
                    $("#dataDiv tbody #tr" + locationId + " #td" + kpiType).text(kpiDataItem.data.toFixed(2));
                break;
            case presentationTypes[0]:
                {
                    var gaugeSelector = "#dataDiv tbody tr[id$='tr" + locationId + "'] td[id$='td" + kpiType + "']";
                    drawGauge(gaugeSelector, kpiType + "_" + locationId, kpiDataItem.data, kpiType);
                    break;
                }
            case presentationTypes[1]:
                {
                    var gaugeSelector = "#dataDiv tbody tr[id$='tr" + locationId + "'] td[id$='td" + kpiType + "']";
                    drawSimpleGauge(gaugeSelector, kpiType + "_" + locationId, kpiDataItem.data, kpiType);
                    break;
                }
        }
    }
}
;
//var baseTimeLine = null;
function handleHistoryDataMsg(kpiDataItems) {
    //var kpiType = null;
    var locationId = null;
    //var direction;
    var historyChartSelector = null;
    //var data = null;
    for (var i = 0; i < kpiDataItems.length; i++) {
        var kpiDataItem = kpiDataItems[i];
        locationId = kpiDataItem.locationId;
        historyChartSelector = "#" + locationId + "Chart";
        if ($(historyChartSelector).length > 0/*.is(':visible')*/) {
            var date = new Date(kpiDataItems[i].time);
            if (historyData[locationId][kpiDataItem.kpiType].length === maxDataPoint)
                historyData[locationId][kpiDataItem.kpiType] = [];
            var tmpData = [];
            tmpData.push(date);
            //baseTimeLine= date;
            tmpData.push(kpiDataItem.data.toFixed(2));
            //boundary lines
            if (historyChartSettings[kpiDataItem.kpiType].boundaryLines === true) {
                tmpData.push(gaugeSettings[kpiDataItem.kpiType].highlights.amber_min);
                tmpData.push(gaugeSettings[kpiDataItem.kpiType].highlights.red_min);
            }
            historyData[locationId][kpiDataItem.kpiType].push(tmpData);
        }

    }

    $.each(historyData, function(locationId, value) {
        $.each(value, function(kpi, value) {

            if (graphs[locationId] !== undefined && graphs[locationId] !== null && $("#chart" + locationId).length !== 0) {
                if (kpi === $("#" + locationId + "Chart" + " option:selected").attr("value"))
                    graphs[locationId].updateOptions({'file': value});
            }
            else
                drawLineGraph(locationId, kpi);
            /*if (graphs[key])
             if (key1 === "AVERAGE_LATENESS")
             graphs[key].updateOptions({'file': value1});
             if (key1 === "AVERAGE_LATENESS")
             drawLineGraph(key, key1);*/
        });
    });
}
;
function drawLineGraph(locationId, kpiType) {
    var containerSelector = "#" + locationId + "Chart";
    if ($(containerSelector).length > 0) {
        $(containerSelector).append("<div id='chartLabel" + locationId + "' class='chart_Labels'></div>");
        $(containerSelector).append("<div id='chart" + locationId + "' class='few'></div>");
        var labelColors = ["#000000"];
        if (historyChartSettings[kpiType].boundaryLines === true) {
            labelColors.push(gaugeColor.AMBER);
            labelColors.push(gaugeColor.RED);
        }
        if(graphs[locationId]!== undefined && graphs[locationId]!== null) 
        {
            graphs[locationId].destroy();
            graphs[locationId] = null;
        } 
        
        var g = new Dygraph(document.getElementById("chart" + locationId), historyData[locationId][kpiType],
            {
                //title: historyChartSettings[kpiType].title,
                //titleHeight: 23,
                ylabel: gaugeSettings[kpiType].unit,
                xlabel: "Time",
                //xLabelHeight:14,
                legend: 'always',
                labelsDivStyles: {'text-align': 'right'},
                axisLabelFontSize: 12,
                //showRangeSelector: historyChartSettings[kpiType].rangeLines,
                //rangeSelectorHeight: 30,
                //width: 580,
                height: 280,
                highlightCircleSize: 2,
                labels: ['Time', kpiSettings[kpiType].text, "Lower Amber", "Lower Red"],
                labelsDiv: document.getElementById("chartLabel" + locationId),
                colors: labelColors,
                strokeWidth: 1,
                strokeBorderWidth: 1,
                highlightSeriesOpts: {
                    strokeWidth: 1,
                    strokeBorderWidth: 1,
                    highlightCircleSize: 5
                }/*,
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
                 }*/
            });
        $("#chart" + locationId).css({"position": "absolute", "left": "10px", "right": "10px", "width": "", "height": ""});
        if (historyChartSettings[kpiType].boundaryLines === true)
            g.updateOptions({
                'Lower Red': {
                    strokePattern: [3, 4],
                    strokeWidth: 2,
                    highlightCircleSize: 0
                            //drawPoints: true,
                            //pointSize: 2
                },
                'Lower Amber': {strokePattern: [3, 4],
                    strokeWidth: 2,
                    highlightCircleSize: 0
                }
            });

        graphs[locationId] = g;
        if (historyChartSettings[kpiType].rangeSelector === true)
            g.updateOptions({
                showRangeSelector: true//,
                //valueRange: null
            });
    }
}
;
function drawGauge(selector, id, value, kpiType) {
    var container = $(selector);
    var color = getGaugeStatus(kpiType, value); //gaugeSettings[kpiType].highlights.amber_min, gaugeSettings[kpiType].highlights.red_min, gaugeSettings[kpiType].highlights.red_max, value);
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
        //var test = $('#canvas_' + id);
        var test = document.getElementById( 'canvas_' + id);
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
            strokeTicks: true,
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
                plate: color,
                majorTicks: '#f5f5f0',
                minorTicks: '#ddd',
                title: '#fff',
                units: "#000000", //'#ffffff',
                numbers: '#000000',
                needle: {start: 'rgba(255, 255, 255, 1)', end: 'rgba(255, 255, 255, .9)'}
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
;
function drawSimpleGauge(selector, id, value, kpiType) {
    var container = $(selector);
    var color = getGaugeStatus(kpiType, value); //gaugeSettings[kpiType].highlights.amber_min, gaugeSettings[kpiType].highlights.red_min, gaugeSettings[kpiType].highlights.red_max, value);
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
            width: 60,
            height: 60,
            glow: true,
            minValue: min,
            maxValue: max,
            strokeTicks: true,
            valueFormat: {
                int: 1,
                dec: 2
            },
            colors: {
                plate: color,
                majorTicks: '#f5f5f0',
                minorTicks: '#ddd',
                title: '#fff',
                units: "#000000", //'#ffffff',
                numbers: '#000000',
                needle: {start: 'rgba(255, 255, 255, 1)', end: 'rgba(255, 255, 255, .9)'}
            },
            showTickes: false,
            showUnits: false,
            showNumbers: false,
            showNeedle: false,
            showHighlights: false,
            showValueBox: true
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
;
function getGaugeStatus(kpiType, value)
{
    var color = "#ffffff";
    var min_threshold;
    var middle_threshold;
    //var max_threshold;
    if (kpiType !== kpiTypes[1])
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
    } else
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
;
function init() {
}
;



