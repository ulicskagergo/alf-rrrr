import QtQuick 2.0
import QtQuick.Controls 1.3
import QtQuick.Layouts 1.1

Item {
    id: thradio
    anchors.fill: parent

    objectName: "ThresholdRadioList"

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

    ColumnLayout{
        GroupBox{
            ExclusiveGroup { id: radioButtonExclusiveGroup }

            RowLayout{
                anchors.fill: parent

                RadioButton {
                    id: radioButton1
                    text: "1"
                    exclusiveGroup: radioButtonExclusiveGroup
                    onClicked:{
                        selectThreshold(1)
                    }
                }

                RadioButton {
                    id: radioButton2
                    text: "2"
                    exclusiveGroup: radioButtonExclusiveGroup
                    onClicked:{
                        selectThreshold(2)
                    }
                }
                RadioButton {
                    id: radioButton3
                    text: "3"
                    exclusiveGroup: radioButtonExclusiveGroup
                    onClicked:{
                        selectThreshold(3)
                    }
                }
                RadioButton {
                    id: radioButton4
                    text: "4"
                    exclusiveGroup: radioButtonExclusiveGroup
                    onClicked:{
                        selectThreshold(4)
                    }
                }
            }

        }


        GroupBox{

            ColumnLayout{

                Text{
                    text: "Set the amount of daytime per day in hours:"
                }

                TextField{
                    id: daytimeText
                    validator: IntValidator {bottom: 0; top: 24;}
                }

                Button{
                    id: dtButton
                    text: "OK"
                    onClicked: {
                        console.log("changedaytime called")

                        changeDaytime(parseInt(daytimeText.text))
                    }
                }

            }
        }

    }







}
