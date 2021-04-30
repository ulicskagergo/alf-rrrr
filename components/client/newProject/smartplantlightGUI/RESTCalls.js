const baseURL = "localhost:8080"
const getPointsExtension = "/data"
const settingsUpdateExtension = "/settings" // both send and receive / GET and PUSH
const getDatesExtension = "/dates"

var pointsObj
var dates

function getPoints(dateIndex) {
    console.log("called getPoints")

    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        var points = JSON.parse(this.responseText);
        console.log(points);
      }
    };
    // TODO get date dummy from index
    var dateDummy = "2021-04-23T13:32:41.100";
    xmlhttp.open("GET", baseURL+chartRefreshExtension+"/"+dateDummy, true);
    xmlhttp.send();

    var pointsJson;
    pointsJson = '{'
        +'"points":'
        +'[{"is_on":true, "actual_value":10, "time":"2021-04-30T14:32:56.675310"}'
        +'[{"is_on":false, "actual_value":80, "time":"2021-04-30T14:33:36.675310"}'
    +'}';
    pointsObj = JSON.parse(pointsJson);

    // !! TODO új json formátumot kezelni

    // removing old points and adding the new ones
    lightValueSeries.removePoints(0, lightValueSeries.count);
    turnOnOffSeries.removePoints(0, turnOnOffSeries.count);
    for(var i in pointsObj.points) {
        console.log("x: " + pointsObj.points[i].x + ", y: " + pointsObj.points[i].y);
        lightValueSeries.append(pointsObj.points[i].x, pointsObj.points[i].y);
        turnOnOffSeries.append(pointsObj.points[i].x, pointsObj.points[i].light===true?1:0);
    }
}


function getDates() {
    console.log("called getDates")
    var datesJson = '{[ "2021-04-23T01:00:00.000", "2021-04-24T00:01:00.000", "2021-04-26T01:00:00.000" ]}';
    dates = JSON.parse(datesJson);
    listmodel.remove(0, listmodel.count);
    for(var i in dates) {
        var date = new Date(dates[i]);
        var dict = { "date": date.toDateString() }
        console.log(dict["date"]);
        listmodel.append(dict);
    }
}

function pushSettings(sensorSensitivity, fromto) {
    console.log("pushSettings: sens:" + sensorSensitivity + ", from-to: " + fromto)

    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        // var points = JSON.parse(this.responseText);
        // console.log(points);
      }
    };
    xmlhttp.open("POST", baseURL+chartRefreshExtension, true);
    // TODO put together settings JSON
    xmlhttp.send(JSON.stringify({ "sensitivity": sensorSensitivity, "from": "15:00", "to":"19:00" }));
}

// received: { "sensitivity": sensorSensitivity, "from": "15:00", "to":"19:00" }
function pullSettings() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        var points = JSON.parse(this.responseText);
        console.log(points);
      }
    };
    xmlhttp.open("GET", baseURL+chartRefreshExtension, true);
    xmlhttp.send();
}
