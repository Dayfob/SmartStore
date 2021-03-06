package com.diplom.smartstore.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.diplom.smartstore.R;
import com.diplom.smartstore.model.Cart;
import com.diplom.smartstore.utils.Http;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Context context; // страница на которой все будет выведено
    Cart cart;
    OnProductListener onProductListener;
    FragmentActivity fragmentActivity;
    Button buttonBuy;
    OnDataChangeListener mOnDataChangeListener;


    public CartAdapter(Context context, Cart cart, OnProductListener onProductListener, FragmentActivity fragmentActivity, Button buttonBuy) {
        this.context = context;
        this.cart = cart;
        this.onProductListener = onProductListener;
        this.fragmentActivity = fragmentActivity;
        this.buttonBuy = buttonBuy;
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View productsItems = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.grid_item_product_flat_cart, parent, false); // указывается дизайн
        return new CartAdapter.CartViewHolder(productsItems, onProductListener); // указываются элементы для работы
    }

    @Override
    public int getItemCount() {
        return cart.getProducts().size();
    }

    public static final class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView productImage;
        ImageView buttonLike;
        ImageView buttonCartRemove;
        TextView productName;
        TextView productPrice;
        TextView productAmount;
        Button btnCartPlus, btnCartMinus;
        OnProductListener onProductListener;

        public CartViewHolder(@NonNull View itemView, OnProductListener onProductListener) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageFlat);
            buttonLike = itemView.findViewById(R.id.productHeartFlat);
            buttonCartRemove = itemView.findViewById(R.id.productCartRemove);
            productName = itemView.findViewById(R.id.productNameFlat);
            productPrice = itemView.findViewById(R.id.productPriceFlat);
            productAmount = itemView.findViewById(R.id.productAmountFlat);
            btnCartPlus = itemView.findViewById(R.id.buttonCartPlus);
            btnCartMinus = itemView.findViewById(R.id.buttonCartMinus);
            this.onProductListener = onProductListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onProductListener.onProductClick(getAdapterPosition());
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // нужно добавить асихронную загрузку фото:
//        new LoadImage(holder.productImage).execute(cart.getProducts().get(position).getImgUrl());
        ImageLoader.getInstance().displayImage(cart.getProducts().get(position).getImgUrl(), holder.productImage);
        holder.productName.setText(cart.getProducts().get(position).getName());
        holder.productPrice.setText(cart.getProducts().get(position).getPrice() + " KZT");
        holder.productAmount.setText(String.valueOf(cart.getProducts().get(position).getAmountCart()));

        if (cart.getProducts().get(position).getLiked()) {
            holder.buttonLike.setColorFilter(fragmentActivity.getResources().getColor(R.color.colorAccent));
        } else {
            holder.buttonLike.setColorFilter(fragmentActivity.getResources().getColor(R.color.colorSecondary));
        }

        holder.buttonLike.setOnClickListener(v -> {
            if (cart.getProducts().get(position).getLiked()) {
                holder.buttonLike.setColorFilter(fragmentActivity.getResources().getColor(R.color.colorSecondary));
                deleteFromFavourite(cart.getProducts().get(position).getId());
                cart.getProducts().get(position).setLiked(false);
            } else {
                holder.buttonLike.setColorFilter(fragmentActivity.getResources().getColor(R.color.colorAccent));
                addToFavourite(cart.getProducts().get(position).getId());
                cart.getProducts().get(position).setLiked(true);
            }
        });

        holder.buttonCartRemove.setOnClickListener(v -> {
            deleteFromCart(cart.getProducts().get(position).getId());
            removeAt(position);
            if (cart.getProducts().size() == 0) {
                buttonBuy.setClickable(false);
                buttonBuy.setBackgroundResource(R.drawable.bg_for_buy_btn_rounded_gray);
                buttonBuy.setTextColor(R.color.colorSecondary);
            }
            if (mOnDataChangeListener != null) {
                try {
                    mOnDataChangeListener.onDataChanged(cart.getProducts().size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        holder.btnCartPlus.setOnClickListener(v -> {
            cart.getProducts().get(position).setAmountCart(cart.getProducts().get(position).getAmountCart() + 1);
            holder.productAmount.setText(String.valueOf(cart.getProducts().get(position).getAmountCart()));
            updateInCart(cart.getProducts().get(position).getId(), cart.getProducts().get(position).getAmountCart());
            if (mOnDataChangeListener != null) {
                try {
                    mOnDataChangeListener.onDataChanged(cart.getProducts().size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.btnCartMinus.setOnClickListener(v -> {
            if (cart.getProducts().get(position).getAmountCart() > 1) {
                cart.getProducts().get(position).setAmountCart(cart.getProducts().get(position).getAmountCart() - 1);
                holder.productAmount.setText(String.valueOf(cart.getProducts().get(position).getAmountCart()));
                updateInCart(cart.getProducts().get(position).getId(), cart.getProducts().get(position).getAmountCart());
            } else {
                deleteFromCart(cart.getProducts().get(position).getId());
                removeAt(position);
                if (cart.getProducts().size() == 0) {
                    buttonBuy.setClickable(false);
                    buttonBuy.setBackgroundResource(R.drawable.bg_for_buy_btn_rounded_gray);
                    buttonBuy.setTextColor(R.color.colorSecondary);
                }
            }
            if (mOnDataChangeListener != null) {
                try {
                    mOnDataChangeListener.onDataChanged(cart.getProducts().size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // интерфейс для прослушивания нажатия на продукт
    public interface OnProductListener {
        void onProductClick(int position);
    }

    public void removeAt(int position) {
        cart.getProducts().remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cart.getProducts().size());
    }

    private void deleteFromFavourite(Integer id) {
        String url = fragmentActivity.getString(R.string.api_server) + fragmentActivity.getString(R.string.deleteFromWishlist);

        JSONObject params = new JSONObject();
        try {
            params.put("item_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();

        Thread request = new Thread() {
            @Override
            public void run() {
                Http http = new Http(fragmentActivity, url);
                http.setMethod("POST");
                http.setToken(true);
                http.setData(data);
                http.send();
                fragmentActivity.runOnUiThread(() -> {
                    Integer code = http.getStatusCode();
                    if (code == 201 || code == 200) {

                    } else if (code == 422) {
                        alertFail("Error 422");
                    } else {
                        alertFail("Error " + code);
                    }
                });
            }

        };
        request.start();
    }

    private void deleteFromCart(Integer id) {
        String url = fragmentActivity.getString(R.string.api_server) + fragmentActivity.getString(R.string.deleteFromCart);

        JSONObject params = new JSONObject();
        try {
            params.put("item_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();

        Thread request = new Thread() {
            @Override
            public void run() {
                Http http = new Http(fragmentActivity, url);
                http.setMethod("POST");
                http.setToken(true);
                http.setData(data);
                http.send();
                fragmentActivity.runOnUiThread(() -> {
                    Integer code = http.getStatusCode();
                    if (code == 201 || code == 200) {

                    } else if (code == 422) {
                        alertFail("Error 422");
                    } else {
                        alertFail("Error " + code);
                    }
                });
            }

        };
        request.start();
    }

    private void addToFavourite(Integer id) {
        String url = fragmentActivity.getString(R.string.api_server) + fragmentActivity.getString(R.string.addToWishlist);

        JSONObject params = new JSONObject();
        try {
            params.put("item_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();

        Thread request = new Thread() {
            @Override
            public void run() {
                Http http = new Http(fragmentActivity, url);
                http.setMethod("POST");
                http.setToken(true);
                http.setData(data);
                http.send();
                fragmentActivity.runOnUiThread(() -> {
                    Integer code = http.getStatusCode();
                    if (code == 201 || code == 200) {

                    } else if (code == 422) {
                        alertFail("Error 422");
                    } else {
                        alertFail("Error " + code);
                    }
                });
            }

        };
        request.start();
    }

    private void updateInCart(int id, int amountCart) {
        String url = fragmentActivity.getString(R.string.api_server) + fragmentActivity.getString(R.string.updateInCart);

        JSONObject params = new JSONObject();
        try {
            params.put("item_id", id);
            params.put("item_amount", amountCart);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = params.toString();

        Thread request = new Thread() {
            @Override
            public void run() {
                Http http = new Http(fragmentActivity, url);
                http.setMethod("POST");
                http.setToken(true);
                http.setData(data);
                http.send();
                fragmentActivity.runOnUiThread(() -> {
                    Integer code = http.getStatusCode();
                    if (code == 201 || code == 200) {

                    } else if (code == 422) {
                        alertFail("Error 422");
                    } else {
                        alertFail("Error " + code);
                    }
                });
            }

        };
        request.start();
    }

    private void alertFail(String s) {
        new AlertDialog.Builder(fragmentActivity)
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    private void alertSuccess(String s) {
        new AlertDialog.Builder(fragmentActivity)
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    public interface OnDataChangeListener {
        void onDataChanged(int size) throws InterruptedException;
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        mOnDataChangeListener = onDataChangeListener;
    }
}
