import QtQuick 2.15
import QtQuick.Controls 2.5
import QtQuick.Layouts 1.3
import QtCharts 2.3

import 'RESTCalls.js' as RESTClient

Charts {
    anchors.fill: parent
    RowLayout {
        ColumnLayout {
            height: main.height-main.header.height
            width: main.width*2/3

            ChartView {
                id: sensorchart
                height: parent.height*0.5
                width: parent.width
                backgroundColor: "#ddf2e3"
                title: "Sensor values"
                antialiasing: true
                visible: true

                ValueAxis {
                    id: sensorAxisY
                    color: "black"
                    min: 0
                    max: 100
                }

                ValueAxis {
                    id: sensorAxisX
                    color: "black"
                    min: 0
                    max: 24
                }

                LineSeries {
                    id: lightValueSeries
                    axisX: sensorAxisX
                    axisY: sensorAxisY
                    name: "sensor values (0-100%)"
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
                height: parent.height*0.5
                width: parent.width
                backgroundColor: "#ddf2e3"
                title: "Light state"
                antialiasing: true
                visible: true

                ValueAxis {
                    id: lightAxisY
                    color: "black"
                    min: 0
                    max: 1
                }

                ValueAxis {
                    id: lightAxisX
                    color: "black"
                    min: 0
                    max: 24
                }

                LineSeries {
                    id: turnOnOffSeries
                    axisX: lightAxisX
                    axisY: lightAxisY
                    name: "turned on/off (1/0)"
                    color: "red"
                    pointLabelsVisible: false
                    pointsVisible: true
                }
                // custom signal, coming from the listview, when the active day is changed
                signal dateChanged(int index)
                onDateChanged: {
                    RESTClient.getPoints(index);
                }
            }

        }

        Item {
            width: listRectangle.width
            height: listRectangle.height
            ListModel {
                id: listmodel
            }

            Rectangle {
                id: listRectangle
                width: main.width/3
                height: (main.height-main.header.height)*0.9
                color: "transparent"

                ListView {
                    id: list
                    width: parent.width; height: parent.height
                    header: Text {
                        text: "Choose a day:"
                    }

                    Component {
                        id: contactsDelegate
                        Rectangle {
                            id: wrapper
                            width: parent.width
                            height: 60
                            color: ListView.isCurrentItem ? "#ddf2e3" : "lightgrey"
                            Text {
                                text: date
                                color: wrapper.ListView.isCurrentItem ? "black" : "white"
                                verticalAlignment: Text.AlignVCenter
                                height: parent.height
                                padding: 20
                            }
                            MouseArea {
                                anchors.fill: parent
                                onClicked: {
                                    list.currentIndex = index;
                                    lightchart.dateChanged(index);
                                }
                            }
                        }
                    }

                    model: listmodel
                    delegate: contactsDelegate

                    Component.onCompleted: {
                        RESTClient.getDates();
                    }
                }
            }

        }

    }
}
