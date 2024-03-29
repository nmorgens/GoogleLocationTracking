package com.example.nathanmorgenstern.googlelocationtracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;



public class MySQLHelper extends SQLiteOpenHelper {

    private static final String SQL_DEBUGGER = "SQL_DEBUG";
    //Create table names
    private static final String TABLE_CHECK_IN      = "check_in_info";
    private static final String TABLE_LOCATION_INFO = "location_info";
    private static final String TABLE_NORMALIZED    = "normalized_info";

    // Location info Columns names
    private static final String KEY_ID          = "id";
    private static final String KEY_LATITUDE    = "latitude";
    private static final String KEY_LONGITUDE   = "longitude";
    private static final String KEY_TIME        = "time";
    private static final String KEY_ADDRESS     = "address";

    //Variables for normalized table
    private static final String KEY_LOCATION_ID = "location_id";
    private static final String KEY_CHECK_IN_ID = "check_in_id";

    // Table check in Column names
    private static final String KEY_CHECK_IN_NAME = "check_in_name";


    private static final String[] LOCATION_COLUMNS   = {KEY_LATITUDE,KEY_LONGITUDE,KEY_TIME,KEY_ADDRESS};
    private static final String[] CHECK_IN_COLUMNS   = {KEY_CHECK_IN_NAME};
    private static final String[] NORMALIZED_COLUMNS = {KEY_LOCATION_ID, KEY_LOCATION_ID};

    //Private variables for initializing locations
    private static final String location1 = "Study spot";
    private static final String location1_address = "Bartholomew Rd\n Piscataway Township\n NJ 08854";
    private static final String loc1_lat = "40.52433664449722";
    private static final String loc1_lon = "-74.45861577987671";

    private static final String location2 = "Bike";
    private static final String location2_address = "Titsworth Pl\n Piscataway Township\n NJ 08854";
    private static final String loc2_lat = "40.52662";
    private static final String loc2_lon = "-74.461641";

    private static final String location3 = "Chem recitation";
    private static final String location3_address = "110 Frelinghuysen Rd \nPiscataway Township\n NJ 08854";
    private static final String loc3_lat = "40.521417";
    private static final String loc3_lon = "-74.4631";

    private static final String location4 = "Wallet Seed";
    private static final String location4_address = "183-199 Bevier Rd\n Piscataway Township\n NJ 08854";
    private static final String loc4_lat = "40.525201";
    private static final String loc4_lon = "-74.463723";

    private static final String location5 = "First integral";
    private static final String location5_address = "720 Davidson Rd\n Piscataway Township\n NJ 08854";
    private static final String loc5_lat = "40.528202";
    private static final String loc5_lon = " -74.465375";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "tracking_db_v62_test";

    //For table creation queries
    // SQL statement to create Table Location Info
    private String CREATE_LOCATION_INFO_TABLE = "CREATE TABLE " +  TABLE_LOCATION_INFO +  " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_LATITUDE  + " TEXT," +
            KEY_LONGITUDE + " TEXT," +
            KEY_TIME      + " TEXT)";

    private String CREATE_CHECK_IN_TABLE = "CREATE TABLE " +  TABLE_CHECK_IN +  " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_CHECK_IN_NAME + " TEXT," +
            KEY_ADDRESS       + " TEXT," +
            KEY_LOCATION_ID   + " INTEGER)";

    private String CREATE_NORMALIZED_TABLE = "CREATE TABLE " + TABLE_NORMALIZED + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LOCATION_ID + " INTEGER,"
            + KEY_CHECK_IN_ID + " INTEGER)";


    public MySQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(SQL_DEBUGGER, "onCreate() database...");
        db.execSQL(CREATE_LOCATION_INFO_TABLE);
        db.execSQL(CREATE_CHECK_IN_TABLE);
        db.execSQL(CREATE_NORMALIZED_TABLE);
        initializeDatabase(db);
        //db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECK_IN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NORMALIZED);
        // create fresh table
        onCreate(db);
    }


    /* SQL TABLE LOCATION HELPER METHODS */

    public int getLocationTablePrimaryKey(String lat, String lon, String time){
        SQLiteDatabase db = this.getReadableDatabase();

        String strQuery = "SELECT " + KEY_ID + " FROM " + TABLE_LOCATION_INFO + " WHERE latitude=? AND longitude=? AND time=?";
        Cursor cursor = db.rawQuery(strQuery, new String[] {lat,lon,time},null);

        Log.v(SQL_DEBUGGER, "Cursor initialized in getLocationTablePrimaryKey()");

        int key = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            key = cursor.getInt(0);
            Log.v(SQL_DEBUGGER, "key " + key);
        }

        db.close();
        cursor.close();
        // return contact list
        return key;
    }

    public void addLocationInfo(LocationInfo loc_info){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();

        values.put(KEY_LATITUDE,  loc_info.getLatitude());
        values.put(KEY_LONGITUDE, loc_info.getLongitude());
        values.put(KEY_TIME,      loc_info.getTime());

        Log.v(SQL_DEBUGGER, "latitude: "   + loc_info.getLatitude());
        Log.v(SQL_DEBUGGER, "longitude: "  + loc_info.getLongitude());
        Log.v(SQL_DEBUGGER, "time: "       + loc_info.getTime());

        // 3. insert
        db.insert(TABLE_LOCATION_INFO, // table
                    null, //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    // Getting All Location Check ins for the list
    public ArrayList<LocationInfo> getAllLocations() {

        Log.v(SQL_DEBUGGER, "getAllLocations()");
        ArrayList<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_INFO + " tbl_location, " +
                                                 TABLE_CHECK_IN      + " tbl_check, "    +
                                                 TABLE_NORMALIZED    + " tbl_norm "      +
                "WHERE" +
                " tbl_norm." + KEY_LOCATION_ID  + " = tbl_location." + KEY_ID +
                " AND tbl_norm." + KEY_CHECK_IN_ID  + " = tbl_check." + KEY_ID;


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int    temp_id;
        String temp_lat;
        String temp_long;
        String temp_time;
        String temp_address;
        String temp_check_in;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                temp_id       = cursor.getColumnIndex(KEY_ID);
                temp_check_in =  cursor.getString(cursor.getColumnIndex(KEY_CHECK_IN_NAME));
                temp_lat      =  cursor.getString(cursor.getColumnIndex(KEY_LATITUDE));
                temp_long     =  cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE));
                temp_time     =  cursor.getString(cursor.getColumnIndex(KEY_TIME));
                temp_address  = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
                LocationInfo temp_location_info = new LocationInfo(temp_id,temp_lat,temp_long,temp_time,temp_address);

                Log.v(SQL_DEBUGGER, "temp_check_in: " + temp_check_in);
                Log.v(SQL_DEBUGGER, "temp_lat: "      + temp_lat);
                Log.v(SQL_DEBUGGER, "temp_long: "     + temp_long);
                Log.v(SQL_DEBUGGER, "temp_time: "     + temp_time);
                Log.v(SQL_DEBUGGER, "temp_address: "  + temp_address);

                if(!temp_check_in.equals("No check in found"))
                    temp_location_info.setCheckInName(temp_check_in);
                locationInfoList.add(temp_location_info);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return locationInfoList;
    }

    //Getting All location Check ins for the map
    public ArrayList<LocationInfo> getAllUniqueLocationCheckIns() {

        Log.v(SQL_DEBUGGER, "getAllLocations()");
        ArrayList<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_INFO + " tbl_location, " +
                TABLE_CHECK_IN      + " tbl_check, "    +
                TABLE_NORMALIZED    + " tbl_norm "      +
                "WHERE" +
                " tbl_norm." + KEY_LOCATION_ID      + " = tbl_location." + KEY_ID +
                " AND tbl_norm." + KEY_CHECK_IN_ID  + " = tbl_check." + KEY_ID    +
                " AND tbl_check."+ KEY_LOCATION_ID  + " = tbl_location." + KEY_ID;


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int    temp_id;
        String temp_lat;
        String temp_long;
        String temp_time;
        String temp_address;
        String temp_check_in;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                temp_id       = cursor.getColumnIndex(KEY_ID);
                temp_check_in =  cursor.getString(cursor.getColumnIndex(KEY_CHECK_IN_NAME));
                temp_lat      =  cursor.getString(cursor.getColumnIndex(KEY_LATITUDE));
                temp_long     =  cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE));
                temp_time     =  cursor.getString(cursor.getColumnIndex(KEY_TIME));
                temp_address  = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
                LocationInfo temp_location_info = new LocationInfo(temp_id,temp_lat,temp_long,temp_time,temp_address);

                Log.v(SQL_DEBUGGER, "temp_check_in: " + temp_check_in);
                Log.v(SQL_DEBUGGER, "temp_lat: "      + temp_lat);
                Log.v(SQL_DEBUGGER, "temp_long: "     + temp_long);
                Log.v(SQL_DEBUGGER, "temp_time: "     + temp_time);
                Log.v(SQL_DEBUGGER, "temp_address: "  + temp_address);

                if(!temp_check_in.equals("No check in found"))
                    temp_location_info.setCheckInName(temp_check_in);
                locationInfoList.add(temp_location_info);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return locationInfoList;
    }

    public int getLocationInfoTableSize(){
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_INFO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int counter = 0;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                counter++;
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return counter;
    }

    public String getAddressOfCheckIn(String checkInName){
        // Select All Query
        String selectQuery = "SELECT  " + KEY_ADDRESS + " FROM " + TABLE_CHECK_IN +
                " WHERE " + KEY_CHECK_IN_NAME + "=?";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{checkInName}, null);

        String  temp = "none found";

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                cursor.moveToFirst();
                temp = cursor.getString(0);
                Log.v(SQL_DEBUGGER, "temp: "     + temp);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return temp;
    }

    public String getAddressOfCheckIn(String checkInName, int loc_id){
        // Select All Query
        String selectQuery = "SELECT  " + KEY_ADDRESS + " FROM " + TABLE_CHECK_IN +
                " WHERE " + KEY_CHECK_IN_NAME + "=? AND " + KEY_LOCATION_ID + "=?";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{checkInName, Integer.toString(loc_id)}, null);

        String  temp = "none found";

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                cursor.moveToFirst();
                temp = cursor.getString(0);
                Log.v(SQL_DEBUGGER, "temp: "     + temp);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return temp;
    }

    /* END LOCATION */

    /* SQL CHECK IN HELPER METHODS */

    public int getCheckInTablePrimaryKey(String checkInName, String address){
        SQLiteDatabase db = this.getReadableDatabase();

        String strQuery = "SELECT " + KEY_ID + " FROM " + TABLE_CHECK_IN + " WHERE check_in_name=? AND address=?";
        Cursor cursor = db.rawQuery(strQuery, new String[] {checkInName,address},null);
        Log.v(SQL_DEBUGGER, "Cursor initialized");

        int key = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            key = cursor.getInt(0);
        }

        db.close();
        cursor.close();
        // return contact list
        return key;
    }

    public void addCheckInName(String checkInName, String address, int temp_id){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();

        values.put(KEY_CHECK_IN_NAME, checkInName);
        values.put(KEY_ADDRESS,address);
        values.put(KEY_LOCATION_ID, temp_id);

        // 3. insert
        db.insert(TABLE_CHECK_IN, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // 4. close
        db.close();
    }

    public String getCheckInName(int FK)    {
        // Select All Query
        String selectQuery = "SELECT " + KEY_CHECK_IN_NAME + " FROM " + TABLE_CHECK_IN + " WHERE " + KEY_LOCATION_ID + "=?";

        SQLiteDatabase db = this.getReadableDatabase();

        String name = "No check in found";
        try {
            Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(FK)}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
        }catch (Exception e){name = "No check in found";}

        db.close();

        return name;
    }

    /* END CHECK IN METHODS */

    /* Normalized Table Helper Methods */

    public void addToNormalized(int fk_location_info, int fk_check_in){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();

        values.put(KEY_LOCATION_ID,  fk_location_info);
        values.put(KEY_CHECK_IN_ID, fk_check_in);


        // 3. insert
        db.insert(TABLE_NORMALIZED, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // 4. close
        db.close();
    }

    /* End Normalized Table Helper Methods*/

    public String getLastCheckInTimeForLocation(String locationName){
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_INFO + " tbl_location, " +
                TABLE_CHECK_IN + " tbl_check, " + TABLE_NORMALIZED + " tbl_norm " +
                "WHERE" +
                " tbl_check."    + KEY_CHECK_IN_NAME + " = '" + locationName + "'" +
                " AND tbl_norm." + KEY_LOCATION_ID  + " = tbl_location." + KEY_ID +
                " AND tbl_norm." + KEY_CHECK_IN_ID  + " = tbl_check." + KEY_ID;



        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String  temp_time = "none found";

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                temp_time     =  cursor.getString(cursor.getColumnIndex(KEY_TIME));

                Log.v(SQL_DEBUGGER, "temp_time: "     + temp_time);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return temp_time;
    }

    /* Initialization methods of dB */
    public void initializeDatabase(SQLiteDatabase db){

        String mTime = "-------";

        LocationInfo l1 = new LocationInfo(0,loc1_lat,loc1_lon, mTime, location1_address);
        l1.setCheckInName(location1);

        LocationInfo l2 = new LocationInfo(0,loc2_lat,loc2_lon, mTime, location2_address);
        l2.setCheckInName(location2);

        LocationInfo l3 = new LocationInfo(0,loc3_lat,loc3_lon, mTime, location3_address);
        l3.setCheckInName(location3);

        LocationInfo l4 = new LocationInfo(0,loc4_lat,loc4_lon, mTime, location4_address);
        l4.setCheckInName(location4);

        LocationInfo l5 = new LocationInfo(0,loc5_lat,loc5_lon, mTime, location5_address);
        l5.setCheckInName(location5);

        ArrayList<LocationInfo> loc_info_array = new ArrayList<LocationInfo>();
        loc_info_array.add(l1);
        loc_info_array.add(l2);
        loc_info_array.add(l3);
        loc_info_array.add(l4);
        loc_info_array.add(l5);

        int fk = 0;
        int fk_checkIn = 0;

        for(int i = 0; i < 5; i ++){
            addLocationInfo(loc_info_array.get(i), db);

            //Foreign key for checkInName table
            fk         = getLocationTablePrimaryKey(loc_info_array.get(i).getLatitude(),loc_info_array.get(i).getLongitude(), db);
            addCheckInName(loc_info_array.get(i).getCheckInName(), loc_info_array.get(i).getAddress(),fk, db);

            //Foreign key to be used in normalized table
            fk_checkIn = getCheckInTablePrimaryKey(loc_info_array.get(i).getCheckInName(),
                                                   loc_info_array.get(i).getAddress(), db);
            addToNormalized(fk,fk_checkIn, db);
        }

        //db.close();
    }

    public void addLocationInfo(LocationInfo loc_info, SQLiteDatabase db){

        ContentValues values = new ContentValues();

        values.put(KEY_LATITUDE,  loc_info.getLatitude());
        values.put(KEY_LONGITUDE, loc_info.getLongitude());
        values.put(KEY_TIME,      loc_info.getTime());

        Log.v(SQL_DEBUGGER, "latitude: "   + loc_info.getLatitude());
        Log.v(SQL_DEBUGGER, "longitude: "  + loc_info.getLongitude());
        Log.v(SQL_DEBUGGER, "time: "       + loc_info.getTime());

        // 3. insert
        db.insert(TABLE_LOCATION_INFO, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

    }

    public void addToNormalized(int fk_location_info, int fk_check_in,SQLiteDatabase db){
        // 1. get reference to writable DB
        //db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();

        values.put(KEY_LOCATION_ID,  fk_location_info);
        values.put(KEY_CHECK_IN_ID, fk_check_in);


        // 3. insert
        db.insert(TABLE_NORMALIZED, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // 4. close
        //db.close();
    }

    public void addCheckInName(String checkInName, String address, int temp_id, SQLiteDatabase db){

        ContentValues values = new ContentValues();

        values.put(KEY_CHECK_IN_NAME, checkInName);
        values.put(KEY_ADDRESS,address);
        values.put(KEY_LOCATION_ID,temp_id);


        // 3. insert
        db.insert(TABLE_CHECK_IN, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // 4. close
        //db.close();
    }

    public int getLocationTablePrimaryKey(String lat, String lon, SQLiteDatabase db){

        String strQuery = "SELECT " + KEY_ID + " FROM " + TABLE_LOCATION_INFO + " WHERE latitude=? AND longitude=?";
        Cursor cursor = db.rawQuery(strQuery, new String[] {lat,lon},null);

        Log.v(SQL_DEBUGGER, "Cursor initialized in getLocationTablePrimaryKey()");

        int key = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            key = cursor.getInt(0);
            Log.v(SQL_DEBUGGER, "key " + key);
        }

        //db.close();
        cursor.close();
        // return contact list
        return key;
    }

    public int getCheckInTablePrimaryKey(String checkInName, String address, SQLiteDatabase db){
        //db = this.getReadableDatabase();

        String strQuery = "SELECT " + KEY_ID + " FROM " + TABLE_CHECK_IN + " WHERE check_in_name=? AND address=?";
        Cursor cursor = db.rawQuery(strQuery, new String[] {checkInName,address},null);
        Log.v(SQL_DEBUGGER, "Cursor initialized");

        int key = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            key = cursor.getInt(0);
        }

        //db.close();
        cursor.close();
        // return contact list
        return key;
    }



}


//References: https://www.codeproject.com/Articles/119293/Using-SQLite-Database-with-Android
//http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
//https://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/