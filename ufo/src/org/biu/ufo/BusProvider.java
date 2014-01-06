package org.biu.ufo;

import com.squareup.otto.Bus;

public abstract class BusProvider {
	private static Bus INSTANCE = new Bus();

	public static synchronized Bus getEventBus() {
		return INSTANCE;
	}
}