package org.biu.ufo.storage;

import android.database.sqlite.SQLiteDatabase;

public abstract class ComponentDBHelper {
	
	public final String TABLE_NAME;
	public static final String COLUMN_ID = "_id";
	
	public ComponentDBHelper(String tableName){
		this.TABLE_NAME = tableName;
	}

	public abstract void createTable(SQLiteDatabase database);
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    createTable(db);		
	}
}
