const baseURL = "http://localhost:8080"
const getPointsExtension = "/data"
const settingsUpdateExtension = "/settings" // both send and receive / GET and PUSH
const getDatesExtension = "/dates"

var pointsObj
var dates

// get data points on a given day ( dates[dateIndex] day )
function getPoints(dateIndex) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
          pointsObj = JSON.parse(xmlhttp.responseText);
          console.log(xmlhttp.responseText);

          // removing old points and adding the new ones
          lightValueSeries.removePoints(0, lightValueSeries.count);
          turnOnOffSeries.removePoints(0, turnOnOffSeries.count);
          for(var i in pointsObj) {
              var timeDate = new Date(pointsObj[i].measureDate);
              // the hours and minutes are represented as a fraction between 0 and 24 (e.g. 14:30 -> 14.5)
              var x = timeDate.getHours() + timeDate.getMinutes()*1/60;
              console.log("x: " + x + ", actual_value: " + pointsObj[i].actualValue);
              lightValueSeries.append(x, pointsObj[i].actualValue);
              turnOnOffSeries.append(x, pointsObj[i].isOn===true ? 1:0);
          }

      }
    };
    console.log(dates);
    var dateToSend = dates[dateIndex];
    xmlhttp.open("GET", baseURL+getPointsExtension+"/"+dateToSend.toISOString().split("T")[0], true);
    xmlhttp.send();

    ///// TEST CODE
    /*
    console.log("called getPoints")
    var pointsJson;
    pointsJson = '{'
        +'"points":'
        +'[{"is_on":true, "actual_value":10, "time":"2021-04-30T13:32:56.675310"},'
        +'{"is_on":true, "actual_value":10, "time":"2021-04-30T14:32:56.675310"},'
        +'{"is_on":false, "actual_value":80, "time":"2021-04-30T15:33:36.675310"}'
    +']}';
    */
    ///// TEST CODE END

}


function getDates() {
    ///// TEST CODE
    /*
    console.log("called getDates")
    var datesJson = '[ "2021-04-23", "2021-04-24", "2021-04-26" ]';
    */
    ///// TEST CODE END
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
          console.log("dates received");
          var stringDates = JSON.parse(xmlhttp.responseText);
          if(listmodel.count>0) {
              listmodel.remove(0, listmodel.count);
          }

          dates = [];
          for(var i in stringDates) {
              dates[i] = new Date(stringDates[i]);
              var dict = { "date": dates[i].toDateString() }
              console.log(dict["date"]);
              listmodel.append(dict);
          }
      }
    };
    xmlhttp.open("GET", baseURL + getDatesExtension, true);
    xmlhttp.send();
}

function pushSettings(sensorSensitivity, from, to) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
        console.log("settings push successful");
        console.log(xmlhttp.responseText);
      }
    };
    xmlhttp.open('POST', baseURL + settingsUpdateExtension, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    var newSettingsObj = {
        sensitivity:sensorSensitivity, // e.g. 80
        from: from, // e.g. 15:00
        to: to // e.g. 19:00
    }

    xmlhttp.send(JSON.stringify(newSettingsObj));
    console.log(JSON.stringify(newSettingsObj));
    ///// TEST CODE
    /*
    console.log("pushSettings: sens:" + sensorSensitivity + ", from-to: " + from + "," + to);
    var newSettingsObj = {
        sensitivity:sensorSensitivity, // e.g. 80
        from: from, // e.g. 15:00
        to: to // e.g. 19:00
    }
    console.log("pushSettings sent: " + JSON.stringify(newSettingsObj));
    */
    ///// TEST CODE END
}

// received: { "sensitivity": sensorSensitivity, "from": "13:00", "to":"20:00" }
function pullSettings() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
        var settingsObj = JSON.parse(xmlhttp.responseText);
        console.log(settingsObj);
        lightSlider.value = settingsObj.sensitivity;
        var fromto = settingsObj.from + " - " + settingsObj.to;
        fromtoText.text = fromto;
      }
    };
    xmlhttp.open("GET", baseURL + settingsUpdateExtension, true);
    xmlhttp.send();
    console.log("sent");

    ///// TEST CODE
    /*
    console.log("pullSettings called");
    var received = '{ "sensitivity": 80, "from": "13:00", "to":"20:00" }';
    var settingsObj = JSON.parse(received);
    lightSlider.value = settingsObj.sensitivity;
    var fromto = settingsObj.from + " - " + settingsObj.to;
    fromtoText.text = fromto;
    */
    ///// TEST CODE END
}
