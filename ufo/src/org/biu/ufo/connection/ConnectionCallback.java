package org.biu.ufo.connection;


public interface ConnectionCallback {
	/**
	 * The data source is connected, so if necessary, keep the device awake.
	 */
	public void sourceConnected(Connection source);

	/**
	 * The data source is connected, so if necessary, let the device go to
	 * sleep.
	 */
	public void sourceDisconnected(Connection source);

}
