package connect;

import java.util.SortedMap;

public class SocketMsgContent {
    private int msgType;
    //
    private int msgContentLen;
    //
    private byte[] msgContent;
    //
    public SocketMsgContent(int msgType, byte[] msgContent){
        this.msgType = msgType;
        this.msgContentLen = msgContent.length;
        this.msgContent = msgContent;
    }

}
