package cl.sarai.saraimapa.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "compras.db";
    private static final int DATABASE_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL("CREATE TABLE usuario (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "usuario TEXT NOT NULL, " +
                    "password TEXT NOT NULL)");

            db.execSQL("CREATE TABLE producto (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "precio REAL NOT NULL, " +
                    "stock INTEGER NOT NULL DEFAULT 0, " +
                    "geolocalizacion TEXT NOT NULL, " +
                    "imagen TEXT)");

            db.execSQL("CREATE TABLE compra (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fecha TEXT NOT NULL, " +
                    "usuario_id INTEGER, " +
                    "total REAL DEFAULT 0, " +
                    "FOREIGN KEY (usuario_id) REFERENCES usuario(id))");

            db.execSQL("CREATE TABLE detalle_compra (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "compra_id INTEGER NOT NULL, " +
                    "producto_id INTEGER NOT NULL, " +
                    "cantidad INTEGER NOT NULL, " +
                    "subtotal REAL NOT NULL, " +
                    "FOREIGN KEY (compra_id) REFERENCES compra(id), " +
                    "FOREIGN KEY (producto_id) REFERENCES producto(id))");

            // Inserta productos iniciales
            db.execSQL("INSERT INTO producto (nombre, precio, stock, geolocalizacion, imagen) VALUES " +
                    "('CD - El viaje de Chihiro (Joe Hisaishi)', 12990, 10, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'chihiro_cd.jpg')," +
                    "('CD - Mi vecino Totoro (Joe Hisaishi)', 11990, 8, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'totoro_cd.jpg')," +
                    "('CD - La princesa Mononoke (Joe Hisaishi)', 13990, 5, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'mononoke_cd.jpg')," +
                    "('CD - El castillo ambulante (Joe Hisaishi)', 12990, 6, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'howl_cd.jpg')," +
                    "('CD - Ponyo en el acantilado (Joe Hisaishi)', 9990, 12, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'ponyo_cd.jpg')," +
                    "('CD - El castillo en el cielo (Joe Hisaishi)', 10990, 9, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'laputa_cd.jpg')," +
                    "('CD - Arrietty y el mundo de los diminutos (Cécile Corbel)', 8990, 7, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'arrietty_cd.jpg')," +
                    "('CD - Kiki entregas a domicilio (Joe Hisaishi)', 11990, 10, 'Arte y libertad - Espacio de expresión artística para disidencias.', 'kiki_cd.jpg');");

            // Usuarios iniciales
            db.execSQL("INSERT INTO usuario (usuario, password) VALUES " +
                    "('chihiro', 'river')," +
                    "('totoro', 'forest');");

            Log.d("DBHelper", "Base de datos creada con éxito.");

        } catch (Exception e) {
            Log.e("DBHelper", "Error al crear la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS detalle_compra");
        db.execSQL("DROP TABLE IF EXISTS compra");
        db.execSQL("DROP TABLE IF EXISTS producto");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        onCreate(db);
    }

    public void logTablas(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            do {
                Log.d("DBHelper", "Tabla encontrada: " + c.getString(0));
            } while (c.moveToNext());
        } else {
            Log.d("DBHelper", "No hay tablas creadas todavía.");
        }
        c.close();
    }
}