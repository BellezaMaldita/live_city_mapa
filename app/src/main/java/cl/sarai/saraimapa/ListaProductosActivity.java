package cl.sarai.saraimapa;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

import cl.sarai.saraimapa.utils.DBHelper;

public class ListaProductosActivity extends AppCompatActivity {

    private ListView listaProductos;
    private Button btnVerTiendas;
    private Button btnGuardarCompra;
    private DBHelper dbHelper;

    private List<Map<String, Object>> productos = new ArrayList<>();
    private Map<Integer, Integer> carrito = new HashMap<>(); // idProducto -> cantidad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_productos);

        dbHelper = new DBHelper(this);
        listaProductos = findViewById(R.id.listaProductos);
        btnGuardarCompra = findViewById(R.id.btnGuardarCompra);
        btnVerTiendas = findViewById(R.id.btnVerTiendas);

        cargarProductos();
        configurarBotonGuardar();

        btnVerTiendas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaProductosActivity.this, MapaActivity.class);
                startActivity(intent);
            }
        });

    }

    private void cargarProductos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, nombre, precio, geolocalizacion FROM producto", null);

        while (c.moveToNext()) {
            Map<String, Object> p = new HashMap<>();
            p.put("id", c.getInt(0));
            p.put("nombre", c.getString(1));
            p.put("precio", c.getDouble(2));
            p.put("tienda", c.getString(3)); // nuevo campo
            productos.add(p);
        }

        c.close();
        db.close();

        listaProductos.setAdapter(new ProductoAdapter());
    }

    private class ProductoAdapter extends BaseAdapter {
        @Override
        public int getCount() { return productos.size(); }

        @Override
        public Object getItem(int position) { return productos.get(position); }

        @Override
        public long getItemId(int position) { return (int) productos.get(position).get("id"); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_producto, parent, false);

            TextView txtNombre = convertView.findViewById(R.id.txtNombre);
            TextView txtTienda = convertView.findViewById(R.id.txtTienda);
            TextView txtPrecio = convertView.findViewById(R.id.txtPrecio);
            Button btnAdd = convertView.findViewById(R.id.btnAdd);

            Map<String, Object> p = productos.get(position);
            int idProducto = (int) p.get("id");
            String nombre = (String) p.get("nombre");
            double precio = (double) p.get("precio");
            String tienda = (String) p.get("tienda");

            txtNombre.setText(nombre);
            txtTienda.setText("Tienda: " + (tienda != null ? tienda : "Desconocida"));
            txtPrecio.setText("$" + String.format(Locale.getDefault(), "%,.0f", precio));

            btnAdd.setOnClickListener(v -> {
                int cantidad = carrito.getOrDefault(idProducto, 0) + 1;
                carrito.put(idProducto, cantidad);
                Toast.makeText(ListaProductosActivity.this,
                        "Añadido: " + nombre + " (" + cantidad + ")", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }


    private void configurarBotonGuardar() {
        btnGuardarCompra.setOnClickListener(v -> guardarCompra());
    }

    private void guardarCompra() {
        if (carrito.isEmpty()) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            ContentValues compraValues = new ContentValues();
            compraValues.put("fecha", fecha);
            int idUsuario = getSharedPreferences("ghibli_prefs", MODE_PRIVATE).getInt("usuario_id", -1);
            compraValues.put("usuario_id", idUsuario);
            compraValues.put("total", calcularTotal());

            long compraId = db.insert("compra", null, compraValues);

            for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
                int idProducto = entry.getKey();
                int cantidad = entry.getValue();
                double precio = obtenerPrecio(db, idProducto);
                double subtotal = cantidad * precio;

                ContentValues detalle = new ContentValues();
                detalle.put("compra_id", compraId);
                detalle.put("producto_id", idProducto);
                detalle.put("cantidad", cantidad);
                detalle.put("subtotal", subtotal);
                db.insert("detalle_compra", null, detalle);
            }

            db.setTransactionSuccessful();
            Toast.makeText(this, "Compra registrada con éxito", Toast.LENGTH_LONG).show();
            carrito.clear();

            // Volver al listado de compras
            Intent intent = new Intent(ListaProductosActivity.this, ComprasActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private double obtenerPrecio(SQLiteDatabase db, int idProducto) {
        Cursor c = db.rawQuery("SELECT precio FROM producto WHERE id=?", new String[]{String.valueOf(idProducto)});
        double precio = 0;
        if (c.moveToFirst()) precio = c.getDouble(0);
        c.close();
        return precio;
    }

    private double calcularTotal() {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
            int id = entry.getKey();
            int cantidad = entry.getValue();
            double precio = obtenerPrecio(dbHelper.getReadableDatabase(), id);
            total += cantidad * precio;
        }
        return total;
    }
}
