package org.biu.ufo.control.analyzers;

import org.biu.ufo.control.Controller;

public interface IAnalyzer {
	void setController(Controller controller);
	void start();
	void stop();
}
