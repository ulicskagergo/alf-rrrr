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
                height: parent.height*0.5
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

        Item {
            width: main.width/3
            height: main.height-main.header.height
            ListModel {
                id: listmodel
                ListElement {
                    name: "Bill Smith"
                    number: "555 3264"
                }
                ListElement {
                    name: "John Brown"
                    number: "555 8426"
                }
                ListElement {
                    name: "Sam Wise"
                    number: "555 0473"
                }
            }

            Rectangle {
                width: 180; height: 200

                Component {
                    id: contactDelegate
                    Item {
                        width: 180; height: 40
                        Column {
                            Text { text: '<b>Name:</b> ' + name }
                            Text { text: '<b>Number:</b> ' + number }
                        }
                    }
                }

                ListView {
                    width: 180; height: 200

                    Component {
                        id: contactsDelegate
                        Rectangle {
                            id: wrapper
                            width: 180
                            height: contactInfo.height
                            color: ListView.isCurrentItem ? "black" : "red"
                            Text {
                                id: contactInfo
                                text: name + ": " + number
                                color: wrapper.ListView.isCurrentItem ? "red" : "black"
                            }
                        }
                    }

                    model: listmodel
                    delegate: contactsDelegate
                    focus: true
                }
            }

        }


    // TODO ListView
    }
}
