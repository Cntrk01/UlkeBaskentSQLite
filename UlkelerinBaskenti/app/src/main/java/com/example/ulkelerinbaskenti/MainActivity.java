package com.example.ulkelerinbaskenti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.ulkelerinbaskenti.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    SQLiteDatabase database;
    ArrayList<Ulke> arrayList;
    UlkeAdapter ulkeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        arrayList=new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ulkeAdapter=new UlkeAdapter(arrayList);
        binding.recyclerView.setAdapter(ulkeAdapter);

        getData();
    }
    public void getData(){
        try {
            //18Birebir isim aynı olmalıdır
            database=this.openOrCreateDatabase("Ulke",MODE_PRIVATE,null);
            //ulke isimi INSERT INTO içindeki isimdir!!
            Cursor cursor=database.rawQuery("SELECT*FROM ulke",null);
            //19)Veritabanı oluştururkenki kullandıgımız isimleri aldık !!
            int nameIx=cursor.getColumnIndex("ulkeIsimi");
            //19.1)ID alma sebebim kullanıcının eklediği her verinin idsi var.Hangisine tıklarsak o tıkladığımıza gidip onun özelliklerini bize getirmelidir.
            //Yani id üzerinden verinin detayına gidicez..
            int idIx=cursor.getColumnIndex("id");
            while (cursor.moveToNext()){
                String name=cursor.getString(nameIx);
                int id=cursor.getInt(idIx);
                //20!!!!)Gelen verileri sınıfa referans verdiık ve onu bir değişkene atadım.
                //Onuda recyclerview'e göndericem ki kullanıcının ekranında göstermek için !!
                Ulke ulke=new Ulke(name,id);
                arrayList.add(ulke);
            }
            //ulkeAdapter.notifyDataSetChanged(); bunu burdada verebiliriz ama ben fonksiyon içerisinde verdim adapter sınıfında
            cursor.close(); //UNUTMAA!!
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mn=getMenuInflater();
        mn.inflate(R.menu.ulke_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.ulke_ekle){
            Intent intent=new Intent(this,DetailActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}