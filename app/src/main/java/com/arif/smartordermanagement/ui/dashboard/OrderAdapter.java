package com.arif.smartordermanagement.ui.dashboard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arif.smartordermanagement.databinding.ItemOrderBinding;
import com.arif.smartordermanagement.model.Order;
import com.arif.smartordermanagement.model.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        this.orders.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order, OnOrderClickListener listener) {
            binding.tvCustomerName.setText(order.getCustomerName());
            binding.tvOrderTotalAmount.setText(order.getTotalAmountFormatted());

            String subdetails = order.getId() + " • " + order.getCategory() + " (" + order.getItemCount() + " items) • " + order.getTimeAgo();
            binding.tvOrderSubdetails.setText(subdetails);

            OrderStatus status = order.getStatus();
            binding.tvOrderStatusBadge.setText(status.getDisplayName());

            int statusTextColor;
            int statusBgColor;

            if (status == OrderStatus.COMPLETED) {
                statusTextColor = Color.parseColor("#137333");
                statusBgColor = Color.parseColor("#E6F4EA");
            } else if (status == OrderStatus.PROCESSING) {
                statusTextColor = Color.parseColor("#185ABC");
                statusBgColor = Color.parseColor("#E8F0FE");
            } else if (status == OrderStatus.PENDING) {
                statusTextColor = Color.parseColor("#B06000");
                statusBgColor = Color.parseColor("#FEF7E0");
            } else { // CANCELLED
                statusTextColor = Color.parseColor("#C5221F");
                statusBgColor = Color.parseColor("#FCE8E6");
            }

            binding.tvOrderStatusBadge.setTextColor(statusTextColor);
            binding.tvOrderStatusBadge.getBackground().setTint(statusBgColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
    }
}
