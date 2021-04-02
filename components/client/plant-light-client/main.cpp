#include "main.h"

int main(int argc, char *argv[])
{

    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    engine.load(QUrl(QStringLiteral("qrc:/main.qml")));

    QObject* rbuttons= MainWindowCpp::findItemByName(engine.rootObjects()[0], QString("ThresholdRadioList"));

    ChangeHandler sh;

    QObject::connect(rbuttons, SIGNAL(thresholdChanged(int)),
            &sh, SLOT(thresholdSlot(int)));


    return app.exec();
}
