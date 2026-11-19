package id.vocalmaster.offline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(8,11,24), panel = Color.rgb(19,24,45), purple = Color.rgb(139,92,246), white = Color.WHITE, muted = Color.rgb(166,177,205);
    private LinearLayout root, content;
    private TextView pitchText, noteText, statusText;
    private AudioRecord recorder;
    private volatile boolean listening = false;
    private Thread audioThread;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int sessions = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        showHome();
    }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); d.setStroke(1, Color.rgb(48,54,85)); return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private void base(String subtitle) {
        root = column(); root.setBackgroundColor(bg);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        content = column(); content.setPadding(20,22,20,30);
        scroll.addView(content); root.addView(scroll); setContentView(root);
        content.addView(text("VOCAL MASTER", 13, purple, true));
        TextView sub = text(subtitle, 27, white, true); sub.setPadding(0,8,0,18); content.addView(sub);
    }
    private void card(String title, String desc, String symbol, Runnable action) {
        LinearLayout c = column(); c.setPadding(17,15,17,15); c.setBackground(shape(panel,22));
        TextView icon = text(symbol,25,Color.rgb(196,181,253),true); c.addView(icon);
        TextView h = text(title,18,white,true); h.setPadding(0,7,0,4); c.addView(h);
        c.addView(text(desc,13,muted,false));
        c.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=12; content.addView(c,p);
    }
    private void button(String label, Runnable action) {
        TextView b = text(label,15,white,true); b.setGravity(Gravity.CENTER); b.setPadding(16,15,16,15); b.setBackground(shape(purple,18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; content.addView(b,p); b.setOnClickListener(v -> action.run());
    }
    private void showHome() {
        stopTuner(); base("Temukan suara terbaikmu.");
        TextView hero = text("🎙️  LATIHAN VOKAL OFFLINE\nLatih teknik, ukur nada, dan pantau perkembanganmu — langsung di perangkat.",15,white,false);
        hero.setPadding(18,20,18,20); hero.setBackground(shape(Color.rgb(35,29,70),22)); content.addView(hero);
        TextView stats = text("  "+sessions+" sesi latihan tersimpan di perangkat",13,muted,false); stats.setPadding(0,15,0,16); content.addView(stats);
        card("🎯  Pitch Detector","Deteksi nada suara secara real-time dari mikrofon","♫",this::showTuner);
        card("🌬️  Latihan Pernapasan","Latihan napas diafragma dengan panduan waktu","◎",()->showExercise("Latihan Pernapasan","Tarik napas perlahan melalui hidung selama 4 hitungan. Tahan 2 hitungan, lalu hembuskan selama 6 hitungan.",4,2,6));
        card("🎵  Vocal Warm-up","Pemanasan ringan sebelum bernyanyi","✦",()->showExercise("Vocal Warm-up","Mulai dengan humming lembut, lanjut lip trill, lalu naikkan nada secara bertahap. Jangan memaksakan tenggorokan.",3,3,5));
        card("🎹  Piano Virtual","Mainkan nada referensi untuk latihan pitch","▤",this::showPiano);
        card("📈  Tes Jangkauan Vokal","Cari nada terendah dan tertinggi yang nyaman","↗",this::showRange);
        card("📚  Materi & Teknik Vokal","Dasar sampai lanjutan: resonansi, register, vibrato","▣",this::showLessons);
        content.addView(text("Semua fitur inti berjalan offline. Audio mikrofon diproses di perangkat.",12,muted,false));
    }
    private void showTuner() {
        base("Pitch Detector");
        pitchText=text("-- Hz",48,white,true); pitchText.setGravity(Gravity.CENTER); content.addView(pitchText);
        noteText=text("Belum ada nada",20,purple,true); noteText.setGravity(Gravity.CENTER); content.addView(noteText);
        statusText=text("Izinkan mikrofon, lalu mulai bernyanyi dengan nada stabil.",13,muted,false); statusText.setGravity(Gravity.CENTER); statusText.setPadding(0,12,0,20); content.addView(statusText);
        button("MULAI DETEKSI",()->startTuner()); button("BERHENTI",()->stopTuner()); button("KEMBALI",()->showHome());
    }
    private void startTuner() {
        if (listening) return;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},77); return;
        }
        int rate=44100, min=AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if (min<=0) { statusText.setText("Mikrofon tidak tersedia pada perangkat ini."); return; }
        try { recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,8192)); recorder.startRecording(); }
        catch(Exception e) { statusText.setText("Tidak dapat mengakses mikrofon."); return; }
        listening=true; statusText.setText("Mendengarkan… nyanyikan satu nada dengan stabil.");
        audioThread=new Thread(()->{
            short[] data=new short[4096];
            while(listening) {
                int n=recorder.read(data,0,data.length);
                if(n>1024) { double hz=detect(data,n,rate); if(hz>55 && hz<1200) {
                    final double f=hz; final String note=noteName(f);
                    handler.post(()->{ if(pitchText!=null){ pitchText.setText(String.format(Locale.US,"%.1f Hz",f)); noteText.setText(note); }});
                }}
            }
        }); audioThread.start();
    }
    private double detect(short[] x,int n,int sr) {
        double rms=0; for(int i=0;i<n;i++) rms+=(double)x[i]*x[i]; rms=Math.sqrt(rms/n); if(rms<250) return 0;
        int minLag=sr/1200, maxLag=Math.min(sr/55,n/2), best=0; double bestCorr=0;
        for(int lag=minLag;lag<=maxLag;lag+=1) { double sum=0; for(int i=0;i<n-lag;i+=3) sum+=(double)x[i]*x[i+lag]; if(sum>bestCorr){bestCorr=sum;best=lag;} }
        return best>0?(double)sr/best:0;
    }
    private String noteName(double hz) {
        String[] names={"C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"};
        int midi=(int)Math.round(69+12*Math.log(hz/440.0)/Math.log(2)); int cents=(int)Math.round((69+12*Math.log(hz/440.0)/Math.log(2)-midi)*100);
        return names[(midi%12+12)%12]+" "+(midi/12-1)+"   "+(cents>=0?"+":"")+cents+" cents";
    }
    private void stopTuner() {
        listening=false;
        try { if(recorder!=null){recorder.stop();recorder.release();recorder=null;} } catch(Exception ignored) {}
        if(audioThread!=null){audioThread=null;}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==77 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) startTuner();
        else if(request==77 && statusText!=null) statusText.setText("Izin mikrofon diperlukan untuk deteksi nada.");
    }
    private void showExercise(String title,String guide,int inhale,int hold,int exhale) {
        base(title); content.addView(text(guide,16,white,false));
        TextView timer=text("Siap berlatih",34,purple,true); timer.setGravity(Gravity.CENTER); timer.setPadding(0,35,0,25); content.addView(timer);
        button("MULAI 1 PUTARAN",()->{ timer.setText("Tarik napas  "+inhale+" dtk"); handler.postDelayed(()->timer.setText("Tahan  "+hold+" dtk"),inhale*1000L); handler.postDelayed(()->timer.setText("Hembuskan  "+exhale+" dtk"),(inhale+hold)*1000L); handler.postDelayed(()->{timer.setText("Bagus! Satu putaran selesai.");sessions++;},(inhale+hold+exhale)*1000L); });
        button("KEMBALI",()->showHome());
    }
    private void showPiano() {
        base("Piano Virtual"); content.addView(text("Sentuh nada untuk mendengar referensi pitch.",14,muted,false));
        String[] names={"C","D","E","F","G","A","B","C5"};
        for(int i=0;i<names.length;i++){ final int midi=60+(i==7?12:i==6?11:i==5?9:i==4?7:i==3?5:i==2?4:i==1?2:0); button("♪   "+names[i],()->playTone(440*Math.pow(2,(midi-69)/12.0))); }
        button("KEMBALI",()->showHome());
    }
    private void playTone(double hz) {
        new Thread(()->{ int sr=44100,duration=700,count=sr*duration/1000; short[] samples=new short[count]; for(int i=0;i<count;i++) samples[i]=(short)(Math.sin(2*Math.PI*hz*i/sr)*5000*Math.min(1.0,Math.min(i/500.0,(count-i)/500.0)));
            AudioTrack t=new AudioTrack.Builder().setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(sr).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(count*2).build();
            t.play(); t.write(samples,0,count); t.stop(); t.release();
        }).start();
    }
    private void showRange() {
        base("Tes Jangkauan Vokal");
        content.addView(text("Gunakan Pitch Detector untuk mencoba nada terendah dan tertinggi yang masih nyaman. Jangan memaksa suara atau mengejar nada yang terasa sakit.",16,white,false));
        card("1. Nada rendah","Mulai dari suara bicara, turunkan perlahan, catat nada stabil terendah.","↓",this::showTuner);
        card("2. Nada tinggi","Naikkan nada bertahap dengan volume lembut dan tanpa menekan tenggorokan.","↑",this::showTuner);
        card("3. Evaluasi","Jangkauan nyaman lebih penting daripada nada ekstrem. Ulangi tes di hari berbeda.","✓",()->showExercise("Evaluasi vokal","Catat nada yang terasa paling nyaman dan mudah diulang.",3,2,4));
        button("KEMBALI",()->showHome());
    }
    private void showLessons() {
        base("Materi & Teknik Vokal");
        String[][] lessons={{"Postur & napas","Berdiri rileks, bahu tidak terangkat. Rasakan gerak perut dan rusuk bawah saat bernapas."},{"Humming","Tutup bibir dengan ringan, buat dengungan nyaman tanpa menekan rahang."},{"Lip trill","Getarkan bibir dengan aliran udara stabil. Hentikan bila terasa tegang."},{"Chest voice","Latih nada bicara di rentang nyaman. Hindari mendorong volume."},{"Head voice & falsetto","Eksplorasi nada tinggi dengan ringan; jangan memaksa atau menekan leher."},{"Mixed voice","Bangun transisi register secara bertahap dengan latihan vokal ringan."},{"Vibrato","Latih nada panjang yang stabil dan rileks. Vibrato alami tidak perlu digoyang paksa."}};
        for(String[] l:lessons) card(l[0],l[1],"✧",()->showExercise(l[0],l[1],4,2,6));
        button("KEMBALI",()->showHome());
    }
    @Override protected void onDestroy(){stopTuner();super.onDestroy();}
}
