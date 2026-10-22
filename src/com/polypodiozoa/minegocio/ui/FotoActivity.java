package com.polypodiozoa.minegocio.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Imagen;

import java.io.File;

public class FotoActivity extends BaseActivity {

    private String ruta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);
        ruta = getIntent().getStringExtra("ruta");
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        ImageView img = findViewById(R.id.imgFoto);
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        if (titulo != null && !titulo.isEmpty()) {
            ((android.widget.TextView) findViewById(R.id.tituloBarra)).setText(titulo);
        }
        if (ruta == null || ruta.isEmpty()) {
            aviso("No se encontró la imagen.");
            finish();
            return;
        }
        Imagen.mostrar(img, ruta, R.drawable.ic_imagen, 1600);
        img.setContentDescription(descripcion != null && !descripcion.isEmpty()
                ? descripcion : "Comprobante: " + new File(ruta).getName());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir(false));
        findViewById(R.id.btnOtraApp).setOnClickListener(v -> compartir(true));
    }

    private Uri uri() {
        if (ruta.startsWith(Archivos.dirTemporal(this).getAbsolutePath())) {
            return Archivos.uriTemporal(this, new File(ruta));
        }
        return Archivos.uriPermanente(this, ruta);
    }

    private void compartir(boolean abrir) {
        Uri uri = uri();
        Intent i;
        if (abrir) {
            i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, Archivos.mimeDe(uri, this));
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            i = new Intent(Intent.ACTION_SEND);
            i.setType("image/*");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para abrir o compartir esta imagen.");
        }
    }
}
