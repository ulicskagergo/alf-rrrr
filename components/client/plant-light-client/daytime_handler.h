#ifndef DAYTIME_HANDLER_H
#define DAYTIME_HANDLER_H

#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QDebug>
#include <QQuickItem>
#include <string>
#include <stdio.h>

class DaytimeHandler : public QObject{
    Q_OBJECT

public slots:
    void daytimeSlot(const int msg);
};

#endif // DAYTIME_HANDLER_H
