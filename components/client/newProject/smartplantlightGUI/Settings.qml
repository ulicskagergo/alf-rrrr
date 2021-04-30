import QtQuick 2.0
import QtQuick.Controls 2.5
import QtQuick.Layouts 1.3

import 'RESTCalls.js' as RESTClient

Item {
    anchors.fill: parent

    objectName: "Settings"

    Rectangle { // background color
        id: rectangle
        width: lightSettings.width+50
        height: lightSettings.height+50
        border.color: "#ddf2e3"
        border.width: 8
        color: "white"

        ColumnLayout {
            id: lightSettings
            width: parent.fill
            anchors.centerIn: rectangle
            spacing: 10

            ColumnLayout {
                /*
                Rectangle { // background color
                    width: parent.width
                    height: parent.height
                    color: "white"
                }
                */
                Text{
                    font.bold: true
                    text: "System should be turned on between:"
                }

                RowLayout {
                    TextInput {
                        id: fromtoText
                        inputMask: "99:99 - 99:99"//input mask
                        text: "07:00 - 19:00" //default text
                        inputMethodHints: Qt.ImhDigitsOnly
                        validator: RegExpValidator { regExp: /^([0-1\s]?[0-9\s]|2[0-3\s]):([0-5\s][0-9\s]):([0-5\s][0-9\s])$ / }
                    }


                }

            }

            ColumnLayout {
                /*
                Rectangle { // background color
                    width: parent.width
                    height: parent.height
                    color: "white"
                }
                */

                Text {
                    font.bold: true
                    text: "Light sensitivity\n(0% - light always on, 100% - light always off):"
                }
                RowLayout {
                    Slider {
                        id: lightSlider
                        from: 0
                        value: 50
                        to: 100
                        stepSize: 10
                    }

                    Text {
                        text: lightSlider.value + "%"
                    }
                }
            }
        }
    }

    Button {
        id: saveButton
        anchors.top: rectangle.bottom
        anchors.topMargin: 10
        anchors.bottomMargin: 10
        text: "Save"

        Rectangle { // background color
            width: parent.width
            height: parent.height
            color: "#ddf2e3"
        }

        onClicked: {
            console.log("Save button pushed")
            RESTClient.pushSettings(lightSlider.value, fromtoText.text)
            // main.restApiCommunication.saveLightSettings(parseInt(daytimeText.text), lightSlider.value)
        }
    }
}
