package com.twisted.net.msg;

/**
 * Messages are Transmissions that are intended to be passed to classes outside the network
 * sector. The alternative is Transmissions that are handled completely by the network sector and
 * never used outside it.
 */
public interface Message extends Transmission {

}
