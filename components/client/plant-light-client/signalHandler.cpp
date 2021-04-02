#include "signal_handler.h"

void ChangeHandler::thresholdSlot(const int msg){

    qDebug()<<"Called threshold slot with value: " << msg;
    std::cout << "thresholdSlot called"<< msg;
    //TODO communication


}


