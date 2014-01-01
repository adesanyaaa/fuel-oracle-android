package org.biu.ufo;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.biu.ufo.connection.Connection;
import org.biu.ufo.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.obd.commands.IObdCommand;
import org.biu.ufo.obd.commands.fuel.FuelLevelObdCommand;
import org.biu.ufo.obd.commands.protocol.EchoOffObdCommand;
import org.biu.ufo.obd.commands.protocol.LineFeedOffObdCommand;
import org.biu.ufo.obd.commands.protocol.ObdResetCommand;
import org.biu.ufo.obd.commands.protocol.SelectProtocolObdCommand;
import org.biu.ufo.obd.commands.protocol.TimeoutObdCommand;
import org.biu.ufo.obd.enums.ObdProtocols;
import org.biu.ufo.openxc.sources.ObdDataSource;
import org.biu.ufo.services.BoundedWorkerService;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.openxc.VehicleManager;
import com.openxc.measurements.FuelLevel;

/**
 * CarGatewayService handles all communication with the Car
 * 
 * Plan:
 *  Implement as an OBD poller that injects OpenXC messages so we can 
 *   use openxc.VehicleManager as single entry point for car data.
 *   
 * 	Need to implement configuration screen to initialize connection parameters.
 * 
 * @author Roee Shlomo
 *
 */
public class CarGatewayService extends BoundedWorkerService {
	private final static String TAG = "CarGatewayService";
	
	private final IBinder binder = new CarGatewayServiceBinder();

	private ObdDataSource dataSource; // TODO: initialize! (use inject)
	private Connection connection; // TODO: initialize! (use inject)
	private BlockingQueue<IObdCommand> jobsQueue = new LinkedBlockingQueue<IObdCommand>();

    public CarGatewayService() {
		super(TAG);
	}
    
    @Override
    public void onCreate() {
    	// TODO Create data source and add it to singleton VehicleManager
    	super.onCreate();
    }

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void startConnection() {
		jobsQueue.clear();
		jobsQueue.add(new ObdResetCommand());
		jobsQueue.add(new EchoOffObdCommand());	
		jobsQueue.add(new EchoOffObdCommand());	// not a mistake
		jobsQueue.add(new LineFeedOffObdCommand());
		jobsQueue.add(new TimeoutObdCommand(62));
		jobsQueue.add(new SelectProtocolObdCommand(ObdProtocols.AUTO));	// For now set protocol to AUTO

		// Just for getting some data
		addQuery(new FuelLevelObdCommand());
	}
	
	public void addQuery(BaseObdQueryCommand cmd) {
		jobsQueue.add(cmd);
		
		if(jobsQueue.size() == 1) {
			executeOnBackground();			
		}
	}

	private void executeOnBackground() {
		runOnBackground(new Runnable() {
			
			@Override
			public void run() {
				try {
					executeQueue();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}


	private void executeQueue() throws InterruptedException, IOException {
		while (!jobsQueue.isEmpty()) {
			final IObdCommand job = jobsQueue.take();
			if(write(job) && receive(job)) {
				handleMeasurement(job);
			}
		}
	}

	private void handleMeasurement(final IObdCommand job) {
		runOnForground(new Runnable() {
			@Override
			public void run() {
				if(job instanceof FuelLevelObdCommand) {
					dataSource.notifyMeasurement(new FuelLevel(((FuelLevelObdCommand) job).getValue()).toRaw());
				}				
			}
		});
	}

	private boolean write(IObdCommand job) throws IOException, InterruptedException {
		boolean success = connection.write((job.getCommand() + "\r").getBytes());
		Thread.sleep(200);
		return success;
	}

	private boolean receive(IObdCommand job) throws IOException {
		// Read from connection
		byte[] bytes = new byte[256];
		int byteCount = connection.read(bytes);

		// Get raw results
		String[] rawResults = new String(bytes, 0, byteCount).split(">");
		for(String rawResult : rawResults) {
			if(job.handleResult(rawResult)) {
				return true;
			}
		}
		
		return false;
	}
	
	public class CarGatewayServiceBinder extends Binder {
		public CarGatewayService getService() {
			return CarGatewayService.this;
		}
	}
	
}
