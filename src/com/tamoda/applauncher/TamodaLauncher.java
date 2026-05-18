package com.tamoda.applauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

// Import tambahan untuk Daya Ingat (SharedPreferences)
import android.content.SharedPreferences;

// Import tambahan untuk Bottom Sheet
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(
    version = 6, // Naik ke versi 6 agar Kodular otomatis membaca pembaruan
    description = "Launcher Anti-Nimpa, MultiDelay Jeda Iklan, Daya Ingat (Memory), dan Bottom Sheet Radius.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class TamodaLauncher extends AndroidNonvisibleComponent {

    private Context context;
    
    // Variabel tambahan untuk sistem Bottom Sheet
    private Dialog bottomSheetDialog;
    private ViewGroup originalParent;
    private int bgColor = Color.TRANSPARENT; 
    private float cornerRadius = 20f;
    private boolean isCancelable = false;

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
    // FITUR BAWAAN: MULTI DELAY (PENGGANTI CLOCK)
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

    // ==========================================
    // FITUR BARU 1: DAYA INGAT (SHARED PREFERENCES)
    // ==========================================
    
    @SimpleFunction(description = "Simpan data (Teks/Angka) permanen. Key adalah nama data, Value adalah isinya.")
    public void SimpanData(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TamodaMemory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @SimpleFunction(description = "Ambil data yang tersimpan. Jika tidak ada, akan memunculkan DefaultValue.")
    public String AmbilData(String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TamodaMemory", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
    
    @SimpleFunction(description = "Hapus daya ingat berdasarkan Key tertentu.")
    public void HapusData(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TamodaMemory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    // ==========================================
    // FITUR BARU 2: BOTTOM SHEET RADIUS TRANSPARAN
    // ==========================================
    
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = "&H00000000")
    @SimpleProperty(description = "Warna background Bottom Sheet.", category = PropertyCategory.APPEARANCE)
    public void SheetBackgroundColor(int color) {
        this.bgColor = color;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int SheetBackgroundColor() {
        return this.bgColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "20.0")
    @SimpleProperty(description = "Lengkungan sudut atas (Radius).", category = PropertyCategory.APPEARANCE)
    public void SheetRadius(float radius) {
        this.cornerRadius = radius;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public float SheetRadius() {
        return this.cornerRadius;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Set TRUE jika ingin tombol Back HP bisa menutup menu ini.", category = PropertyCategory.BEHAVIOR)
    public void SheetCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean SheetCancelable() {
        return this.isCancelable;
    }

    @SimpleFunction(description = "Munculkan Bottom Sheet. Hanya butuh memasukkan blok Layout target.")
    public void TampilkanBottomSheet(AndroidViewComponent layoutComponent) {
        final View view = layoutComponent.getView();

        view.setAlpha(1.0f); 

        if (view.getParent() != null) {
            originalParent = (ViewGroup) view.getParent();
            originalParent.removeView(view);
        }

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(this.bgColor);
        float[] radii = {this.cornerRadius, this.cornerRadius, this.cornerRadius, this.cornerRadius, 0, 0, 0, 0};
        bgShape.setCornerRadii(radii);
        view.setBackground(bgShape);
        view.setVisibility(View.VISIBLE);

        bottomSheetDialog = new Dialog(context);
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(view);
        
        bottomSheetDialog.setCancelable(this.isCancelable); 
        bottomSheetDialog.setCanceledOnTouchOutside(false); 

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);

            // Menghilangkan efek gelap & mengizinkan klik tembus ke background video
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setAlpha(0.0f);
                view.setVisibility(View.GONE);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (view.getParent() != null) {
                                ((ViewGroup) view.getParent()).removeView(view);
                            }
                            if (originalParent != null) {
                                originalParent.addView(view);
                                view.setVisibility(View.GONE); 
                            }
                            BottomSheetDitutup(); 
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 50);
            }
        });

        bottomSheetDialog.show();
    }

    @SimpleFunction(description = "Sembunyikan / Tutup Bottom Sheet.")
    public void TutupBottomSheet() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    @SimpleFunction(description = "Cek apakah Bottom Sheet sedang terbuka.")
    public boolean ApakahBottomSheetTerbuka() {
        if (bottomSheetDialog != null) {
            return bottomSheetDialog.isShowing();
        }
        return false;
    }

    @SimpleEvent(description = "Event saat Bottom Sheet selesai ditutup dan layout aman dikembalikan.")
    public void BottomSheetDitutup(){
        EventDispatcher.dispatchEvent(this, "BottomSheetDitutup");
    }
}
