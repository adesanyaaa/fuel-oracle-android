package org.biu.ufo.services;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.obd.commands.IObdCommand;
import org.biu.ufo.obd.commands.fuel.FuelLevelObdCommand;
import org.biu.ufo.obd.commands.protocol.EchoOffObdCommand;
import org.biu.ufo.obd.commands.protocol.LineFeedOffObdCommand;
import org.biu.ufo.obd.commands.protocol.ObdResetCommand;
import org.biu.ufo.obd.commands.protocol.SelectProtocolObdCommand;
import org.biu.ufo.obd.commands.protocol.TimeoutObdCommand;
import org.biu.ufo.obd.connection.Connection;
import org.biu.ufo.obd.connection.ConnectionCallback;
import org.biu.ufo.obd.enums.ObdProtocols;
import org.biu.ufo.openxc.VehicleManagerConnector;
import org.biu.ufo.openxc.VehicleManagerConnector.VehicleManagerConnectorCallback;
import org.biu.ufo.openxc.sources.ObdDataSource;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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
@EService
public class CarGatewayService extends BoundedWorkerService implements VehicleManagerConnectorCallback, ConnectionCallback {
	private final static String TAG = "CarGatewayService";

	private final IBinder binder = new CarGatewayServiceBinder();
	private final BlockingQueue<IObdCommand> jobsQueue = new LinkedBlockingQueue<IObdCommand>();

	@Bean
	ObdDataSource vmCustomDataSource;
	private VehicleManagerConnector vmConnector;
	private Connection connection;	// TODO: initialize


	public CarGatewayService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		vmConnector = new VehicleManagerConnector(this, this);
		vmConnector.bindToVehicleManager();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		vmConnector.unbindToVehicleManager();
		vmConnector.cleanup();
	}

	@Override
	@UiThread
	public void onVMConnected() {
		vmConnector.getVehicleManager().addSource(vmCustomDataSource);
		startConnection();
	}

	@Background
	public void startConnection() {
		connection.start();
	}

	@Override
	public void onVMDisconnected() {
		connection.stop();
	}

	@Override
	public void sourceConnected(Connection source) {
		initializeDevice();
	}

	@Override
	public void sourceDisconnected(Connection source) {
		// TODO Auto-generated method stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void initializeDevice() {
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
					vmCustomDataSource.notifyMeasurement(new FuelLevel(((FuelLevelObdCommand) job).getValue()).toRaw());
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
		public void addQuery(BaseObdQueryCommand cmd) {
			CarGatewayService.this.addQuery(cmd);
		}
	}

}
