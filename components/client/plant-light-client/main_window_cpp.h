#ifndef MAIN_WINDOW_CPP_H
#define MAIN_WINDOW_CPP_H

#include <QObject>
#include <QQuickItem>

class MainWindowCpp : public QObject
{
    Q_OBJECT

public:
    static QQuickItem* findItemByName(QObject *rootObject, const QString& name);
    static QQuickItem* findItemByName(QList<QObject*> nodes, const QString& name);
};
#endif // MAIN_WINDOW_CPP_H
