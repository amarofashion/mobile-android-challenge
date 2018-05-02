package com.amaro.bruno.amarochallenge.catalogue.products.presentation;

import com.amaro.bruno.amarochallenge.BaseView;
import com.amaro.bruno.amarochallenge.catalogue.products.ui.adapter.ProductsListAdapter;
import com.amaro.bruno.amarochallenge.catalogue.products.model.Product;
import com.amaro.bruno.amarochallenge.catalogue.products.use_case.ProductsListFilter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;

public interface ProductListContract {

    interface View extends BaseView {
        void onSuccessListProducts(ArrayList<Product> products);
    }

    interface Presenter{
        void getProductsList(Scheduler scheduler, Scheduler observer);
        List<String> getSizes(Product product);
        ArrayList<String> getPrices();
        List<Product> getProductsBySize(List<Product> products, String size);
        List<Product> getProductsLessThan(List<Product> products, double val);
        List<Product> getProductsByPriceRange(List<Product> products, double initialPrice, double finalPrice);
        List<Product> getProductsHigherThan(List<Product> products, double val);
        List<Product> filterProductsAdapter(ProductsListAdapter productsAdapter, List<Product> allProducts, ProductsListFilter filter);
    }

}
