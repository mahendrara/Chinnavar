/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var msgTypes = ["SET_FILTERS", "CHANGE_SETTINGS", "KPI_INSTANT_DATA", "KPI_HISTORY_DATA"];
var kpiTypes = ["AVERAGE_LATENESS", "PERCENTAGE_OF_HEADWAYS", "KILOMETERS_DELIVERED",
    "JOURNEY_TIME_STATISTICS", "PLATFORM_WAIT_TIME"];
var kpiLocations = ["ALL_LINES", "METROPOLITAN", "HAMMERSMITH_CITY", "CIRCLE", "DISTRICT", "PICCADILLY"];
var kpiSettings = {"AVERAGE_LATENESS": {"prefix": "averageLateness","text": "Average Lateness"},
    "PERCENTAGE_OF_HEADWAYS": {"prefix": "headwayPercentage","text":"Headway Percentage"},
    "KILOMETERS_DELIVERED": {"prefix": "kilometer","text":"Excess Kilometres Delivered"},
    "JOURNEY_TIME_STATISTICS": {"prefix": "journeyTime","text":"Excess Journey Time"},
    "PLATFORM_WAIT_TIME": {"prefix": "waitTime","text": "Excess Platform Wait Time"}};
//temp
var locationSettings = {
    "ALL_LINES": {"background": "#000000", font: "#FFFFFF"},
    "METROPOLITAN": {"background": "#893267", font: "#FFFFFF"},
    "HAMMERSMITH_CITY": {"background": "#E899A8", font: "#FFFFFF"},
    "CIRCLE": {"background": "#F8D42D", font: "#FFFFFF"},
    "DISTRICT": {"background": "#00A575", font: "#FFFFFF"},
    "PICCADILLY": {"background": "#0450A1", font: "#FFFFFF"}};
var gaugeSettings = {
    "AVERAGE_LATENESS": {"visible": true, "majorTicks": ['-20', '-15', '-10', '-5', '0', '5', '10', '15', '20'], "highlights": {"green_min": -20, "amber_min": 5, "red_min": 10, "red_max": 20}, "units":"minute"},
    "PERCENTAGE_OF_HEADWAYS": {"visible": true, "majorTicks": ['70', '75', '80', '85', '90', '95', '100'], "highlights": {"green_max": 100, "green_min": 90, "amber_min": 80, "red_min": 70}, "units":"%"},
    "KILOMETERS_DELIVERED": {"visible": false,  "majorTicks": ['-20', '-15', '-10', '-5', '0', '5', '10', '15', '20'], "highlights": {"green_min": -20, "amber_min": 5, "red_min": 10, "red_max": 20}, "units":"km"},
    "JOURNEY_TIME_STATISTICS": {"visible": true, "majorTicks": ['-20', '-15', '-10', '-5', '0', '5', '10', '15', '20'], "highlights": {"green_min": -20, "amber_min": 5, "red_min": 10, "red_max": 20}, "units":"minute"},
    "PLATFORM_WAIT_TIME": {"visible": true, "majorTicks": ['-20', '-15', '-10', '-5', '0', '5', '10', '15', '20'], "highlights": {"green_min": -20, "amber_min": 5, "red_min": 10, "red_max": 20}, "units":"minute"}
};

var historyChartSettings = {
    "AVERAGE_LATENESS": {"visible": true, "boundaryLines": true, "rangeSelector": true, "title": "History of Average Lateness", "yLabel": "Minute"/*, "valueRange": [0, 20]*/},
    "PERCENTAGE_OF_HEADWAYS": {"visible": true, "boundaryLines": true, "rangeSelector": true, "title": "History of Headway Percentage", "yLabel": "Percentage" /*, "valueRange": [50, 100]*/},
    "KILOMETERS_DELIVERED": {"visible": true, "boundaryLines": true, "rangeSelector": true, "title": "History of Excess Kilometers Delivered", "yLabel": "Kilometer"},
    "JOURNEY_TIME_STATISTICS": {"visible": true, "boundaryLines": true, "rangeSelector": true, "title": "History of Excess Journey Time Statistics", "yLabel": "Minute"},
    "PLATFORM_WAIT_TIME": {"visible": true, "boundaryLines": true, "rangeSelector": true, "title": "History of Excess Platform Wait Time Statistics", "yLabel": "Minute" /*", "valueRange": [0, 100]*/}
};
var presentationTypes = ["gauge","simpleGauge","numeric", "chart"];
//default settings;
var presentationSettings = {"instant": { "presentation": "gauge"},"history" : {"presentation":"chart"}};

var maxDataPoint = 5000;//buffer size

var graphs = {};
/*
 * configurations for gauge 
 */
var RED = "#D63636";//"#cc0605";
var AMBER = "#FFD24D";//"#ffbe00";
var GREEN = "#33a532";
//var gaugeColor = {"GREEN": "rgba(3, 84, 0, 1)", "AMBER": "rgba(191, 142, 0, 1)", "RED": "rgba(100, 0, 0, 1)"};
var gaugeColor = {"GREEN": "#035400", "AMBER": "#AC8000", "RED": "#640000"};
var gauges = {};
var views = {0: "combinedView",1: "instantView", 2: "historyView"};
var selectedView = "combinedView";