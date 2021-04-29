QT += charts
QT += qml
QT += quick

SOURCES += \
    RestApiCommunication.cpp \
    main.cpp

target.path = .
INSTALLS += target

HEADERS += \
    RestApiCommunication.h

DISTFILES += \
    Charts.qml \
    SensorChart.qml \
    Settings.qml \
    SettingsScreen.qml \
    StatisticsScreen.qml \
    bg-plants.jpg \
    main.qml

RESOURCES += \
    qml.qrc
