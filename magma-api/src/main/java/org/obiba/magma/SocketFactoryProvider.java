package org.obiba.magma;

import javax.net.SocketFactory;

/**
 * Delegate socket factory to client application.
 */
public interface SocketFactoryProvider {

    SocketFactory getSocketFactory();

}
