const baseURL = "localhost:8080"
const getPointsExtension = "/data"
const settingsUpdateExtension = "/settings" // both send and receive / GET and PUSH
const getDatesExtension = "/dates"

var pointsObj
var dates

// get data points on a given day ( dates[dateIndex] day )
function getPoints(dateIndex) {
/*
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        var points = JSON.parse(this.responseText);
        console.log(points);
      }
    };
    var dateToSend = dates[dateIndex];
    xmlhttp.open("GET", baseURL+chartRefreshExtension+"/"+dateToSend, true);
    xmlhttp.send();
*/

    ///// TEST CODE
    console.log("called getPoints")
    var pointsJson;
    pointsJson = '{'
        +'"points":'
        +'[{"is_on":true, "actual_value":10, "time":"2021-04-30T13:32:56.675310"},'
        +'{"is_on":true, "actual_value":10, "time":"2021-04-30T14:32:56.675310"},'
        +'{"is_on":false, "actual_value":80, "time":"2021-04-30T15:33:36.675310"}'
    +']}';
    ///// TEST CODE END

    pointsObj = JSON.parse(pointsJson);

    // removing old points and adding the new ones
    lightValueSeries.removePoints(0, lightValueSeries.count);
    turnOnOffSeries.removePoints(0, turnOnOffSeries.count);
    for(var i in pointsObj.points) {
        var timeDate = new Date(pointsObj.points[i].time);
        // the hours and minutes are represented as a fraction between 0 and 24 (e.g. 14:30 -> 14.5)
        var x = timeDate.getHours() + timeDate.getMinutes()*1/60;
        console.log("x: " + x + ", actual_value: " + pointsObj.points[i].actual_value);
        lightValueSeries.append(x, pointsObj.points[i].actual_value);
        turnOnOffSeries.append(x, pointsObj.points[i].is_on===true ? 1:0);
    }
}


function getDates() {
    ///// TEST CODE
    console.log("called getDates")
    var datesJson = '[ "2021-04-23T00:00:00.000", "2021-04-24T00:00:00.000", "2021-04-26T01:00:00.000" ]';
    ///// TEST CODE END

    dates = JSON.parse(datesJson);
    if(listmodel.count>0) {
        listmodel.remove(0, listmodel.count);
    }

    for(var i in dates) {
        var date = new Date(dates[i]);
        var dict = { "date": date.toDateString() }
        console.log(dict["date"]);
        listmodel.append(dict);
    }
}

function pushSettings(sensorSensitivity, from, to) {
/*
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        // var points = JSON.parse(this.responseText);
        // console.log(points);
      }
    };
    xmlhttp.open("POST", baseURL+chartRefreshExtension, true);

    var settingsObj = {
        sensitivity:sensorSensitivity, // e.g. 80
        from: fromHour + ":" + fromMin, // e.g. 15:00
        to: toHour + ":" + toMin // e.g. 19:00
    }

    xmlhttp.send(JSON.stringify(settingsObj));
*/

    ///// TEST CODE
    console.log("pushSettings: sens:" + sensorSensitivity + ", from-to: " + from + "," + to);
    var settingsObj = {
        sensitivity:sensorSensitivity, // e.g. 80
        from: from, // e.g. 15:00
        to: to // e.g. 19:00
    }
    console.log("pushSettings sent: " + JSON.stringify(settingsObj));
    ///// TEST CODE END
}

// received: { "sensitivity": sensorSensitivity, "from": "13:00", "to":"20:00" }
function pullSettings() {
    /*
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState === 4 && this.status === 200) {
        var points = JSON.parse(this.responseText);
        console.log(points);
      }
    };
    xmlhttp.open("GET", baseURL+chartRefreshExtension, true);
    xmlhttp.send();
    */

    ///// TEST CODE
    console.log("pullSettings called");
    var received = '{ "sensitivity": 80, "from": "13:00", "to":"20:00" }';
    var settingsObj = JSON.parse(received);
    lightSlider.value = settingsObj.sensitivity;
    var fromto = settingsObj.from + " - " + settingsObj.to;
    fromtoText.text = fromto;
    ///// TEST CODE END
}
