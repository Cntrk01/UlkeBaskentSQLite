package com.example.ulkelerinbaskenti;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.ulkelerinbaskenti.databinding.ActivityDetailBinding;
import com.example.ulkelerinbaskenti.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.security.Permission;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    //8izin aldıktan sonra ne yapacağız galeriden birşey seçtikten sonra ne olacak gibi durumlar için artık bunu kullanıyoruz.activityForResult kaldırıldı
    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitmek için
    ActivityResultLauncher<String> permissionLauncher; //9izin alırken string ifadeler için kullanıyoruz.izini istemek için
    Bitmap selectedImage;
    SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        database=this.openOrCreateDatabase("Ulke",MODE_PRIVATE,null); //VERİTABANINI OKUTMAYI UNUTTUGUM İÇİN ÇALIŞMADI BUNU UNUTMA !!
        Intent intent=getIntent();
        String info=intent.getStringExtra("info");//infoyu aldım
        if(info.equals("new")){
            //Birşey ekleyecek ondan dolayı bütün değerler boş olcak buton görünür olcak ki ekleme yapınca kayıt etsin
            binding.ulkeBaskentText.setText("");
            binding.ulkeBaskentYilText.setText("");
            binding.ulkeText.setText("");
            binding.kaydetButton.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            binding.imageView.setImageBitmap(selectImage);

        }
        else{
            //ekleme yoksa sadece göstericek.
            //tıklanınca gelen id değerini vurda aldık.yoksa def deger 0 olcak
            int artId=intent.getIntExtra("ulkeId",1);
            System.out.println(artId);  //calisiyor
            binding.kaydetButton.setVisibility(View.INVISIBLE);
            //Şimdi burda veritabanından verileri çekmemiz lazım ki neye tıklandıysa onun verileri gelsin
            try {
                //Burda artId'yi string dizisine çevirdik.Bu da ? yazan yerin yerine geçicektir.
                Cursor cursor=database.rawQuery("SELECT*FROM ulke WHERE id=?",new String[]{String.valueOf(artId)});
                int isimIx=cursor.getColumnIndex("ulkeIsimi");
                int baskentIx=cursor.getColumnIndex("ulkeBaskenti");
                int yilIx=cursor.getColumnIndex("ulkeBaskenti");
                int imageIx=cursor.getColumnIndex("image");


                while (cursor.moveToNext()){
                    binding.ulkeBaskentText.setText(cursor.getString(baskentIx));
                    binding.ulkeText.setText(cursor.getString(isimIx));
                    binding.ulkeBaskentYilText.setText(cursor.getString(yilIx));
                    //image bize byte dizisi vercek ben bunu bitmapa cevirmem gerekiyor.çünkü byte olarak kayıt ettım

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();


            }catch (Exception e){
                e.printStackTrace();
            }

        }
        registerLauncher();
    }

    //13=kaydet butonunun tıklanma methodu
    public void save(View view){
        String ulkeIsim=binding.ulkeText.getText().toString();
        String ulkeBaskent=binding.ulkeBaskentText.getText().toString();
        String ulkeYil=binding.ulkeBaskentYilText.getText().toString();
        //14)Gelen resmi küçük bir boyuta çevirmemiz gereklidir onun işlemini yapcaz.
        Bitmap smaller=makeSmallerBitmap(selectedImage,300);
        //15)Resmimizi 1 ve 0 a çevirmemiz gerekiyor ki veri tabanına kayıt edelim.
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smaller.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray= outputStream.toByteArray();
        try {
            //16Veritabanınıı oluşturduk
            database=this.openOrCreateDatabase("Ulke",MODE_PRIVATE,null);
            //16.1)Tablo oluşturdul.Dikkat resimler BLOB olarak tutulur!!!
            database.execSQL("CREATE TABLE IF NOT EXISTS ulke (id INTEGER PRIMARY KEY,ulkeIsimi VARCHAR,ulkeBaskenti VARCHAR,ulkeBaskentYili VARCHAR,image BLOB)");
            //16.1)Veri ekleyeceğiz sorgu ile değil çünkü girilecek olan VALUES degerlerini bilmiyoruz bunun için SqliteStatement kullanacz
            String sqlString="INSERT INTO ulke (ulkeIsimi,ulkeBaskenti,ulkeBaskentYili,image) VALUES(?,?,?,?) ";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            //16.2)Bunun avantajı var biz farklı değerler bağlayabiliytoruz örnegin biz bindString kullanıcaz. Ama istersek bindLong bindBlob da kullanabiliriz...,Ayrıca index 1 den başlar.Kullanıcıdan aldıggımız degerleri bind içine veriyoruz.
            sqLiteStatement.bindString(1,ulkeIsim);
            sqLiteStatement.bindString(2,ulkeBaskent);
            sqLiteStatement.bindString(3,ulkeYil);
            sqLiteStatement.bindBlob(4,byteArray); //kullanıcıdan aldıgımız görselin degiskeni
            sqLiteStatement.execute(); //16.3)en son execute yapıyoruz
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent=new Intent(DetailActivity.this,MainActivity.class); //17)Kullanıcı en son save butonuna bastı Main sınıfına geri döndük
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //!!!Bundan önceki ve bu aktivitenin hepsini kapat gittiğini aç yani yablzca main çalışıyor olucak.
        startActivity(intent);
    }
  //14)Bu fonksiyon büyük olan görsellerin eşit bir şekilde küçültülmesini sağlıyor boyuttan kazanmak için.Matematiksel bir algoritma var
    public Bitmap makeSmallerBitmap(Bitmap image,int maxSize){
        //resimin genişlik ve yüksekliğini aldık
        int widht=image.getWidth();
        int height=image.getHeight();
        //burada bunu oranladık ki 1 den büyükse kenar oranları görsel yatay degilse dikey oluyor.
        float bitmapRadio=(float) widht/(float) height;

        if(bitmapRadio>1){
            //Görsel yatay
            //Örneğin maxSize 200 girdik.Fotografın orjinal widhti 300 dü. Biz 300 e 2/3 yapınca 200 bulduk.O Zaman heightinde 3/2'sini almamız gerekiyor ki eşit miktarda bölünsün !!!
            widht=maxSize;
            height= (int) (widht/bitmapRadio);
        }else{
            //Görsel dikey
            height=maxSize;
            //Burada carptık zaten bitmapRadio 1 den küçük bir değer
            widht=(int) (height*bitmapRadio);
        }

        return  image.createScaledBitmap(image,widht,height,true);
    }

    //1image butonunun tıklanma methodu
    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ //izin verilmemiş mi ? diyoruz granted izin vermek
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Galeriye Erişim İçin İzin Gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //5izin isticek
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }

    private void  registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    //Bu bir intenttir .
                    Intent intentFromResult=result.getData();
                    if(intentFromResult !=null){
                        Uri imageData= intentFromResult.getData();
                        try {
                           if(Build.VERSION.SDK_INT>=28){
                               ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(),imageData);
                               selectedImage=ImageDecoder.decodeBitmap(source);
                               binding.imageView.setImageBitmap(selectedImage);
                           }else{
                               selectedImage=MediaStore.Images.Media.getBitmap(DetailActivity.this.getContentResolver(),imageData);
                               binding.imageView.setImageBitmap(selectedImage);
                           }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result==true){
                    //izin verildi
                    Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                }else{
                    //izin verilmedi
                    Toast.makeText(DetailActivity.this,"İzin Verilmedi",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}