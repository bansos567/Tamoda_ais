Package com.tamoda.applauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import android.net.Uri;
// Ini import tambahan untuk fitur Timer/Waktu
import android.os.Handler;
import android.os.Looper;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(
    version = 5, // Naik ke versi 5 biar Kodular langsung update
    description = "Launcher Anti-Nimpa + Fitur MultiDelay Pintar untuk Jeda Iklan.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class TamodaLauncher extends AndroidNonvisibleComponent {

    private Context context;

    public TamodaLauncher(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
    }

    // ==========================================
    // FITUR BAWAAN: LAUNCHER ANTI-NIMPA
    // ==========================================
    
    @SimpleFunction(description = "Buka link (YouTube/FB/Web) sebagai Task terpisah (Gak nimpa).")
    public void BukaUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            
            context.startActivity(intent);
            BerhasilDibuka();
        } catch (ActivityNotFoundException e) {
            AplikasiTidakDitemukan("Browser/App Link");
        }
    }

    @SimpleFunction(description = "Buka aplikasi lain sebagai Task terpisah (Gak nimpa).")
    public void BukaPindahAplikasi(String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            
            context.startActivity(intent);
            BerhasilDibuka();
        } else {
            AplikasiTidakDitemukan(packageName);
        }
    }

    @SimpleFunction(description = "Share ke WhatsApp (Task terpisah).")
    public void ShareKeWhatsApp(String pesan) {
        shareTextToApp("com.whatsapp", pesan);
    }

    @SimpleFunction(description = "Share ke Telegram (Task terpisah).")
    public void ShareKeTelegram(String pesan) {
        shareTextToApp("org.telegram.messenger", pesan);
    }
    
    @SimpleFunction(description = "Share ke Facebook (Task terpisah).")
    public void ShareKeFacebook(String pesan) {
        shareTextToApp("com.facebook.katana", pesan);
    }

    private void shareTextToApp(String packageName, String pesan) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage(packageName);
        intent.putExtra(Intent.EXTRA_TEXT, pesan);
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try {
            context.startActivity(intent);
            BerhasilDibuka();
        } catch (ActivityNotFoundException e) {
            AplikasiTidakDitemukan(packageName);
        }
    }

    @SimpleEvent(description = "Tertrigger jika aplikasi berhasil dipanggil.")
    public void BerhasilDibuka() {
        EventDispatcher.dispatchEvent(this, "BerhasilDibuka");
    }

    @SimpleEvent(description = "Tertrigger jika aplikasi tidak ada.")
    public void AplikasiTidakDitemukan(String packageName) {
        EventDispatcher.dispatchEvent(this, "AplikasiTidakDitemukan", packageName);
    }

    // ==========================================
    // FITUR BARU: MULTI DELAY (PENGGANTI CLOCK)
    // ==========================================
    
    @SimpleFunction(description = "Mulai jeda waktu untuk tugas tertentu (Ms = Milidetik)")
    public void StartDelay(final String taskName, int durationMs) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                TimeFinished(taskName);
            }
        }, durationMs);
    }

    @SimpleEvent(description = "Event yang terpanggil saat waktu jeda habis")
    public void TimeFinished(String taskName) {
        EventDispatcher.dispatchEvent(this, "TimeFinished", taskName);
    }
}
