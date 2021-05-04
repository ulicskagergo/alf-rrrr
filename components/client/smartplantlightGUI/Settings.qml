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
                Text{
                    font.bold: true
                    text: "System should be turned on between:"
                }

                TextInput {
                    id: fromtoText
                    inputMask: "99:99 - 99:99"// input mask
                    text: "00:00 - 00:00" // default text
                    inputMethodHints: Qt.ImhDigitsOnly
                    validator: RegExpValidator { regExp: /^([0-1\s]?[0-9\s]|2[0-3\s]):([0-5\s][0-9\s]) - ([0-1\s]?[0-9\s]|2[0-3\s]):([0-5\s][0-9\s])$ / }
                    onFocusChanged: {
                        if(focus===true) {
                            cursorPosition = 0;
                        }
                    }
                }

                Text {
                    id: errorMsg
                    font.italic: true
                    color: "darkgrey"
                    text: "Invalid input: time of system turning on must be earlier than time of turning off"
                    visible: false
                }

            }

            ColumnLayout {
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
            Component.onCompleted: {
                RESTClient.pullSettings();
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
            errorMsg.visible = false;
            console.log("Save button pushed");
            // validate that from is earlier than to
            var fromHour = parseInt(fromtoText.text.split(" ")[0].split(":")[0]);
            var toHour = parseInt(fromtoText.text.split(" ")[2].split(":")[0]);
            var fromMin = parseInt(fromtoText.text.split(" ")[0].split(":")[1]);
            var toMin = parseInt(fromtoText.text.split(" ")[2].split(":")[1]);
            if((fromHour < toHour) || ( fromHour === toHour && fromMin < toMin )) {
                RESTClient.pushSettings(lightSlider.value, fromtoText.text.split(" - ")[0], fromtoText.text.split(" - ")[1]);
            } else {
                errorMsg.visible = true;
            }

        }
    }
}
