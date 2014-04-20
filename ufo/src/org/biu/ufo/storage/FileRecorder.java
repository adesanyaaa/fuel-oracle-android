package org.biu.ufo.storage;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.biu.ufo.model.DrivePoint;
import org.biu.ufo.model.Location;

import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.openxc.measurements.serializers.JsonSerializer;
import com.openxc.sources.DataSourceException;
import com.openxc.util.FileOpener;

/**
 * Record raw vehicle measurements to a file as JSON.
 *
 * This data sink is a simple passthrough that records every raw vehicle
 * measurement as it arrives to a file on the device. It uses a heuristic to
 * detect different "trips" in the vehicle, and splits the recorded trace by
 * trip.
 *
 * The heuristic is very simple: if we haven't received any new data in a while,
 * consider the previous trip to have ended. When activity resumes, start a new
 * trip.
 */
public class FileRecorder {
    private final static String TAG = "RouteFileRecorder";
    private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private FileOpener mFileOpener;
    private BufferedWriter mWriter;
	private JsonReader m_jsReader;
	String mDirectory;

    public FileRecorder(String directory, FileOpener fileOpener) {
        mFileOpener = fileOpener;
        mDirectory = directory;
    }
    
    public String openNewFile() throws IOException {
    	return openTimestampedFile();
    }

    public synchronized boolean writeRecord(String type, Object value,String label, boolean lastRecord) {
        if(mWriter == null) {
        	Log.e(TAG, "no file!");
        	return false;
        }

        try {
        	mWriter.write(JsonSerializer.serialize(type,value,label, System.currentTimeMillis()));
            if(!lastRecord){
            	mWriter.write(',');
            }
            mWriter.newLine();
        } catch(IOException e) {
            Log.w(TAG, "Unable to write measurement to file", e);
            return false;
        }
        return true;
    }

    public synchronized void stop() {
        close();
        Log.i(TAG, "Shutting down");
    }

    public synchronized void flush() {
        if(mWriter != null) {
            try {
                mWriter.flush();
            } catch(IOException e) {
                Log.w(TAG, "Unable to flush writer", e);
            }
        }
    }

    private synchronized void close() {
        if(mWriter != null) {
            try {
            	mWriter.write(']');
                mWriter.close();
            } catch(IOException e) {
                Log.w(TAG, "Unable to close output file", e);
            }
            mWriter = null;
        }
    }

    private synchronized String openTimestampedFile() throws IOException {
        Calendar calendar = GregorianCalendar.getInstance();
        String filename = sDateFormatter.format(
                calendar.getTime()) + ".json";
        if(mWriter != null) {
            close();
        }
        mWriter = mFileOpener.openForWriting(filename);
        mWriter.write('[');
        Log.i(TAG, "Opened trace file " + filename + " for writing");
        return filename;
    }
    
    
    
    public ArrayList<DrivePoint> readTraceLocations(String filename)throws DataSourceException {
    	ArrayList<DrivePoint> route = new ArrayList<DrivePoint>();
    	JsonReader reader = null;
    	 try {
    		 reader = openForReading(filename);
    		 reader.beginArray();
    	     while (reader.hasNext()) {
    	    	 route.add(readLocation(reader));
    	     }
    	     reader.endArray();
    	     reader.close();
		} catch (IOException e) {
			}
			return route;
		}
		

    private DrivePoint readLocation(JsonReader reader) {
    	DrivePoint dp = new DrivePoint();
    	String str_latlng = "";
    	String str_label = "";
    	double longitude = 0;
    	double latitude = 0;
    	long out_timestamp =0;
    	String type = "";
    	
    	try {

    	reader.beginObject();
         while (reader.hasNext()) {
        	 String name = reader.nextName();
           if (name.equals("name")) {
        	   type = reader.nextString();
           }else if (name.equals("value")) {
        	   str_latlng = reader.nextString();
        	   int sprt = str_latlng.indexOf(',');
        	   latitude = Double.parseDouble(str_latlng.substring(0,sprt++));
        	   longitude = Double.parseDouble(str_latlng.substring(sprt));
           }else if (name.equals("event")) {
        	   str_label = reader.nextString();
           }else if (name.equals("timestamp")) {
        	   out_timestamp = new Double(reader.nextDouble()).longValue();
           }else {
				reader.skipValue();
           }
         }
         reader.endObject();
         
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	Location loc = new Location(new LatLng(latitude, longitude));
    	loc.setTimestamp(out_timestamp);
    	dp.setLocation(loc);
    	dp.setLabel(str_label);
        return dp;
       }
    
	

	
    public JsonReader openForReading(String filename) throws IOException {
        Log.i(TAG, "Opening " + mDirectory + "/" + filename
                + " for writing on external storage");

        File externalStoragePath = Environment.getExternalStorageDirectory();
        File directory = new File(externalStoragePath.getAbsolutePath() +
                "/" + mDirectory);
        
      
        File file = new File(directory, filename);
        try {
            //directory.mkdirs();
            InputStream inputStream=new FileInputStream(file);
            return new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            //new BufferedReader(new InputStreamReader(inputStream));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + file + " for writing", e);
            throw e;
        }
    }
   
}
