package com.twisted.net.msg;

/**
 * Messages are Transmissions that are intended to be passed to classes outside of the network
 * sector. The alternative is Messages that are handled completely by the network sector and never
 * used outside of it.
 */
public interface Message extends Transmission {

}
