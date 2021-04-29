import QtQuick 2.0
import QtQuick.Controls 2.5
import QtQuick.Layouts 1.3

Item {
    anchors.fill: parent

    objectName: "Settings"

    signal thresholdChanged(msg: int)
    signal daytimeChanged(msg: int)

    property int selectedThreshold: 0

    function selectThreshold(th){
        selectedThreshold=th;
        console.log("selectedthreshold called")

        thresholdChanged(th);
    }

    function changeDaytime(dt){
        console.log("changedaytime called")
        daytimeChanged(dt)

    }

    RowLayout {
        id: lightSettings
        width: parent.fill
        spacing: 10

        GroupBox {
            // width: parent/3
            ColumnLayout{

                Text{
                    text: "Amount of light per day:"
                }

                TextField {
                    id: daytimeText
                    validator: IntValidator {bottom: 0; top: 14;}
                    placeholderText: "Enter value in hours"
                }

            }
        }

        GroupBox {
            // width: parent/3
            ColumnLayout {
                Text {
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
        anchors.top: lightSettings.bottom
        anchors.topMargin: 10
        anchors.bottomMargin: 10
        text: "Save"
        onClicked: {
            console.log("Save button pushed")
            // main.restApiCommunication.saveLightSettings(parseInt(daytimeText.text), lightSlider.value)
        }
    }

}
