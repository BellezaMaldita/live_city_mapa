package cl.sarai.saraimapa;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import cl.sarai.saraimapa.utils.DBHelper;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsuario, edtPassword;
    private Button btnLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);

        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        dbHelper = new DBHelper(this);

        // Forzar creación de la base
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuario = edtUsuario.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (usuario.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                int idUsuario = validarUsuario(usuario, password);

                if (idUsuario != -1) {
                    // guardamos el ID del usuario logueado
                    getSharedPreferences("ghibli_prefs", MODE_PRIVATE)
                            .edit()
                            .putInt("usuario_id", idUsuario)
                            .apply();

                    Toast.makeText(MainActivity.this, "Bienvenido " + usuario, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ComprasActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private int validarUsuario(String usuario, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM usuario WHERE usuario=? AND password=?",
                new String[]{usuario, password});

        int idUsuario = -1;
        if (c.moveToFirst()) {
            idUsuario = c.getInt(0);
        }
        c.close();
        db.close();
        return idUsuario;
    }

}
