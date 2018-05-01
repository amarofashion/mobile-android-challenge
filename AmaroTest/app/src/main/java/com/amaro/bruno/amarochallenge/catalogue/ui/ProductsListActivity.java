package com.amaro.bruno.amarochallenge.catalogue.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amaro.bruno.amarochallenge.BaseActivity;
import com.amaro.bruno.amarochallenge.R;
import com.amaro.bruno.amarochallenge.catalogue.adapter.ProductItemClickListener;
import com.amaro.bruno.amarochallenge.catalogue.adapter.ProductsListAdapter;
import com.amaro.bruno.amarochallenge.catalogue.di.InjectionProductsListAdapter;
import com.amaro.bruno.amarochallenge.catalogue.extensions.ViewUtils;
import com.amaro.bruno.amarochallenge.catalogue.listener.IOptionPriceSelectListener;
import com.amaro.bruno.amarochallenge.catalogue.mock.ProductsMock;
import com.amaro.bruno.amarochallenge.catalogue.model.Product;
import com.amaro.bruno.amarochallenge.catalogue.presentation.ProductListContract;
import com.amaro.bruno.amarochallenge.catalogue.presentation.ProductListPresenter;
import com.amaro.bruno.amarochallenge.catalogue.use_case.ProductsListFilter;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class ProductsListActivity extends BaseActivity implements ProductListContract.View, ProductItemClickListener, IOptionPriceSelectListener{

    @BindView(R.id.coordinator_products)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    @BindView(R.id.tv_items)
    TextView tvItems;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.linear_filter)
    LinearLayout linearFilter;

    @BindView(R.id.tv_search_price)
    TextView tvSearchPrice;

    @BindView(R.id.grid_products)
    GridView gridProducts;

    @Inject
    ProductListPresenter productListPresenter;

    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private ProductsListFilter productsFilter;
    private ProductsListAdapter productsAdapter;
    private int myLastVisiblePos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);

        ButterKnife.bind(this);
        AndroidInjection.inject(this);

        setup();

        productListPresenter.setContext(this);
        productListPresenter.getProductsList();

        gridProducts.setAdapter(productsAdapter);
        gridProducts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) { }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int currentFirstVisPos = absListView.getFirstVisiblePosition();
                if(currentFirstVisPos > myLastVisiblePos) {
                    ViewUtils.hideViewOnScrollDownWithAnimation(linearFilter);
                }
                if(currentFirstVisPos < myLastVisiblePos) {
                    ViewUtils.showViewOnScrollUpWithAnimation(linearFilter);
                }

                myLastVisiblePos = currentFirstVisPos;
            }
        });

        swipeRefresh.setOnRefreshListener(() -> {
            productListPresenter.getProductsList();

            hideProgress();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        productListPresenter.stop();
    }

    @Override
    protected void setup() {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.app_name);
        }

        tvSearchPrice.setPaintFlags(tvSearchPrice.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        searchView.setVoiceSearch(true);
        searchView.setHint(getString(R.string.search_product_name));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productsAdapter.getFilter().filter(newText);
                return false;
            }
        });

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        productsFilter = new ProductsListFilter();

        allProducts.addAll(ProductsMock.productList());
        filteredProducts.addAll(allProducts);

        tvItems.setText(getString(R.string.qty_items, allProducts.size()));

        productsAdapter = InjectionProductsListAdapter.getProductsListAdapter(this, filteredProducts,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products_list_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    private void openSearchDialog(Bundle bundle){
        SearchDialogFragment searchDialogFragment = SearchDialogFragment.newInstance();
        searchDialogFragment.setArguments(bundle);
        searchDialogFragment.setOptionPriceSelectListener(this);
        searchDialogFragment.show(getFragmentManager(), SearchDialogFragment.TAG);
    }

    @OnClick(R.id.linear_wrapper_price)
    public void searchPriceClick(){
        Bundle bundle = new Bundle();
        bundle.putString(SearchDialogFragment.BUNDLE_SEARCH_OPTION, SearchDialogFragment.SEARCH_PRICE);

        bundle.putStringArrayList(SearchDialogFragment.BUNDLE_PRICES, productListPresenter.getPrices());
        openSearchDialog(bundle);
    }

    @Override
    public void onPriceSelected(String price) {
        tvSearchPrice.setText(price);
        productsFilter.setPrice(price);

        productListPresenter.filterProductsAdapter(productsAdapter, allProducts, productsFilter);
    }

    @Override
    public void onProductItemClicked(Product product) {
//        TODO Open DialogFragment of Product details
        Log.d("Click", product.getName());
    }

    @Override
    public void showProgress() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onSuccessListProducts(ArrayList<Product> products) {
        allProducts = products;
        productsAdapter = InjectionProductsListAdapter.getProductsListAdapter(this, allProducts,this);
        gridProducts.setAdapter(productsAdapter);
    }

    @Override
    public void onError(String msg) {
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
