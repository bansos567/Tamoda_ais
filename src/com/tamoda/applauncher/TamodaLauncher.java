package com.tamoda.applauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(version = 2,
    description = "Launcher & Share Text ke Aplikasi (Tidak Nimpa Task)",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@SimpleObject(external = true)
public class TamodaLauncher extends AndroidNonvisibleComponent {

    private Context context;

    public TamodaLauncher(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
    }

    @SimpleFunction(description = "Buka aplikasi berdasarkan Package Name dengan Task terpisah.")
    public void BukaPindahAplikasi(String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            BerhasilDibuka();
        } else {
            AplikasiTidakDitemukan(packageName);
        }
    }

    @SimpleFunction(description = "Share teks langsung ke WhatsApp tanpa nimpa task.")
    public void ShareKeWhatsApp(String pesan) {
        shareTextToApp("com.whatsapp", pesan);
    }

    @SimpleFunction(description = "Share teks langsung ke Telegram tanpa nimpa task.")
    public void ShareKeTelegram(String pesan) {
        shareTextToApp("org.telegram.messenger", pesan);
    }
    
    @SimpleFunction(description = "Share teks/URL langsung ke Facebook tanpa nimpa task.")
    public void ShareKeFacebook(String pesan) {
        shareTextToApp("com.facebook.katana", pesan);
    }

    private void shareTextToApp(String packageName, String pesan) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage(packageName);
        intent.putExtra(Intent.EXTRA_TEXT, pesan);
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
            BerhasilDibuka();
        } catch (ActivityNotFoundException e) {
            AplikasiTidakDitemukan(packageName);
        }
    }

    @SimpleEvent(description = "Tertrigger jika aplikasi berhasil dipanggil atau pesan di-share.")
    public void BerhasilDibuka() {
        EventDispatcher.dispatchEvent(this, "BerhasilDibuka");
    }

    @SimpleEvent(description = "Tertrigger jika package name tidak terinstall di HP user.")
    public void AplikasiTidakDitemukan(String packageName) {
        EventDispatcher.dispatchEvent(this, "AplikasiTidakDitemukan", packageName);
    }
}
