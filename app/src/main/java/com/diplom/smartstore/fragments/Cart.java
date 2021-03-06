package com.diplom.smartstore.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diplom.smartstore.R;
import com.diplom.smartstore.activities.CreateOrderActivity;
import com.diplom.smartstore.adapters.CartAdapter;
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

    RecyclerView cartRecycler;
    Context context;
    TextView productsAmount;
    TextView productsPrice;
    TextView productsTotalPrice;
    Button buttonBuy;
    View view;
    List<Product> productList = new ArrayList<>();
    private final List<Product> wishlistProductList = new ArrayList<>();
    Cart Cart = this;
    com.diplom.smartstore.model.Cart cart;

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        buttonBuy = view.findViewById(R.id.cartBuyButton);

        buttonBuy.setClickable(false);
        buttonBuy.setBackgroundResource(R.drawable.bg_for_buy_btn_rounded_gray);
        buttonBuy.setTextColor(R.color.colorSecondary);
        buttonBuy.setOnClickListener(v -> {
            // ???????????????? ?????? ?????????????? ???????????? "???????????????? ??????????"
            Intent intent = new Intent(getActivity(), CreateOrderActivity.class);
            startActivity(intent);
        });

        getCartProducts();

        return view;
    }

    @Override
    public void onProductClick(int position) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        com.diplom.smartstore.fragments.Product productFragment = new com.diplom.smartstore.fragments.Product();
        Bundle bundle = new Bundle();
        bundle.putInt("id", productList.get(position).getId());
        productFragment.setArguments(bundle);
        ft.replace(R.id.content, productFragment);
        ft.addToBackStack("product");
        ft.commit();
    }

    private void getCartProducts() {
        String url = getString(R.string.api_server) + getString(R.string.getCart);
        String urlWishlist = getString(R.string.api_server) + getString(R.string.getWishlist);

        Thread request = new Thread() {
            @SuppressLint({"ResourceAsColor", "SetTextI18n"})
            @Override
            public void run() {
                if (isAdded()) {
                    Http http = new Http(getActivity(), url);//getActivity ???????? ?????????????????? ???????????? ????????????????
                    http.setToken(true);
                    http.send();
                    Http httpWishlist = new Http(getActivity(), urlWishlist);//getActivity ???????? ?????????????????? ???????????? ????????????????
                    httpWishlist.setToken(true);
                    httpWishlist.send();
                    if (isAdded()) {
                        //getActivity ???????? ?????????????????? ???????????? ????????????????
                        requireActivity().runOnUiThread(() -> {
                            Integer codeHttpWishlist = httpWishlist.getStatusCode();
                            if (codeHttpWishlist == 200) {
                                try {
                                    // ???????????????? JSON ??????????
                                    JSONObject response = new JSONObject(httpWishlist.getResponse());

                                    // ???????????????? ???? ???????????? JSON ???????????? ??????????????????
                                    JSONArray jsonarray = response.getJSONArray("wishlistProducts");

                                    // ???????????????????? ????????????
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject wishlistProduct = jsonarray.getJSONObject(i); // ?????????????? ?????????? ??????????????
                                        JSONObject product = wishlistProduct.getJSONObject("item_id"); // ??????????????

                                        JSONObject productBrand = product.getJSONObject("brand_id"); // ?????????? ???????????????? (???????????????? ?????????????? ??????????????)
                                        JSONObject productCategory = product.getJSONObject("category_id"); // ?????????????????? ???????????????? (???????????????? ?????????????? ??????????????)
                                        JSONObject productSubcategory = product.getJSONObject("subcategory_id"); // ???????????????????????? ???????????????? (???????????????? ?????????????? ??????????????)


                                        JSONArray productSubcategoryAttributes = productSubcategory.getJSONArray("attributes"); // ???????????? ???????????????????? ????????????????????????
                                        JSONArray productAttributes = productSubcategory.getJSONArray("attributes"); // ???????????? ???????????????????? ????????????????????????

                                        // ???????????????????? ????????????
                                        List<Attribute> attributesSubcategory = new ArrayList<>();
                                        List<Attribute> attributesProduct = new ArrayList<>();

                                        for (int j = 0; j < productSubcategoryAttributes.length(); j++) {
                                            // ?????????????????? ???????????????? ?? ???????????? ???????????????????? ????????????????????????
                                            Attribute productSubcategoryAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), null);
                                            attributesSubcategory.add(productSubcategoryAttribute);
                                            // ?????????????????? ???????????????? ?? ???????????? ???????????????????? ????????????
                                            Attribute productAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), productAttributes.get(j).toString());
                                            attributesProduct.add(productAttribute);
                                        }

                                        // ?????????????????? ?????????? ?? ???????????? ?????????????? ???????????? ??????????????
                                        wishlistProductList.add(new Product(product.getInt("id"),
                                                product.getString("name"),
                                                product.getString("slug"),
                                                product.getString("image_url"),
                                                product.getString("description"),
                                                new Brand(productBrand.getInt("id"), productBrand.getString("name"),
                                                        productBrand.getString("slug"), productBrand.getString("description")),
                                                new Category(productCategory.getInt("id"), productCategory.getString("name"),
                                                        productCategory.getString("slug"), productCategory.getString("description"), null),
                                                new Subcategory(productSubcategory.getInt("id"), productSubcategory.getString("name"),
                                                        productSubcategory.getString("slug"), productSubcategory.getString("description"),
                                                        null, attributesSubcategory), // image
                                                0,
                                                product.getInt("amount_left"),
                                                product.getInt("price"),
                                                attributesProduct,
                                                product.getBoolean("liked")));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (codeHttpWishlist == 401) {
                                alertFail("Please login");
                            } else {
                                alertFail("Error " + codeHttpWishlist);
                            }


                            Integer code = http.getStatusCode();
                            if (code == 200) {
                                try {
                                    // ???????????????? JSON ??????????
                                    JSONObject response = new JSONObject(http.getResponse());

                                    // ???????????????? ???? ???????????? JSON ???????????? ??????????????
                                    JSONObject jsonobject = response.getJSONObject("cart");
                                    Integer totalPrice = jsonobject.getInt("total_price");

                                    // ???????????????? ???? ???????????? JSON ???????????? ??????????????????
                                    JSONArray jsonarray = response.getJSONArray("cartProducts");

                                    productList.clear();
                                    // ???????????????????? ????????????
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject cartProduct = jsonarray.getJSONObject(i); // ?????????????? ??????????????
                                        JSONObject product = cartProduct.getJSONObject("item_id"); // ??????????????
                                        int productAmount = cartProduct.getInt("item_amount"); // ??????????????


                                        JSONObject productBrand = product.getJSONObject("brand_id"); // ?????????? ???????????????? (???????????????? ?????????????? ??????????????)
                                        JSONObject productCategory = product.getJSONObject("category_id"); // ?????????????????? ???????????????? (???????????????? ?????????????? ??????????????)
                                        JSONObject productSubcategory = product.getJSONObject("subcategory_id"); // ???????????????????????? ???????????????? (???????????????? ?????????????? ??????????????)


                                        JSONArray productSubcategoryAttributes = productSubcategory.getJSONArray("attributes"); // ???????????? ???????????????????? ????????????????????????
                                        JSONArray productAttributes = productSubcategory.getJSONArray("attributes"); // ???????????? ???????????????????? ????????????????????????

                                        // ???????????????????? ????????????
                                        List<Attribute> attributesSubcategory = new ArrayList<>();
                                        List<Attribute> attributesProduct = new ArrayList<>();

                                        for (int j = 0; j < productSubcategoryAttributes.length(); j++) {
                                            // ?????????????????? ???????????????? ?? ???????????? ???????????????????? ????????????????????????
                                            Attribute productSubcategoryAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), null);
                                            attributesSubcategory.add(productSubcategoryAttribute);
                                            // ?????????????????? ???????????????? ?? ???????????? ???????????????????? ????????????
                                            Attribute productAttribute = new Attribute(j, productSubcategoryAttributes.get(j).toString(), productAttributes.get(j).toString());
                                            attributesProduct.add(productAttribute);
                                        }

                                        boolean inCart = false;

                                        for (Product wishlistProduct : wishlistProductList) {
                                            if (product.getInt("id") == wishlistProduct.getId()) {
                                                inCart = true;
                                            }
                                        }

                                        // ?????????????????? ?????????? ?? ???????????? ?????????????? ??????????????
                                        productList.add(new Product(product.getInt("id"),
                                                product.getString("name"),
                                                product.getString("slug"),
                                                product.getString("image_url"),
                                                product.getString("description"),
                                                new Brand(productBrand.getInt("id"), productBrand.getString("name"),
                                                        productBrand.getString("slug"), productBrand.getString("description")),
                                                new Category(productCategory.getInt("id"), productCategory.getString("name"),
                                                        productCategory.getString("slug"), productCategory.getString("description"), null),
                                                new Subcategory(productSubcategory.getInt("id"), productSubcategory.getString("name"),
                                                        productSubcategory.getString("slug"), productSubcategory.getString("description"),
                                                        null, attributesSubcategory),
                                                productAmount,
                                                product.getInt("amount_left"),
                                                product.getInt("price"),
                                                attributesProduct,
                                                inCart));
                                    }

                                    if (productList.size() > 0) {
                                        buttonBuy.setClickable(true);
                                        buttonBuy.setBackgroundResource(R.drawable.bg_for_buy_btn_rounded);
                                        buttonBuy.setTextColor(R.color.White);
                                    }

                                    cart = new com.diplom.smartstore.model.Cart(productList);
                                    int productsAmountSum = 0;
                                    int productsPriceSum = 0;
                                    for (Product product : cart.getProducts()) {
                                        productsAmountSum += product.getAmountCart();
                                        productsPriceSum += product.getAmountCart() * product.getPrice();
                                    }


                                    // Add the following lines to create RecyclerView
                                    cartRecycler = view.findViewById(R.id.cartRecyclerView);
                                    cartRecycler.setHasFixedSize(true);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                                            RecyclerView.VERTICAL, false);
                                    cartRecycler.setLayoutManager(layoutManager);
                                    CartAdapter cartAdapter = new CartAdapter(context, cart, Cart, getActivity(), buttonBuy);
                                    cartAdapter.setOnDataChangeListener(size -> {
                                        //do whatever here
                                        getUpdatedCart();
                                    });

                                    cartRecycler.setAdapter(cartAdapter);

                                    productsAmount = view.findViewById(R.id.cartProductAmountTextView);
                                    productsPrice = view.findViewById(R.id.cartProductPriceAmountTextView);
                                    productsTotalPrice = view.findViewById(R.id.cartProductTotalPriceAmountTextView);

                                    productsAmount.setText(productsAmountSum + " PCS.");
                                    productsPrice.setText(productsPriceSum + " KZT");
                                    productsTotalPrice.setText(totalPrice + " KZT");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (code == 401) {
                                alertFail("Please login");
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

    @SuppressLint("SetTextI18n")
    private void getUpdatedCart() {
        int productsAmountSum = 0;
        int productsPriceSum = 0;
        for (Product product : cart.getProducts()) {
            productsAmountSum += product.getAmountCart();
            productsPriceSum += product.getAmountCart() * product.getPrice();
        }
        productsAmount.setText(productsAmountSum + " PCS.");
        productsPrice.setText(productsPriceSum + " KZT");
        productsTotalPrice.setText(productsPriceSum + " KZT");
    }
}
