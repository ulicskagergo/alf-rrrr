#include "main.h"

int main(int argc, char *argv[])
{

    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    engine.load(QUrl(QStringLiteral("qrc:/main.qml")));

    QObject* rbuttons= MainWindowCpp::findItemByName(engine.rootObjects()[0], QString("ThresholdRadioList"));


    ThresholdHandler sh;
    DaytimeHandler dh;

        QObject::connect(rbuttons, SIGNAL(thresholdChanged(int)),
                &sh, SLOT(thresholdSlot(int)));

        QObject::connect(rbuttons, SIGNAL(daytimeChanged(int)),
                &dh, SLOT(daytimeSlot(int)));



    return app.exec();
}
