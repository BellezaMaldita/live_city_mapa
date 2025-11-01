package cl.sarai.saraimapa;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import cl.sarai.saraimapa.utils.DBHelper;

public class RegistroActivity extends AppCompatActivity {

    private EditText edtNuevoUsuario, edtNuevaPassword;
    private Button btnGuardarUsuario;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        edtNuevoUsuario = findViewById(R.id.edtNuevoUsuario);
        edtNuevaPassword = findViewById(R.id.edtNuevaPassword);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        dbHelper = new DBHelper(this);

        btnGuardarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = edtNuevoUsuario.getText().toString().trim();
                String pass = edtNuevaPassword.getText().toString().trim();

                if (usuario.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("usuario", usuario);
                values.put("password", pass);

                long result = db.insert("usuario", null, values);
                db.close();

                if (result != -1) {
                    Toast.makeText(RegistroActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // vuelve al login
                } else {
                    Toast.makeText(RegistroActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
