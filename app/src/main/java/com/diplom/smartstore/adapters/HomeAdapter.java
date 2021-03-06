package com.diplom.smartstore.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diplom.smartstore.R;
import com.diplom.smartstore.fragments.SubcategoryProductList;
import com.diplom.smartstore.model.App;
import com.diplom.smartstore.model.News;
import com.diplom.smartstore.model.Product;
import com.diplom.smartstore.model.Subcategory;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements NewsAdapter.OnNewsListener, SubcategoryAdapter.OnSubcategoryListener, ProductAdapter.OnProductListener {

    private static final int TYPE_NEWS = 1;
    private static final int TYPE_CATEGORIES = 2;
    private static final int TYPE_PRODUCTS = 3;

    Context context; // страница на которой все будет выведено
    App app;
    List<News> newsList;
    List<Subcategory> subcategoryList;
    List<Product> productList;
    FragmentActivity fragmentActivity;

    // конструктор
    public HomeAdapter(Context context, App app, List<News> newsList, List<Subcategory> subcategoryList, List<Product> productList, FragmentActivity fragmentActivity) {
        this.context = context;
        this.app = app;
        this.newsList = newsList;
        this.subcategoryList = subcategoryList;
        this.productList = productList;
        this.fragmentActivity = fragmentActivity;
    }

    // какие данные будут вставлены, установка данных
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_NEWS) {
            NewsHomeViewHolder newsViewHolder = (NewsHomeViewHolder) holder;
            newsViewHolder.moduleTitle.setText("News");
            setNewsRecycler(newsViewHolder.newsRecycler, app.getNews());
        } else if (getItemViewType(position) == TYPE_CATEGORIES) {
            SubcategoriesHomeViewHolder subcategoriesViewHolder = (SubcategoriesHomeViewHolder) holder;
            subcategoriesViewHolder.moduleTitle.setText("Subcategories");
            setSubcategoriesRecycler(subcategoriesViewHolder.subcategoriesRecycler, app.getSubcategories());
        } else if (getItemViewType(position) == TYPE_PRODUCTS) {
            ProductsHomeViewHolder productsViewHolder = (ProductsHomeViewHolder) holder;
            productsViewHolder.moduleTitle.setText("Products");
            setProductsRecycler(productsViewHolder.productRecycler, app.getProducts());
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    // держатель вызывающий раздуватель
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_NEWS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_news_list, parent, false);
            return new NewsHomeViewHolder(view);
        } else if (viewType == TYPE_CATEGORIES) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_catalog, parent, false);
            return new SubcategoriesHomeViewHolder(view);
        } else { //(viewType == TYPE_PRODUCTS)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_product_list, parent, false);
            return new ProductsHomeViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_NEWS;
        } else if (position == 1) {
            return TYPE_CATEGORIES;
        } else {
            return TYPE_PRODUCTS;
        }
    }

    @Override
    public void onNewsClick(int position) {
//        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        SubcategoryProductList subcategoryList = new SubcategoryProductList();
//        Bundle bundle = new Bundle();
//        bundle.putInt("id", newsList.get(position).getId());
//        subcategoryList.setArguments(bundle);
//        ft.replace(R.id.content, subcategoryList);
//        ft.addToBackStack("product");
//        ft.commit();
    }

    @Override
    public void onSubcategoryClick(int position) {
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        SubcategoryProductList subcategoryFragment = new SubcategoryProductList();
        Bundle bundle = new Bundle();
        bundle.putInt("id", subcategoryList.get(position).getId());
        subcategoryFragment.setArguments(bundle);
        ft.replace(R.id.content, subcategoryFragment);
        ft.addToBackStack("subcategoryProducts");
        ft.commit();
    }

    @Override
    public void onProductClick(int position) {
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        com.diplom.smartstore.fragments.Product productFragment = new com.diplom.smartstore.fragments.Product();
        Bundle bundle = new Bundle();
        bundle.putInt("id", productList.get(position).getId());
        productFragment.setArguments(bundle);
        ft.replace(R.id.content, productFragment);
        ft.addToBackStack("product");
        ft.commit();
    }

    // описываются элементы для работы
    public static final class NewsHomeViewHolder extends RecyclerView.ViewHolder {
        TextView moduleTitle;
        RecyclerView newsRecycler;

        public NewsHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleTitle = itemView.findViewById(R.id.newsModuleTitle);
            newsRecycler = itemView.findViewById(R.id.newsRecyclerView);
        }
    }

    // описываются элементы для работы
    public static final class SubcategoriesHomeViewHolder extends RecyclerView.ViewHolder {
        TextView moduleTitle;
        RecyclerView subcategoriesRecycler;

        public SubcategoriesHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleTitle = itemView.findViewById(R.id.parentCategory);
            subcategoriesRecycler = itemView.findViewById(R.id.subcategoryRecyclerView);
        }
    }

    // описываются элементы для работы
    public static final class ProductsHomeViewHolder extends RecyclerView.ViewHolder {
        TextView moduleTitle;
        RecyclerView productRecycler;

        public ProductsHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleTitle = itemView.findViewById(R.id.productModuleTitle);
            productRecycler = itemView.findViewById(R.id.productsRecyclerView);
        }
    }

    // лист новостей
    private void setNewsRecycler(RecyclerView recyclerView, List<News> newsList) {
        NewsAdapter newsAdapter = new NewsAdapter(context, newsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(newsAdapter);
    }

    // лист категорий
    private void setSubcategoriesRecycler(RecyclerView recyclerView, List<Subcategory> subcategoryList) {
        SubcategoryAdapter subcategoryAdapter = new SubcategoryAdapter(context, subcategoryList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(subcategoryAdapter);
    }

    // лист продуктов
    private void setProductsRecycler(RecyclerView recyclerView, List<Product> productsList) {
        ProductAdapter productAdapter = new ProductAdapter(context, productsList, this, fragmentActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(productAdapter);
    }
}
