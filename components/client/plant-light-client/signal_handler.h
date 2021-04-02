#ifndef SIGNAL_HANDLER_H
#define SIGNAL_HANDLER_H

#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QDebug>
#include <QQuickItem>
#include <iostream>

class ChangeHandler : public QObject{
    Q_OBJECT

public slots:
    void thresholdSlot(const int msg);
};

#endif // SIGNAL_HANDLER_H
