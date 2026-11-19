package id.vocalmaster.offline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg=0xFF080B18, panel=0xFF141A2E, panel2=0xFF1C2340, purple=0xFF9B7BFF, cyan=0xFF66D9F2, white=0xFFF7F7FF, muted=0xFFA5AEC8;
    private LinearLayout page, body;
    private TextView hzView,noteView,statusView;
    private AudioRecord recorder; private volatile boolean listening=false; private Thread audioThread;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int sessions=0;
    private Runnable backAction=null;
    private boolean onHome=true;

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);home();}
    private GradientDrawable shape(int c,int r,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c,blend(c,0xFF11162B)});d.setCornerRadius(r);d.setStroke(1,stroke);return d;}
    private int blend(int a,int b){return Color.rgb((Color.red(a)+Color.red(b))/2,(Color.green(a)+Color.green(b))/2,(Color.blue(a)+Color.blue(b))/2);}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    private void startPage(String title,String eyebrow){
        stopMic(); onHome=false; backAction=()->home(); page=col();page.setBackgroundColor(bg);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);body=col();body.setPadding(20,18,20,28);sc.addView(body);page.addView(sc);setContentView(page);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);nav.setPadding(0,0,0,12); TextView back=txt("‹  KEMBALI",13,cyan,true);back.setPadding(12,10,18,10);back.setBackground(shape(0xFF202943,16,0xFF53618A));back.setElevation(7);back.setOnClickListener(v->goBack());nav.addView(back);TextView brand=txt("     GV  /  VOKAL MASTER",12,purple,true);nav.addView(brand);body.addView(nav);
        TextView head=txt(title,28,white,true);head.setPadding(0,9,0,2);body.addView(head);
        TextView sub=txt(eyebrow,12,muted,false);sub.setPadding(0,0,0,18);body.addView(sub);
    }
    private void label(String s){TextView t=txt(s.toUpperCase(Locale.ROOT),11,muted,true);t.setPadding(2,16,2,10);body.addView(t);}
    private void card(String icon,String title,String desc,Runnable run){
        LinearLayout c=col();c.setPadding(18,17,18,17);c.setBackground(shape(0xFF1B2440,22,0xFF53618A));c.setElevation(9f);
        TextView ic=txt(icon,24,purple,true);c.addView(ic);TextView h=txt(title,17,white,true);h.setPadding(0,9,0,4);c.addView(h);c.addView(txt(desc,12,muted,false)); TextView hint=txt("BUKA MODUL  ›",10,cyan,true);hint.setPadding(0,12,0,0);c.addView(hint);
        c.setOnClickListener(v->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=11;body.addView(c,p);
    }
    private void action(String s,Runnable r){TextView b=txt(s,14,white,true);b.setGravity(Gravity.CENTER);b.setPadding(15,15,15,15);b.setBackground(shape(0xFF7656D8,18,0xFFC2B2FF));b.setElevation(8f);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=10;body.addView(b,p);b.setOnClickListener(v->r.run());}
    private void home(){
        onHome=true;backAction=null; startPage("Train your voice.","YOUR PERSONAL OFFLINE VOCAL COACH"); onHome=true; backAction=null;
        LinearLayout hero=col();hero.setPadding(19,20,19,20);hero.setBackground(shape(0xFF27204B,25,0xFF594A91));
        TextView pill=txt("✦  VOCAL PERFORMANCE STUDIO",10,cyan,true);hero.addView(pill);
        TextView h=txt("Temukan\nsuara terbaikmu.",29,white,true);h.setPadding(0,13,0,7);hero.addView(h);
        hero.addView(txt("Latih teknik. Kenali nada. Bangun karakter vokal.",13,0xFFD5D2EA,false));
        TextView start=txt("  ▶  MULAI LATIHAN",13,white,true);start.setPadding(0,20,0,0);hero.addView(start);body.addView(hero);
        LinearLayout stats=new LinearLayout(this);stats.setPadding(0,16,0,7);
        stats.addView(stat("0"+sessions,"SESI LATIHAN"));stats.addView(stat("OFFLINE","MODE PRIVAT"));body.addView(stats);
        label("Studio latihan");
        card("◉","Pitch Analyzer","Deteksi frekuensi, nama nada, dan kestabilan pitch.",this::tuner);
        card("✺","Vocal Identity","Kenali perkiraan tipe suara, jangkauan vokal, karakter, dan rekomendasi genre.",this::identity);
        card("◌","Breathing Coach","Latihan napas terarah untuk kontrol dan stamina.",()->exercise("Breathing Coach","Tarik napas lembut 4 hitungan. Tahan 2 hitungan, lalu hembuskan 6 hitungan.",4,2,6));
        card("✧","Vocal Warm-up","Humming, lip trill, dan pemanasan rentang nada.",()->exercise("Vocal Warm-up","Mulai humming ringan, lanjut lip trill, lalu siren lembut. Jangan memaksa tenggorokan.",4,2,5));
        card("♫","Virtual Piano","Nada referensi untuk membantu latihan intonasi.",this::piano);
        card("⌁","Vocal Range Test","Eksplorasi nada rendah dan tinggi yang nyaman.",this::range);
        card("▤","Vocal Academy","Materi teknik vokal dari dasar hingga lanjutan.",this::lessons);
        label("Informasi");
        TextView credit=txt("GV Vokal Master  •  By Rakana\nDibuat untuk latihan vokal offline. Audio diproses langsung di perangkat.",11,muted,false);credit.setGravity(Gravity.CENTER);credit.setPadding(8,14,8,10);body.addView(credit);
    }
    private View stat(String value,String caption){LinearLayout box=col();box.setPadding(14,12,14,12);box.setBackground(shape(panel,17,0xFF303956));TextView v=txt(value,17,white,true);box.addView(v);box.addView(txt(caption,9,muted,true));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(0,0,8,0);box.setLayoutParams(p);return box;}
    private void identity(){
        startPage("Vocal Identity","DISCOVER YOUR VOICE PROFILE");
        LinearLayout hero=col();hero.setPadding(17,18,17,18);hero.setBackground(shape(0xFF27204B,22,0xFF594A91));
        hero.addView(txt("✺  VOICE PROFILE SCAN",11,cyan,true));
        TextView intro=txt("Kenali karakter suaramu.",23,white,true);intro.setPadding(0,10,0,7);hero.addView(intro);
        hero.addView(txt("Nyanyikan nada rendah, nada tengah, lalu nada tinggi yang terasa nyaman selama proses pemindaian. Hasil dihitung langsung di ponsel.",13,0xFFD5D2EA,false));body.addView(hero);
        label("Yang akan dianalisis");
        card("↕","Rentang nada","Perkiraan nada terendah dan tertinggi yang berhasil terdeteksi.",()->{});
        card("♫","Kategori vokal","Perkiraan bass, baritone, tenor, contralto, mezzo-soprano, atau soprano berdasarkan rentang.",()->{});
        card("✧","Rekomendasi genre","Ide genre yang mungkin cocok untuk dieksplorasi, bukan batasan kemampuan bernyanyi.",()->{});
        TextView result=txt("Belum ada hasil pemindaian.",15,white,true);result.setPadding(15,18,15,18);result.setBackground(shape(panel,18,0xFF303956));body.addView(result);
        action("◉  MULAI SCAN SUARA  •  12 DETIK",()->scanIdentity(result));
        action("KEMBALI KE MENU",this::home);
    }
    private void scanIdentity(TextView result){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},78);return;}
        result.setText("Sedang merekam… nyanyikan nada nyaman dari rendah ke tinggi. Jangan memaksa suara.");
        new Thread(()->{
            int sr=44100,min=AudioRecord.getMinBufferSize(sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            AudioRecord rec=null;java.util.ArrayList<Double> pitches=new java.util.ArrayList<>();
            try{
                rec=new AudioRecord(MediaRecorder.AudioSource.MIC,sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,16384));rec.startRecording();
                short[] data=new short[4096];long end=System.currentTimeMillis()+12000;
                while(System.currentTimeMillis()<end){
                    int n=rec.read(data,0,data.length);if(n>1024){double f=detect(data,n,sr);if(f>=65&&f<=1000){pitches.add(f);}}
                }
            }catch(Exception ignored){}finally{if(rec!=null){try{rec.stop();}catch(Exception ignored){}rec.release();}}
            final String report;
            if(pitches.size()<12) report="Suara belum terbaca dengan cukup jelas. Coba lagi di ruangan tenang, dekatkan ponsel, dan nyanyikan nada stabil.";
            else{
                double low=percentile(new java.util.ArrayList<>(pitches),0.05),high=percentile(new java.util.ArrayList<>(pitches),0.95);
                int lo=(int)Math.round(69+12*Math.log(low/440)/Math.log(2));
                int hi=(int)Math.round(69+12*Math.log(high/440)/Math.log(2));
                String type;
                if(lo<=40&&hi<=62)type="Bass (perkiraan)";
                else if(lo<=45&&hi<=67)type="Baritone (perkiraan)";
                else if(lo<=49&&hi>=68)type="Tenor (perkiraan)";
                else if(lo<=53&&hi<=74)type="Contralto (perkiraan)";
                else if(lo<=57&&hi>=74)type="Mezzo-soprano (perkiraan)";
                else if(hi>=76)type="Soprano (perkiraan)";
                else type="Rentang vokal menengah (perlu tes lanjutan)";
                String genres;
                if(type.startsWith("Bass")||type.startsWith("Baritone"))genres="Pop mellow, jazz, blues, soul, country, dan lagu bernada rendah.";
                else if(type.startsWith("Tenor"))genres="Pop, rock, musical theatre, R&B, dan balada.";
                else if(type.startsWith("Contralto"))genres="Jazz, soul, blues, pop akustik, dan ballad.";
                else if(type.startsWith("Mezzo"))genres="Pop, R&B, soul, musical theatre, dan jazz.";
                else if(type.startsWith("Soprano"))genres="Pop, musical theatre, klasik, folk, dan balada bernada tinggi.";
                else genres="Coba pop, akustik, jazz, atau genre yang nyaman di tessitura suaramu.";
                report="HASIL VOCAL IDENTITY\n\nKandidat rentang (belum klasifikasi final): "+type+"\nRentang inti terdeteksi (persentil 5–95): "+midiName(lo)+" – "+midiName(hi)+"\nJumlah sampel suara: "+pitches.size()+"\n\nIDE GENRE UNTUK DIEKSPLORASI\n"+genres+"\n\nCatatan akurasi: YIN mengurangi kesalahan deteksi oktaf dibanding metode sebelumnya. Rentang memakai persentil agar satu nada salah tidak langsung menentukan hasil. Namun kategori ini masih kandidat berbasis nada saja. Klasifikasi vokal yang dapat dipercaya perlu tes tessitura (nada yang nyaman dinyanyikan berulang), transisi register/passaggio, warna suara, dan evaluasi beberapa sesi. Genre bukan ditentukan oleh tipe suara saja.";
            }
            final String out=report;handler.post(()->result.setText(out));
        }).start();
    }
    private String midiName(int m){String[] ns={"C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"};return ns[(m%12+12)%12]+(m/12-1);}
    private void tuner(){
        startPage("Pitch Analyzer","REAL-TIME VOICE ANALYSIS");
        LinearLayout meter=col();meter.setGravity(Gravity.CENTER);meter.setPadding(15,28,15,28);meter.setBackground(shape(0xFF171C35,25,0xFF48406F));
        meter.addView(txt("LIVE FREQUENCY",10,muted,true));hzView=txt("--.-",54,white,true);hzView.setPadding(0,10,0,0);meter.addView(hzView);
        noteView=txt("Hz  •  Menunggu suara",17,purple,true);meter.addView(noteView);statusView=txt("Nyalakan deteksi lalu nyanyikan satu nada stabil.",12,muted,false);statusView.setGravity(Gravity.CENTER);statusView.setPadding(10,14,10,0);meter.addView(statusView);body.addView(meter);
        label("Kontrol");action("▶  MULAI DETEKSI",this::startMic);action("■  BERHENTI",this::stopMic);action("←  KEMBALI KE MENU",this::home);
    }
    private void startMic(){
        if(listening)return;
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},77);return;}
        int sr=44100,min=AudioRecord.getMinBufferSize(sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        try{recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,8192));recorder.startRecording();}catch(Exception e){statusView.setText("Mikrofon tidak dapat diakses.");return;}
        listening=true;statusView.setText("Mendengarkan suara…");
        audioThread=new Thread(()->{
            short[] data=new short[4096];
            java.util.ArrayDeque<Double> recent=new java.util.ArrayDeque<>();
            while(listening){
                AudioRecord r=recorder;if(r==null)break;
                int n;try{n=r.read(data,0,data.length);}catch(Exception e){break;}
                if(n>1024){
                    double f=detect(data,n,sr);
                    if(f>55&&f<1200){
                        recent.addLast(f);if(recent.size()>15)recent.removeFirst();
                        java.util.ArrayList<Double> sorted=new java.util.ArrayList<>(recent);
                        java.util.Collections.sort(sorted);
                        double smooth=sorted.get(sorted.size()/2);
                        double midi=69+12*Math.log(smooth/440.0)/Math.log(2);
                        double nearest=Math.rint(midi),cents=(midi-nearest)*100.0;
                        java.util.ArrayList<Double> deviations=new java.util.ArrayList<>();
                        for(double value:recent){double m=69+12*Math.log(value/440.0)/Math.log(2);deviations.add(Math.abs((m-nearest)*100.0));}
                        java.util.Collections.sort(deviations);
                        double stability=deviations.get(deviations.size()/2);
                        final double hz=smooth,stable=stability,offset=cents;
                        final String note=noteName(smooth);
                        handler.post(()->{if(hzView!=null){
                            hzView.setText(String.format(Locale.US,"%.1f Hz",hz));noteView.setText(note);
                            String quality=stable<10?"stabil":stable<25?"cukup stabil":"belum stabil";
                            statusView.setText(String.format(Locale.US,"Selisih nada: %+.0f cents  •  Variasi median: %.0f cents  •  %s",offset,stable,quality));
                        }});
                    } else if(recent.size()>0){recent.clear();handler.post(()->{if(statusView!=null)statusView.setText("Sinyal belum jelas. Coba nada tunggal dengan suara stabil.");});}
                }
            }
        });audioThread.start();
    }
    // YIN pitch estimator: normalized difference function reduces octave errors
    // compared with selecting the largest raw autocorrelation peak.
    private double detect(short[] x,int n,int sr){
        if(n<1024)return 0;
        int size=Math.min(n,4096), minLag=Math.max(2,sr/1000), maxLag=Math.min(size/2,sr/60);
        double mean=0;for(int i=0;i<size;i++)mean+=x[i];mean/=size;
        double rms=0;for(int i=0;i<size;i++){double v=x[i]-mean;rms+=v*v;}
        rms=Math.sqrt(rms/size);if(rms<180)return 0;
        double[] diff=new double[maxLag+1];
        for(int tau=1;tau<=maxLag;tau++){
            double sum=0;
            for(int i=0;i<size-maxLag;i+=2){double a=x[i]-mean,b=x[i+tau]-mean;double d=a-b;sum+=d*d;}
            diff[tau]=sum;
        }
        double running=0;double[] cmnd=new double[maxLag+1];cmnd[0]=1;
        for(int tau=1;tau<=maxLag;tau++){running+=diff[tau];cmnd[tau]=running>0?diff[tau]*tau/running:1;}
        int chosen=-1;
        for(int tau=minLag;tau<=maxLag;tau++){
            if(cmnd[tau]<0.15){while(tau+1<=maxLag&&cmnd[tau+1]<cmnd[tau])tau++;chosen=tau;break;}
        }
        if(chosen<0){double best=1;for(int tau=minLag;tau<=maxLag;tau++)if(cmnd[tau]<best){best=cmnd[tau];chosen=tau;}if(best>0.32)return 0;}
        double refined=chosen;
        if(chosen>1&&chosen<maxLag){
            double a=cmnd[chosen-1],b=cmnd[chosen],c=cmnd[chosen+1],den=2*(2*b-a-c);
            if(Math.abs(den)>1e-9)refined+= (a-c)/den;
        }
        double hz=sr/refined;
        return hz>=60&&hz<=1000?hz:0;
    }
    private double percentile(java.util.ArrayList<Double> values,double q){
        java.util.Collections.sort(values);
        int i=(int)Math.round((values.size()-1)*q);
        return values.get(Math.max(0,Math.min(values.size()-1,i)));
    }
    private String noteName(double f){String[] ns={"C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B"};double m=69+12*Math.log(f/440)/Math.log(2);int midi=(int)Math.round(m),c=(int)Math.round((m-midi)*100);return ns[(midi%12+12)%12]+" "+(midi/12-1)+"   "+(c>=0?"+":"")+c+" cents";}
    private void stopMic(){listening=false;AudioRecord old=recorder;recorder=null;if(old!=null){try{old.stop();}catch(Exception ignored){}try{old.release();}catch(Exception ignored){}}audioThread=null;}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==77&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startMic();else if(r==77&&statusView!=null)statusView.setText("Izin mikrofon dibutuhkan.");else if(r==78)home();}
    private void exercise(String title,String guide,int inhale,int hold,int exhale){
        Runnable returnPage=backAction; startPage(title,"GUIDED VOCAL SESSION"); backAction=returnPage; card("✦","Panduan latihan",guide,()->{});TextView timer=txt("READY",36,purple,true);timer.setGravity(Gravity.CENTER);timer.setPadding(0,28,0,28);body.addView(timer);
        action("MULAI 1 PUTARAN",()->{timer.setText("TARIK  "+inhale+"s");handler.postDelayed(()->timer.setText("TAHAN  "+hold+"s"),inhale*1000L);handler.postDelayed(()->timer.setText("HEMBUS  "+exhale+"s"),(inhale+hold)*1000L);handler.postDelayed(()->{timer.setText("SELESAI  ✓");sessions++;},(inhale+hold+exhale)*1000L);});action("KEMBALI",this::home);
    }
    private void piano(){
        startPage("Virtual Piano","REFERENCE NOTES • 4TH OCTAVE");label("Pilih nada");
        String[] names={"C4","D4","E4","F4","G4","A4","B4","C5"};int[] semis={0,2,4,5,7,9,11,12};
        for(int i=0;i<names.length;i++){final int midi=60+semis[i];TextView key=txt(names[i]+"     ♫",17,white,true);key.setGravity(Gravity.CENTER);key.setPadding(10,17,10,17);key.setBackground(shape(0xFF252B43,13,0xFF555E80));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=7;body.addView(key,p);key.setOnClickListener(v->tone(440*Math.pow(2,(midi-69)/12.0)));}
        action("KEMBALI",this::home);
    }
    private void tone(double hz){new Thread(()->{int sr=44100,n=sr*650/1000;short[] a=new short[n];for(int i=0;i<n;i++)a[i]=(short)(Math.sin(2*Math.PI*hz*i/sr)*4500*Math.min(1,Math.min(i/400.0,(n-i)/400.0)));AudioTrack t=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(sr).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(n*2).build();try{t.play();t.write(a,0,n);t.stop();}finally{t.release();}}).start();}
    private void range(){startPage("Vocal Range Test","FIND YOUR COMFORTABLE RANGE");card("01  ↓","Nada terendah","Turunkan nada perlahan dari suara bicara. Catat nada yang stabil dan nyaman.",this::tuner);card("02  ↑","Nada tertinggi","Naikkan nada secara bertahap dengan volume lembut. Jangan menekan leher.",this::tuner);card("03  ✓","Evaluasi","Rentang nyaman lebih penting daripada nada ekstrem. Hentikan jika terasa sakit.",()->exercise("Evaluasi","Catat nada yang paling mudah diulang.",3,2,4));action("KEMBALI",this::home);}
    private void lessons(){startPage("Vocal Academy","LEARN • PRACTICE • IMPROVE");String[][] data={{"Postur & napas","Berdiri rileks. Bahu tidak terangkat; rasakan gerak rusuk bawah."},{"Humming","Dengungkan nada nyaman dengan bibir tertutup ringan."},{"Lip Trill","Getarkan bibir dengan aliran udara stabil tanpa menegangkan rahang."},{"Chest Voice","Latih nada bicara di rentang nyaman tanpa mendorong volume."},{"Head Voice & Falsetto","Eksplorasi nada tinggi dengan ringan, tanpa menekan leher."},{"Mixed Voice","Latih transisi register bertahap dengan vokal ringan."},{"Vibrato","Pertahankan nada panjang yang stabil dan rileks."}};for(String[] d:data)card("✧",d[0],d[1],()->exercise(d[0],d[1],4,2,6));action("KEMBALI",this::home);}
    private void goBack(){ if(backAction!=null){Runnable target=backAction;backAction=null;stopMic();target.run();}else home(); }
    @Override public void onBackPressed(){
        if(!onHome){goBack();return;}
        new android.app.AlertDialog.Builder(this).setTitle("Keluar dari GV Vokal Master?")
            .setMessage("Yakin ingin menutup aplikasi?")
            .setNegativeButton("Batal",(d,w)->d.dismiss())
            .setPositiveButton("Keluar",(d,w)->{stopMic();finish();}).show();
    }
    @Override protected void onDestroy(){stopMic();super.onDestroy();}
}