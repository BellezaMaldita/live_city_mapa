package cl.sarai.saraimapa;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

import cl.sarai.saraimapa.utils.DBHelper;

public class DetalleCompraActivity extends AppCompatActivity {

    private ListView listaDetalle;
    private DBHelper dbHelper;
    private List<Map<String, Object>> detalles = new ArrayList<>();
    private int compraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_compra);

        dbHelper = new DBHelper(this);
        listaDetalle = findViewById(R.id.listaDetalle);

        compraId = getIntent().getIntExtra("compra_id", -1);
        if (compraId != -1) cargarDetalle();
    }

    private void cargarDetalle() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT p.nombre, d.cantidad, d.subtotal " +
                        "FROM detalle_compra d " +
                        "JOIN producto p ON d.producto_id = p.id " +
                        "WHERE d.compra_id = ?",
                new String[]{String.valueOf(compraId)});

        while (c.moveToNext()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("nombre", c.getString(0));
            fila.put("cantidad", c.getInt(1));
            fila.put("subtotal", c.getDouble(2));
            detalles.add(fila);
        }
        c.close();
        db.close();

        listaDetalle.setAdapter(new DetalleAdapter());
    }

    private class DetalleAdapter extends BaseAdapter {
        @Override
        public int getCount() { return detalles.size(); }

        @Override
        public Object getItem(int position) { return detalles.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_detalle, parent, false);

            TextView txtNombre = convertView.findViewById(R.id.txtNombreProducto);
            TextView txtCantidad = convertView.findViewById(R.id.txtCantidad);
            TextView txtSubtotal = convertView.findViewById(R.id.txtSubtotal);

            Map<String, Object> fila = detalles.get(position);
            txtNombre.setText((String) fila.get("nombre"));
            txtCantidad.setText("x" + fila.get("cantidad"));
            txtSubtotal.setText("$" + String.format("%,.0f", fila.get("subtotal")));

            return convertView;
        }
    }
}

