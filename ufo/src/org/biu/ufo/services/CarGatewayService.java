package org.biu.ufo.services;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.commands.IObdCommand;
import org.biu.ufo.car.obd.commands.SpeedObdCommand;
import org.biu.ufo.car.obd.commands.engine.EngineRPMObdCommand;
import org.biu.ufo.car.obd.commands.fuel.FuelLevelObdCommand;
import org.biu.ufo.car.obd.commands.protocol.EchoOffObdCommand;
import org.biu.ufo.car.obd.commands.protocol.LineFeedOffObdCommand;
import org.biu.ufo.car.obd.commands.protocol.ObdResetCommand;
import org.biu.ufo.car.obd.commands.protocol.SelectProtocolObdCommand;
import org.biu.ufo.car.obd.commands.protocol.TimeoutObdCommand;
import org.biu.ufo.car.obd.connection.BluetoothConnection;
import org.biu.ufo.car.obd.connection.Connection;
import org.biu.ufo.car.obd.connection.ConnectionCallback;
import org.biu.ufo.car.obd.enums.ObdProtocols;
import org.biu.ufo.car.openxc.VehicleManagerConnector;
import org.biu.ufo.car.openxc.VehicleManagerConnector.VehicleManagerConnectorCallback;
import org.biu.ufo.car.openxc.sources.ObdDataSource;
import org.biu.ufo.control.events.connection.ObdConnectionLostMessage;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.VehicleSpeed;

/**
 * CarGatewayService handles all communication with the Car
 * 
 * Plan:
 *  Implement as an OBD poller that injects OpenXC messages so we can 
 *   use openxc.VehicleManager as single entry point for car data.
 *   
 * 	Need to implement configuration screen to initialize connection parameters.
 * 
 * @author Roee Shlomo, pires(android-obd-reader)
 *
 */
@EService
public class CarGatewayService extends BoundedWorkerService implements ConnectionCallback {
	private final static String TAG = "CarGatewayService";

	@Bean
	ObdDataSource vmCustomDataSource;
	
	@Bean
	VehicleManagerConnector vmConnector;
  
	@Bean
	OttoBus bus;
	
	private final IBinder binder = new CarGatewayServiceBinder();
	private Connection connection;
	
	private final BlockingQueue<IObdCommand> jobsQueue = new LinkedBlockingQueue<IObdCommand>();
	private final AtomicBoolean isQueueRunning = new AtomicBoolean(false);
	private final AtomicBoolean shouldBeActive = new AtomicBoolean(false);
	private final AtomicBoolean isActive = new AtomicBoolean(false);
	
	/**
	 * Constructor
	 */
	public CarGatewayService() {
		super(TAG);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		vmConnector.bindToVehicleManager(new VehicleManagerConnectorCallback() {
			@Override
			public void onVMDisconnected() {
				Log.d(TAG, "VehicleManager disconnected");
			}
			
			@Override
			public void onVMConnected() {
				vmConnector.getVehicleManager().addSource(vmCustomDataSource);				
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(vmConnector.getVehicleManager() != null) {
			vmConnector.getVehicleManager().removeSource(vmCustomDataSource);			
		}
		vmConnector.unbindFromVehicleManager();
	}

	public boolean start(String deviceAddress) {
		// Stop old connection
		stop();
		
		// Create new connection
		try {
			connection = new BluetoothConnection(this, this, deviceAddress);
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		
		// Start connection
		shouldBeActive.set(true);
		runOnBackground(new Runnable() {
			@Override
			public void run() {
				if(shouldBeActive.get()) {
					connection.start();			
				}
			}
		});
		
		return true;
	}
	
	public void stop() {
		shouldBeActive.set(false);
		
		getServiceHandler().removeCallbacks(mQueueCommands);

		if(connection != null) {
			connection.stop();
			connection = null;
		}
		
		isActive.set(false);
	}

	@Override
	public void sourceConnected(Connection source) {
		if(source == connection && shouldBeActive.get()) {
			initializeDevice();
			runOnBackground(mQueueCommands);
		}
	}

	@Override
	@UiThread
	public void sourceDisconnected(Connection source) {
		if(source == connection) {
			Log.e(TAG, "Connection lost to " + source.toString());
			if(shouldBeActive.get()) {
				stop();
				bus.post(new ObdConnectionLostMessage());
			}
		}
	}

	public void initializeDevice() {
		jobsQueue.clear();
		jobsQueue.add(new ObdResetCommand());
		jobsQueue.add(new EchoOffObdCommand());	
		jobsQueue.add(new EchoOffObdCommand());	// not a mistake
		jobsQueue.add(new LineFeedOffObdCommand());
		jobsQueue.add(new TimeoutObdCommand(62));
		jobsQueue.add(new SelectProtocolObdCommand(ObdProtocols.AUTO));	// For now set protocol to AUTO

		isActive.set(true);
		
		// Just for getting some data
		addQuery(new FuelLevelObdCommand());
	}

	public void addQuery(BaseObdQueryCommand cmd) {
		jobsQueue.add(cmd);
		
		if(!isQueueRunning.get()) {
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
	}

	private void executeQueue() throws InterruptedException, IOException {
		isQueueRunning.set(true);
		
		while (shouldBeActive.get() && !jobsQueue.isEmpty()) {
			final IObdCommand job = jobsQueue.take();
			if(write(connection, job) && receive(connection, job)) {
				handleMeasurement(job);
			}
		}
		
		isQueueRunning.set(false);
	}

	@UiThread
	void handleMeasurement(final IObdCommand job) {
		if(job instanceof FuelLevelObdCommand) {
			vmCustomDataSource.notifyMeasurement(new FuelLevel(((FuelLevelObdCommand) job).getValue()).toRaw());
		} else if(job instanceof EngineRPMObdCommand) {
			vmCustomDataSource.notifyMeasurement(new EngineSpeed(((EngineRPMObdCommand) job).getRPM()).toRaw());
		} else if(job instanceof SpeedObdCommand) {
			vmCustomDataSource.notifyMeasurement(new VehicleSpeed(((SpeedObdCommand) job).getMetricSpeed()).toRaw());
		}/* else if(job instanceof FuelConsumptionRateObdCommand) {
			vmCustomDataSource.notifyMeasurement(new FuelConsumed(((FuelConsumptionRateObdCommand) job).getMetricSpeed()).toRaw());
		}*/
	}

	private static boolean write(Connection connection, IObdCommand job) throws IOException, InterruptedException {
		boolean success = connection.write((job.getCommand() + "\r").getBytes());
		Thread.sleep(200);
		return success;
	}

	private static boolean receive(Connection connection, IObdCommand job) throws IOException {
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

	private Runnable mQueueCommands = new Runnable() {
		public void run() {
			if(shouldBeActive.get()) {
				if (isActive.get()) {
					// query
					addQuery(new FuelLevelObdCommand());
					addQuery(new EngineRPMObdCommand());
					addQuery(new SpeedObdCommand(false));

					// run again in 5s
					runOnBackgroundDelayed(mQueueCommands, 5000);				
				} else {
					// run again in 2s
					runOnBackgroundDelayed(mQueueCommands, 2000);
				}
			}
		}
	};

	public class CarGatewayServiceBinder extends Binder {
		public boolean start(String deviceAddress) {
			return CarGatewayService.this.start(deviceAddress);
		}
		
		public void stop() {
			CarGatewayService.this.stop();
		}
	}
	
}
