package cl.sarai.saraimapa;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

import cl.sarai.saraimapa.utils.DBHelper;

public class ComprasActivity extends AppCompatActivity {

    private ListView listaCompras;
    private DBHelper dbHelper;
    private List<Map<String, Object>> compras = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compras);

        dbHelper = new DBHelper(this);
        listaCompras = findViewById(R.id.listaCompras);

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(ComprasActivity.this, ListaProductosActivity.class);
            startActivity(intent);
        });

        cargarCompras();
    }

    private void cargarCompras() {
        compras.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int idUsuario = getSharedPreferences("ghibli_prefs", MODE_PRIVATE).getInt("usuario_id", -1);
        Cursor c = db.rawQuery("SELECT id, fecha, total FROM compra WHERE usuario_id=? ORDER BY id DESC",
                new String[]{String.valueOf(idUsuario)});

        while (c.moveToNext()) {
            Map<String, Object> comp = new HashMap<>();
            comp.put("id", c.getInt(0));
            comp.put("fecha", c.getString(1));
            comp.put("total", c.getDouble(2));
            compras.add(comp);
        }
        c.close();
        db.close();

        listaCompras.setAdapter(new ComprasAdapter());
    }

    private class ComprasAdapter extends BaseAdapter {
        @Override
        public int getCount() { return compras.size(); }

        @Override
        public Object getItem(int position) { return compras.get(position); }

        @Override
        public long getItemId(int position) { return (int) compras.get(position).get("id"); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_compra, parent, false);

            TextView txtFecha = convertView.findViewById(R.id.txtFechaCompra);
            TextView txtTotal = convertView.findViewById(R.id.txtTotalCompra);
            Button btnVerDetalle = convertView.findViewById(R.id.btnVerDetalle);

            Map<String, Object> comp = compras.get(position);
            int idCompra = (int) comp.get("id");
            String fecha = (String) comp.get("fecha");
            double total = (double) comp.get("total");

            txtFecha.setText("Fecha: " + fecha);
            txtTotal.setText("Total: $" + String.format("%,.0f", total));

            btnVerDetalle.setOnClickListener(v -> {
                Intent intent = new Intent(ComprasActivity.this, DetalleCompraActivity.class);
                intent.putExtra("compra_id", idCompra);
                startActivity(intent);
            });

            return convertView;
        }
    }
}
