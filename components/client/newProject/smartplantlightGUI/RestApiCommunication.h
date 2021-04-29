#ifndef RESTAPICOMMUNICATION_H
#define RESTAPICOMMUNICATION_H

#include <QObject>
#include <iostream>

class RestApiCommunication : public QObject
{
    Q_OBJECT
private:
    QString url;
    // sends a POST with a JSON body containing updated settings
    void sendSettings(int sensitivity, int amount);

    void requestCurrentState() {
        std::cout << "REST API state requested" << std::endl;
    }
public:
    RestApiCommunication(QString url) {
        this->url = url;
    }
    // sends the updated light settings to the server

    Q_INVOKABLE void saveLightSettings(int sensitivity, int amount);

    // Q_INVOKABLE void getCurrentState();

    // Q_INVOKABLE void refreshChart();
};

#endif // RESTAPICOMMUNICATION_H
