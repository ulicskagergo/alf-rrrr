import QtQuick 2.0
import QtQuick.Controls 2.5
import QtQuick.Layouts 1.3
import QtCharts 2.3

import 'RESTCalls.js' as RESTClient

ColumnLayout {
    height: parent.height
    width: parent.width*2/3

    ChartView {
        id: sensorchart
        height: parent.height*0.4
        width: parent.width
        backgroundColor: "lightgrey"
        title: "Sensor values"
        antialiasing: true
        visible: true

        ValueAxis {
            id: sensorAxisY
            min: 0
            max: 100
        }

        ValueAxis {
            id: sensorAxisX
            min: 0
            max: 24
        }

        LineSeries {
            id: lightValueSeries
            axisX: sensorAxisX
            axisY: sensorAxisY
            name: "Light values"
            color: "blue"
            onPointAdded: {
                console.log("point added");
            }
            pointLabelsVisible: false
            pointsVisible: true
        }
    }

    ChartView {
        id: lightchart
        height: parent.height*0.4
        width: parent.width
        backgroundColor: "lightgrey"
        title: "Light state"
        antialiasing: true
        visible: true

        ValueAxis {
            id: lightAxisY
            min: 0
            max: 1
        }

        ValueAxis {
            id: lightAxisX
            min: 0
            max: 24
        }

        LineSeries {
            id: turnOnOffSeries
            axisX: lightAxisX
            axisY: lightAxisY
            name: "Light state"
            color: "red"
            pointLabelsVisible: false
            pointsVisible: true
        }
        Component.onCompleted: {
            RESTClient.getPoints()
        }

    }

}
