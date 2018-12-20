package com.eiffage.almacenes.Objetos;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class MySqliteOpenHelper extends SQLiteOpenHelper {

    Context context;
    private static final String DATABASE_NAME = "Almacenes";
    private static final int DATABASE_VERSION = 1;

    private static final String CREAR_TABLA_ALMACENES = "CREATE TABLE Almacen (" +
            "nombreAlmacen TEXT PRIMARY KEY," +
            "provincia TEXT, elegido TEXT)";

    public MySqliteOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_ALMACENES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "Almacen");
        this.onCreate(db);
    }

    public void insertarAlmacen(SQLiteDatabase db, Almacen almacen){
        Cursor c = db.rawQuery("SELECT * FROM Almacen WHERE nombreAlmacen LIKE '" + almacen.getAlmacen() + "'", null);
        if(c.getCount() == 0)
            db.execSQL("INSERT INTO Almacen VALUES ('" + almacen.getAlmacen() + "', '" + almacen.getProvincia() + "', 'NO')");
        c.close();
    }

    public void cambiarAlmacenElegido(SQLiteDatabase db, Almacen almacen){
        try{
            db.execSQL("UPDATE Almacen set elegido = 'NO' WHERE elegido = 'SI'");
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        Cursor c = db.rawQuery("SELECT * FROM Almacen WHERE nombreAlmacen LIKE '" + almacen.getAlmacen() + "'", null);
        if(c.getCount() == 0)
            db.execSQL("INSERT INTO Almacen VALUES ('" + almacen.getAlmacen() + "', '" + almacen.getProvincia() + "', 'SI')");
        else
            db.execSQL("UPDATE Almacen set elegido = 'SI' WHERE nombreAlmacen LIKE '" + almacen.getAlmacen() + "'");
        c.close();

    }

    public boolean isElegido(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM Almacen WHERE elegido = 'SI'", null);
        if(c.getCount() == 0)
            return false;
        else return true;
    }

    public Almacen getElegido(SQLiteDatabase db){
        try {
            Cursor c = db.rawQuery("SELECT * FROM Almacen WHERE elegido = 'SI'", null);
            c.moveToFirst();
            if( c.getString(2).equals("SI")){
                String almacen = c.getString(0);
                String prov = c.getString(1);
                Log.d("ALMACEN ELEGIDO", almacen);
                c.close();
                return new Almacen(prov, almacen);
            }
            else {
                Log.d("ALMACEN ELEGIDO", "¡HELP!");
                c.close();
                return null;
            }
        }
        catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    public int numAlmacenes(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM Almacen", null);
        int val = c.getCount();
        c.close();
        return val;

    }

    public ArrayList<Almacen> getAlmacenes(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT * FROM Almacen", null);
        c.moveToFirst();
        ArrayList<Almacen> almacenes = new ArrayList<>();
        while(!c.isAfterLast()){
            almacenes.add(new Almacen(c.getString(1), c.getString(0)));
            c.moveToNext();
        }
        c.close();
        return almacenes;
    }

    public Almacen getAlmacen(SQLiteDatabase db, String nombreAlmacen){
        Cursor c = db.rawQuery("SELECT * FROM Almacen WHERE nombreAlmacen LIKE '" + nombreAlmacen + "'", null);
        Almacen a = new Almacen("-","-");
        if(c.getCount() > 0){
            c.moveToFirst();
            a = new Almacen(c.getString(1), c.getString(0));
        }
        return a;
    }


}
