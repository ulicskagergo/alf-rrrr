import QtQuick 2.15
import QtQuick.Controls 2.5
import QtQuick.Window 2.12
import QtQuick.Layouts 1.3

ApplicationWindow {
    background: bgPlants
    Image {
        id: bgPlants
        source: "bg-plants.jpg"
    }
    id: main
    width: 1280
    height: 720
    visible: true
    title: qsTr("Smart Plant Light")

    Loader { id: pageLoader }

    Dialog {
        id: about
        width: 320
        height: 140
        modal: true
        focus: true
        anchors.centerIn: Overlay.overlay
        standardButtons: Dialog.Ok
        title: "About"
        contentItem: ColumnLayout {
            Text {
                text: "Smart Plant Light v1.0"
            }
            Text {
                id: link_Text
                text: '<html><style type="text/css"></style><a href="https://github.com/alkalmazasfejlesztes/hf2021-rrrr\">Github</a></html>'
                onLinkActivated: Qt.openUrlExternally(link)
            }
        }
        closePolicy: Popup.CloseOnEscape | Dialog.OK
    }

    header: MenuBar {
        Menu {
            title: qsTr("&View")
            Action {
                text: qsTr("&Statistics")
                onTriggered: pageLoader.source = "StatisticsScreen.qml"
            }
            Action {
                text: qsTr("&Settings")
                onTriggered: pageLoader.source = "SettingsScreen.qml"
            }
            MenuSeparator {}
            Action {
                text: qsTr("&Quit")
                onTriggered: Qt.quit()
            }
        }
        Menu {
            title: qsTr("&Help")
            Action {
                text: qsTr("&About")
                onTriggered: about.open()
            }
        }
    }

}
