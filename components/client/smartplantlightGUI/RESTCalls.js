var baseURL = "http://localhost:8080"
const getPointsExtension = "/data"
const settingsUpdateExtension = "/settings" // both send and receive / GET and PUSH
const getDatesExtension = "/dates"

var pointsObj
var dates

function setIP(ipaddress) {
    console.log(ipaddress)
    if(ipaddress!="") {
        baseURL = "http://" + ipaddress + ":8080";
    }
    console.log("URL is set to: " + baseURL);
}

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
    var dateToSend = dates[dateIndex];
    xmlhttp.open("GET", baseURL+getPointsExtension+"/"+dateToSend.toISOString().split("T")[0], true);
    xmlhttp.send();

}


function getDates() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
          console.log("dates received: " + xmlhttp.responseText);
          var stringDates = JSON.parse(xmlhttp.responseText);
          if(listmodel.count>0) {
              listmodel.remove(0, listmodel.count);
          }

          dates = [];
          for(var i in stringDates) {
              dates[i] = new Date(stringDates[i]);
              var dict = { "date": dates[i].toDateString() }
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
      }
    };
    xmlhttp.open('POST', baseURL + settingsUpdateExtension, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    var newSettingsObj = {
        sensitivity:sensorSensitivity, // e.g. 80
        from: from, // e.g. 15:00
        to: to // e.g. 19:00
    }

    console.log("Sending settings:" + JSON.stringify(newSettingsObj));
    xmlhttp.send(JSON.stringify(newSettingsObj));
}

// received: { "sensitivity": sensorSensitivity, "from": "13:00", "to":"20:00" }
function pullSettings() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
        var settingsObj = JSON.parse(xmlhttp.responseText);
        console.log("Pulling settings: " + xmlhttp.responseText);
        lightSlider.value = settingsObj.sensitivity;
        var fromto = settingsObj.from + " - " + settingsObj.to;
        fromtoText.text = fromto;
      }
    };
    xmlhttp.open("GET", baseURL + settingsUpdateExtension, true);
    xmlhttp.send();
}
