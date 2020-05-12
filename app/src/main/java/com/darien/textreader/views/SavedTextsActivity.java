package com.darien.textreader.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.darien.textreader.R;
import com.darien.textreader.adapters.SearchTextAdapter;
import com.darien.textreader.models.TextSaved;
import com.darien.textreader.viewmodels.SavedTextsViewModel;

import java.util.ArrayList;

public class SavedTextsActivity extends AppCompatActivity {
    private SearchView searchView;
    private SearchTextAdapter adapter;
    private RecyclerView numbersRecyclerView;
    private TextView tvNoNumbers;
    private SavedTextsViewModel savedTextsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_texts);
        initVariables();
    }

    private void initVariables(){
        numbersRecyclerView = findViewById(R.id.phone_results_recycler_view);
        tvNoNumbers = findViewById(R.id.no_number_text_view);
        savedTextsViewModel = new SavedTextsViewModel(this);
        final Observer<ArrayList<TextSaved>> textSavedObserver = (results) ->{
            showTVNoNumbers(results.size());
            adapter.setTexts(results);
            adapter.notifyDataSetChanged();
        };
        savedTextsViewModel.getTexts().observe(this, textSavedObserver);
        //inicializando los elementos de la lista
        adapter = new SearchTextAdapter(this, new SearchTextAdapter.TextsListListener() {
            @Override
            public void onTextClicked(String text) {
                //llamar si se toca un numero en la lista
                Intent intent = new Intent(SavedTextsActivity.this, ReadCurrentTextActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("canSave", false);
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(String text) {
                //borrar si se toca el boton de delete en la lista
                showDeleteAlert(text);
            }
        });

        savedTextsViewModel.getAllTexts();
        numbersRecyclerView.setAdapter(adapter);
        numbersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //creando el buscador
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //buscar los elementos en la lista cuando se va tecleando un numero
                if (s.length() > 0){
                    savedTextsViewModel.searchText(s);
                }else {
                    savedTextsViewModel.getAllTexts();
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //Metodo para mostrar el texto de No hay numeros guardados si no hay elemetos a mostrar en la lista
    private void showTVNoNumbers(int size){
        if (size <= 0){
            tvNoNumbers.setVisibility(View.VISIBLE);
        }else {
            tvNoNumbers.setVisibility(View.GONE);
        }
    }

    //alerta para eliminar elementos
    private void showDeleteAlert(String text){
        String alertText = getResources().getString(R.string.sure_to_delete);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(alertText)
                .setTitle(getResources().getString(R.string.text_alert))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.text_accept), (dialog, id) -> {
                    //eliminar elementos de BD y refrescar la ista
                    savedTextsViewModel.deleteText(text);
                    savedTextsViewModel.getAllTexts();
                    searchView.setQuery("", false);
                    dialog.dismiss();
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, id) ->{
                    dialog.dismiss();
                });
        try {
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
