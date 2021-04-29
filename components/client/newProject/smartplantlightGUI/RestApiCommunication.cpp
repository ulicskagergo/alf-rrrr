#include "RestApiCommunication.h"
#include <QJsonDocument>
#include <QJsonObject>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QNetworkAccessManager>
#include <iostream>

void RestApiCommunication::sendSettings(int sensitivity, int amount) {
    std::cout << "rest API post sent" << std::endl;
    /*
    QNetworkAccessManager *mgr = new QNetworkAccessManager(this);
    QString concatenatedUrl = url+"/settings";
    const QUrl url(concatenatedUrl);
    QNetworkRequest request(url);
    request.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");

    QJsonObject obj;
    obj["sensitivity"] = QString::number(sensitivity);
    obj["amount"] = QString::number(amount);
    QJsonDocument doc(obj);
    QByteArray data = doc.toJson();
    QNetworkReply *reply = mgr->post(request, data);

    QObject::connect(reply, &QNetworkReply::finished, [=](){
        if(reply->error() == QNetworkReply::NoError){
            QString contents = QString::fromUtf8(reply->readAll());
            qDebug() << contents;
        }
        else{
            QString err = reply->errorString();
            qDebug() << err;
        }
        reply->deleteLater();
    });
    */
}

void RestApiCommunication::saveLightSettings(int sensitivity, int amount) {
    std::cout << "saveLightSettings called" << std::endl;
    sendSettings(sensitivity, amount);
}
