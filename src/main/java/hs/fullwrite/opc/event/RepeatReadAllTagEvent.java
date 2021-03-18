package hs.fullwrite.opc.event;

import hs.fullwrite.opc.OpcExecute;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/13 17:54
 */
public class RepeatReadAllTagEvent extends BaseEvent {
    public RepeatReadAllTagEvent() {
        super(true);
    }

    @Override
    public void execute(OpcExecute opcExecute) {

    }
}
