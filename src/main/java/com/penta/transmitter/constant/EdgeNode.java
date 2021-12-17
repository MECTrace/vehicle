package com.penta.transmitter.constant;

public enum EdgeNode {

    EDGE_NODE_1("20.194.98.12"),
    EDGE_NODE_2("52.141.2.70"),
    EDGE_NODE_3("52.141.1.55"),
    EDGE_NODE_4("20.41.96.101"),
    EDGE_NODE_5("20.41.102.11"),
    EDGE_NODE_6("52.231.98.206"),
    EDGE_NODE_7("20.194.20.219"),
    EDGE_NODE_8("52.141.61.111"),
    EDGE_NODE_9("52.141.61.52"),
    EDGE_NODE_10("52.141.61.54");

    private final String IP;
    EdgeNode(String value) {
        this.IP = value;
    }
    public String getIP() {return IP;}

}
