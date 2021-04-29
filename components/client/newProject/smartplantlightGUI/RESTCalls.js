const baseURL = "localhost"
const chartRefreshExtension = "/fullRefresh"
const settingsUpdateExtension = "/settings"

var pointsObj

function getPoints() {
    console.log("called getPoints")
    /*
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        var points = JSON.parse(this.responseText);
        console.log(points);
      }
    };
    xmlhttp.open("GET", baseURL+chartRefreshExtension, true);
    xmlhttp.send();
    */
    var pointsJson;
    pointsJson = '{'
        +'"points":'
        +'[{"x":1.2,"y":20,"light":"on"},'
        +'{"x":2.3,"y":40,"light":"off"},'
        +'{"x":3.5,"y":10,"light":"off"},'
        +'{"x":4.2,"y":30,"light":"off"}]'
    +'}';
    pointsObj = JSON.parse(pointsJson);

    // removing old points and adding the new ones
    lightValueSeries.removePoints(0, lightValueSeries.count);
    turnOnOffSeries.removePoints(0, turnOnOffSeries.count);
    for(var i in pointsObj.points) {
        console.log("x: " + pointsObj.points[i].x + ", y: " + pointsObj.points[i].y);
        lightValueSeries.append(pointsObj.points[i].x, pointsObj.points[i].y);
        turnOnOffSeries.append(pointsObj.points[i].x, pointsObj.points[i].light=="on"?1:0);
    }
}
