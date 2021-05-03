import QtQuick 2.0
import QtQuick.Controls 2.5
import QtQuick.Layouts 1.3

import 'RESTCalls.js' as RESTClient

Dialog {
    id: about
    width: 320
    height: 200
    modal: true
    focus: true
    anchors.centerIn: Overlay.overlay
    standardButtons: Dialog.Ok
    title: "Set address"
    visible: true
    ColumnLayout {
        anchors.fill: parent
        spacing: 10
        Text {
            id: ipText
            text: "Input the address of the server (or even localhost)\n(and make sure it is up and running!)"
        }
        TextField {
            focus: true
            visible: true
            id: ip
        }
    }
    onClosed: {
        RESTClient.setIP(ip.text);
    }

    closePolicy: Dialog.OK
}
