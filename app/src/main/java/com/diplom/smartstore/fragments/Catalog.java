package com.diplom.smartstore.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diplom.smartstore.R;
import com.diplom.smartstore.adapters.CatalogAdapter;
import com.diplom.smartstore.model.Attribute;
import com.diplom.smartstore.model.Category;
import com.diplom.smartstore.model.Subcategory;
import com.diplom.smartstore.utils.Http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Catalog extends Fragment {

    RecyclerView catalogRecycler;
    Context context;
    View view;
    List<Category> categoryList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // load data
        getCatalog();

        return view;
    }

    private void getCatalog() {
        String url = getString(R.string.api_server) + getString(R.string.getCategories);

        Thread request = new Thread() {
            @Override
            public void run() {
                if (isAdded()) {
                    Http http = new Http(getActivity(), url);//getActivity изза фрагмента вместо активити
                    http.setToken(true);
                    http.send();
                    if (isAdded()) {
                        //getActivity изза фрагмента вместо активити
                        requireActivity().runOnUiThread(() -> {
                            Integer code = http.getStatusCode();
                            if (code == 200) {
                                try {
                                    // получаем JSON ответ
                                    JSONArray response = new JSONArray(http.getResponse());

                                    // перебираем массив
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject category = response.getJSONObject(i); // категория
                                        JSONArray subcategories = category.getJSONArray("subcategories"); // подкатегории
                                        List<Subcategory> subcategoryList = new ArrayList<>();

                                        for (int j = 0; j < subcategories.length(); j++) {
                                            JSONObject subcategory = subcategories.getJSONObject(j); // подкатегория
                                            JSONArray subcategoryAttributes = subcategory.getJSONArray("attributes"); // список аттрибутов подкатегории

                                            List<Attribute> attributesSubcategory = new ArrayList<>();

                                            for (int p = 0; p < subcategoryAttributes.length(); p++) {
                                                // добавляем аттрибут в массив аттрибутов подкатегории
                                                attributesSubcategory.add(new Attribute(p, subcategoryAttributes.get(p).toString(), null));
                                            }

                                            // добавляем подктегорию в массив подктегорий категории
                                            subcategoryList.add(new Subcategory(
                                                    subcategory.getInt("id"),
                                                    subcategory.getString("name"),
                                                    subcategory.getString("slug"),
                                                    subcategory.getString("description"),
                                                    subcategory.getString("image_url"),
                                                    attributesSubcategory));


                                        }
                                        // добавляем ктегорию в массив категорий
                                        categoryList.add(new Category(
                                                category.getInt("id"),
                                                category.getString("name"),
                                                category.getString("slug"),
                                                category.getString("description"),
                                                subcategoryList));

                                    }

                                    // Add the following lines to create RecyclerView
                                    catalogRecycler = view.findViewById(R.id.categoriesRecyclerView);
                                    catalogRecycler.setHasFixedSize(true);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                                            RecyclerView.VERTICAL, false);
                                    catalogRecycler.setLayoutManager(layoutManager);
                                    catalogRecycler.setAdapter(new CatalogAdapter(context, categoryList, getActivity()));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                alertFail("Error " + code);
                            }
                        });
                    }
                }
            }
        };
        request.start();
    }

    private void alertFail(String s) {
        new AlertDialog.Builder(getActivity())
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void alertSuccess(String s) {
        new AlertDialog.Builder(getActivity())
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }
}
