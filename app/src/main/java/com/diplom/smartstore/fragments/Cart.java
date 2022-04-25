package com.diplom.smartstore.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diplom.smartstore.R;
import com.diplom.smartstore.adapters.CartAdapter;
import com.diplom.smartstore.adapters.WishlistAdapter;
import com.diplom.smartstore.model.Attribute;
import com.diplom.smartstore.model.Brand;
import com.diplom.smartstore.model.Category;
import com.diplom.smartstore.model.Product;
import com.diplom.smartstore.model.Subcategory;
import com.diplom.smartstore.utils.Http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Cart extends Fragment implements CartAdapter.OnProductListener {

    private static final String TAG = "onClick";
    CartAdapter cartAdapter;
    RecyclerView cartRecycler;
    Context context;
    TextView productsAmount;
    TextView productsPrice;
    TextView productsTotalPrice;
    Button buttonBuy;
    View view;
    List<Product> productList = new ArrayList<>();
    Cart Cart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        // load data
//        List<Product> productList = new ArrayList<>();
//        productList.add(new Product(1, "Кухня", "Kuhnya", "Dess",
//                null, null, null, null, 1,2,
//                100,null));
//        productList.add(new Product(2, "Кухня", "Kuhnya", "Dess",
//                null, null, null, null, 1, 2,
//                100,null));
//        productList.add(new Product(3, "Кухня", "Kuhnya", "Dess",
//                null, null, null, null, 1, 2,
//                100,null));
//        productList.add(new Product(4, "Кухня", "Kuhnya", "Dess",
//                null, null, null, null, 1, 2,
//                100,null));
//        productList.add(new Product(5, "Кухня", "Kuhnya", "Dess",
//                null, null, null, null, 1, 2,
//                100,null));
//
//        com.diplom.smartstore.model.Cart cart = new com.diplom.smartstore.model.Cart(productList);

        getCartProducts();

        buttonBuy = view.findViewById(R.id.cartBuyButton);
        buttonBuy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // действие при нажатии кнопки "оформить заказ"
                Log.d(TAG, "оформить заказ: clicked " + v);
            }
        });

        return view;
    }

    @Override
    public void onProductClick(int position) {
        Log.d(TAG, "onProductClick: clicked " + position);
    }

    private void getCartProducts() {
        String url = getString(R.string.api_server) + getString(R.string.getCart);

        Thread request = new Thread() {
            @Override
            public void run() {
                if (isAdded()) {
                    Http http = new Http(getActivity(), url);//getActivity изза фрагмента вместо активити
                    http.setToken(true);
                    http.send();
                    if (isAdded()) {
                        requireActivity().runOnUiThread(new Runnable() {//getActivity изза фрагмента вместо активити
                            @Override
                            public void run() {
                                Integer code = http.getStatusCode();
                                if (code == 200) {
                                    try {
                                        // получаем JSON ответ
                                        JSONObject response = new JSONObject(http.getResponse());

                                        // выбираем из ответа JSON объект корзины
                                        JSONObject jsonobject = response.getJSONObject("cart");
                                        Integer totalPrice = jsonobject.getInt("total_price");

                                        // выбираем из ответа JSON массив продуктов
                                        JSONArray jsonarray = response.getJSONArray("cartProducts");

                                        // перебираем массив
                                        for (int i = 0; i < jsonarray.length(); i++) {
                                            JSONObject cartProduct = jsonarray.getJSONObject(i); // продукт корзины
                                            JSONObject product = cartProduct.getJSONObject("item_id"); // продукт

                                            JSONObject productBrand = product.getJSONObject("brand_id"); // бренд продукта (аттрибут объекта продукт)
                                            JSONObject productCategory = product.getJSONObject("category_id"); // категория продукта (аттрибут объекта продукт)
                                            JSONObject productSubcategory = product.getJSONObject("subcategory_id"); // подкатегория продукта (аттрибут объекта продукт)


                                            JSONArray productSubcategoryAttributes = productSubcategory.getJSONArray("attributes"); // список аттрибутов подкатегории
                                            JSONArray productAttributes = productSubcategory.getJSONArray("attributes"); // список аттрибутов подкатегории

                                            // перебираем список
                                            List<Attribute> attributesSubcategory = new ArrayList<>();
                                            List<Attribute> attributesProduct = new ArrayList<>();

                                            for (int j = 0; j < productSubcategoryAttributes.length(); j++) {
                                                // добавляем аттрибут в массив аттрибутов подкатегории
                                                Attribute productSubcategoryAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), null);
                                                attributesSubcategory.add(productSubcategoryAttribute);
                                                // добавляем аттрибут в массив аттрибутов товара
                                                Attribute productAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), productAttributes.get(j).toString());
                                                attributesProduct.add(productAttribute);
                                            }

                                            // добавляем товар в массив товаров корзины
                                            productList.add(new Product(product.getInt("id"),
                                                    product.getString("name"),
                                                    product.getString("slug"),
                                                    product.getString("image_url"),
                                                    product.getString("description"),
                                                    new Brand(productBrand.getInt("id"), productBrand.getString("name"),
                                                            productBrand.getString("slug"), productBrand.getString("description")),
                                                    new Category(productCategory.getInt("id"), productCategory.getString("name"),
                                                            productCategory.getString("slug"), productCategory.getString("description")),
                                                    new Subcategory(productSubcategory.getInt("id"), productSubcategory.getString("name"),
                                                            productSubcategory.getString("slug"), productSubcategory.getString("description"),
                                                            null, attributesSubcategory),
                                                    0,
                                                    product.getInt("amount_left"),
                                                    product.getInt("price"),
                                                    attributesProduct));
                                        }

                                        com.diplom.smartstore.model.Cart cart = new com.diplom.smartstore.model.Cart(productList);
                                        int productsAmountSum = 0;
                                        int productsPriceSum = 0;
                                        for (Product product : cart.getProducts()) {
                                            productsAmountSum += product.getAmountCart();
                                            productsPriceSum += product.getAmountCart() * product.getPrice();
                                        }
                                        Log.d("product", "===:> " + productsAmountSum + "===:> " + productsPriceSum);


                                        // Add the following lines to create RecyclerView
                                        cartRecycler = view.findViewById(R.id.cartRecyclerView);
                                        cartRecycler.setHasFixedSize(true);
                                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                                                RecyclerView.VERTICAL, false);
                                        cartRecycler.setLayoutManager(layoutManager);
                                        cartRecycler.setAdapter(new CartAdapter(context, cart, Cart));

                                        productsAmount = view.findViewById(R.id.cartProductAmountTextView);
                                        productsPrice = view.findViewById(R.id.cartProductPriceAmountTextView);
                                        productsTotalPrice = view.findViewById(R.id.cartProductTotalPriceAmountTextView);

                                        productsAmount.setText(productsAmountSum + " шт.");
                                        productsPrice.setText(productsPriceSum + " тенге");
                                        productsTotalPrice.setText(totalPrice + " тенге");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    alertFail("Ошибка " + code);
                                }
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void alertSuccess(String s) {
        new AlertDialog.Builder(getActivity())
                .setMessage(s)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
