package com.arif.smartordermanagement.ui.operations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.model.SalesInvoice;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesInvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EMPLOYEE = 0;
    private static final int TYPE_INVOICE = 1;

    private final List<EmployeeGroup> groups = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private List<SalesInvoice> allInvoices = new ArrayList<>();
    private String statusFilter = "all";
    private RecyclerView attachedRecyclerView;

    public void setInvoices(List<SalesInvoice> invoices) {
        allInvoices = invoices == null ? new ArrayList<>() : new ArrayList<>(invoices);
        rebuildGroups();
    }

    public void setStatusFilter(String filter) {
        statusFilter = filter == null ? "all" : filter.toLowerCase(Locale.ROOT);
        rebuildGroups();
    }

    private void rebuildGroups() {
        groups.clear();
        Map<String, EmployeeGroup> groupsByEmployee = new LinkedHashMap<>();
        for (SalesInvoice invoice : allInvoices) {
            String status = invoice.getStatus().trim().toLowerCase(Locale.ROOT);
            boolean matches = "all".equals(statusFilter)
                    || ("accepted".equals(statusFilter) && "accepted".equals(status))
                    || ("pending".equals(statusFilter) && status.contains("pending"))
                    || ("return".equals(statusFilter) && status.contains("return"));
            if (!matches) continue;
            String key = invoice.getUsername() + "|" + invoice.getEmployeeName();
            EmployeeGroup group = groupsByEmployee.get(key);
            if (group == null) {
                group = new EmployeeGroup(invoice.getEmployeeName(), invoice.getUsername());
                groupsByEmployee.put(key, group);
            }
            group.invoices.add(invoice);
            group.total = group.total.add(amount(invoice.getPayableAmount()));
            if ("accepted".equals(status)) group.acceptedCount++;
            if (status.contains("pending")) group.pendingCount++;
            if (status.contains("return")) group.returnCount++;
        }
        groups.addAll(groupsByEmployee.values());
        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();
        for (EmployeeGroup group : groups) {
            rows.add(Row.employee(group));
            if (group.expanded) {
                for (SalesInvoice invoice : group.invoices) rows.add(Row.invoice(invoice));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        attachedRecyclerView = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).group != null ? TYPE_EMPLOYEE : TYPE_INVOICE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_EMPLOYEE) {
            return new EmployeeViewHolder(inflater.inflate(R.layout.item_sales_employee, parent, false));
        }
        return new InvoiceViewHolder(inflater.inflate(R.layout.item_sales_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof EmployeeViewHolder) {
            ((EmployeeViewHolder) holder).bind(row.group);
        } else {
            ((InvoiceViewHolder) holder).bind(row.invoice);
        }
    }

    @Override
    public int getItemCount() { return rows.size(); }

    private void toggle(EmployeeGroup selectedGroup, View clickedView) {
        boolean shouldExpand = !selectedGroup.expanded;
        if (shouldExpand) {
            for (EmployeeGroup group : groups) {
                if (group.expanded) collapse(group);
            }
        }

        int headerPosition = findHeaderPosition(selectedGroup);
        if (headerPosition == RecyclerView.NO_POSITION) return;

        if (!shouldExpand) {
            collapse(selectedGroup);
            clickedView.announceForAccessibility("Invoices collapsed");
            return;
        }

        selectedGroup.expanded = true;
        List<Row> invoiceRows = new ArrayList<>();
        for (SalesInvoice invoice : selectedGroup.invoices) invoiceRows.add(Row.invoice(invoice));
        rows.addAll(headerPosition + 1, invoiceRows);
        notifyItemChanged(headerPosition);
        notifyItemRangeInserted(headerPosition + 1, invoiceRows.size());
        clickedView.announceForAccessibility("Invoices expanded");
        centerEmployee(headerPosition);
    }

    private void collapse(EmployeeGroup group) {
        int headerPosition = findHeaderPosition(group);
        if (headerPosition == RecyclerView.NO_POSITION) return;
        int childCount = group.expanded ? group.invoices.size() : 0;
        group.expanded = false;
        if (childCount > 0) {
            rows.subList(headerPosition + 1, headerPosition + 1 + childCount).clear();
            notifyItemRangeRemoved(headerPosition + 1, childCount);
        }
        notifyItemChanged(headerPosition);
    }

    private int findHeaderPosition(EmployeeGroup target) {
        for (int position = 0; position < rows.size(); position++) {
            if (rows.get(position).group == target) return position;
        }
        return RecyclerView.NO_POSITION;
    }

    private void centerEmployee(int position) {
        RecyclerView recyclerView = attachedRecyclerView;
        if (recyclerView == null) return;
        recyclerView.post(() -> {
            if (recyclerView.getLayoutManager() == null) return;
            RecyclerView.SmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
                @Override
                protected int getVerticalSnapPreference() { return SNAP_TO_ANY; }

                @Override
                public int calculateDyToMakeVisible(@NonNull View view, int snapPreference) {
                    int viewCenter = view.getTop() + view.getHeight() / 2;
                    int viewportCenter = recyclerView.getPaddingTop()
                            + (recyclerView.getHeight() - recyclerView.getPaddingTop()
                            - recyclerView.getPaddingBottom()) / 2;
                    return viewCenter - viewportCenter;
                }
            };
            scroller.setTargetPosition(position);
            recyclerView.getLayoutManager().startSmoothScroll(scroller);
        });
    }

    private static BigDecimal amount(String value) {
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException | NullPointerException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static String money(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "৳" + format.format(value);
    }

    private final class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView avatar;
        private final TextView name;
        private final TextView summary;
        private final TextView totalLabel;
        private final TextView total;
        private final ImageView expandIcon;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.employeeAvatar);
            name = itemView.findViewById(R.id.employeeName);
            summary = itemView.findViewById(R.id.employeeSummary);
            totalLabel = itemView.findViewById(R.id.employeeTotalLabel);
            total = itemView.findViewById(R.id.employeeTotal);
            expandIcon = itemView.findViewById(R.id.employeeExpandIcon);
        }

        void bind(EmployeeGroup group) {
            String employeeName = group.employeeName.isEmpty() ? "Unknown employee" : group.employeeName;
            name.setText(group.username.isEmpty()
                    ? employeeName : employeeName + " (" + group.username + ")");
            avatar.setText(initials(employeeName));
            summary.setText(String.format(Locale.getDefault(),
                    "Inv %d  ·  Acc %d  ·  Pend %d  ·  Ret %d",
                    group.invoices.size(), group.acceptedCount, group.pendingCount, group.returnCount));
            totalLabel.setText("TOTAL SALES");
            total.setText(money(group.total));
            expandIcon.setImageResource(R.drawable.ic_spinner_chevron);
            expandIcon.animate().cancel();
            expandIcon.animate()
                    .rotation(group.expanded ? 180f : 0f)
                    .setDuration(200)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
            itemView.setSelected(group.expanded);
            itemView.setContentDescription(name.getText() + ", " + summary.getText()
                    + ", total payable " + total.getText()
                    + (group.expanded ? ", expanded. Activate to collapse." : ", collapsed. Activate to expand."));
            if (itemView instanceof com.google.android.material.card.MaterialCardView) {
                com.google.android.material.card.MaterialCardView card =
                        (com.google.android.material.card.MaterialCardView) itemView;
                card.setCardBackgroundColor(itemView.getContext().getColor(
                        group.expanded ? R.color.google_blue_container : R.color.surface_light));
                card.setStrokeColor(itemView.getContext().getColor(
                        group.expanded ? R.color.google_blue : R.color.outline_light));
            }
            itemView.setOnClickListener(view -> toggle(group, view));
        }

        private String initials(String name) {
            String[] parts = name.trim().split("\\\\s+");
            if (parts.length > 1) {
                return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                        .toUpperCase(Locale.ROOT);
            }
            return name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.ROOT);
        }
    }

    private static final class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView invoiceNumber, invoiceStatus, salePointName, invoiceMeta;
        private final TextView invoiceAddress;
        private final TextView invoiceValueAmount, invoiceDiscountAmount;
        private final TextView invoiceAdditionalDiscountAmount, invoiceReturnAmount;
        private final TextView invoicePayableAmount, brandTotals;
        private final View additionalDiscountRow;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            invoiceNumber = itemView.findViewById(R.id.invoiceNumber);
            invoiceStatus = itemView.findViewById(R.id.invoiceStatus);
            salePointName = itemView.findViewById(R.id.salePointName);
            invoiceMeta = itemView.findViewById(R.id.invoiceMeta);
            invoiceAddress = itemView.findViewById(R.id.invoiceAddress);
            invoiceValueAmount = itemView.findViewById(R.id.invoiceValueAmount);
            invoiceDiscountAmount = itemView.findViewById(R.id.invoiceDiscountAmount);
            invoiceAdditionalDiscountAmount = itemView.findViewById(R.id.invoiceAdditionalDiscountAmount);
            additionalDiscountRow = itemView.findViewById(R.id.additionalDiscountRow);
            invoiceReturnAmount = itemView.findViewById(R.id.invoiceReturnAmount);
            invoicePayableAmount = itemView.findViewById(R.id.invoicePayableAmount);
            brandTotals = itemView.findViewById(R.id.brandTotals);
        }

        void bind(SalesInvoice invoice) {
            invoiceNumber.setText(invoice.getOrderDate());
            bindStatus(invoice.getStatus());

            salePointName.setText(invoice.getSalePointName());
            String invoiceId = invoice.getInvoiceNumber().isEmpty()
                    ? invoice.getInvoiceId() : invoice.getInvoiceNumber();
            String invoiceDate = invoice.getInvoiceDate();
            String meta = invoiceId.isEmpty() ? "" : invoiceId;
            if (!invoiceDate.isEmpty()) {
                meta = meta.isEmpty() ? "Invoiced " + invoiceDate : meta + "  •  Invoiced " + invoiceDate;
            }
            invoiceMeta.setText(meta);
            invoiceMeta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
            invoiceAddress.setText(invoice.getAddress());
            invoiceAddress.setVisibility(invoice.getAddress().isEmpty() ? View.GONE : View.VISIBLE);
            invoiceValueAmount.setText(currency(invoice.getInvoiceValue()));
            invoiceDiscountAmount.setText(currency(invoice.getDiscountValue()));
            invoiceAdditionalDiscountAmount.setText(currency(invoice.getAdditionalDiscount()));
            additionalDiscountRow.setVisibility(amount(invoice.getAdditionalDiscount()).signum() == 0
                    ? View.GONE : View.VISIBLE);
            invoiceReturnAmount.setText(currency(invoice.getReturnAmount()));
            invoicePayableAmount.setText(currency(invoice.getPayableAmount()));
            String brands = invoice.getBrandTotals();
            brandTotals.setText(brands);
            brandTotals.setVisibility(brands.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void bindStatus(String status) {
            invoiceStatus.setText(status.isEmpty() ? "Unknown" : status);
            invoiceStatus.setContentDescription("Invoice status: " + (status.isEmpty() ? "unknown" : status));
            String normalized = status.toLowerCase(Locale.ROOT);
            int foreground;
            int background;
            if (normalized.equals("accepted")) {
                foreground = android.graphics.Color.rgb(19, 115, 51);
                background = android.graphics.Color.rgb(230, 244, 234);
            } else if (normalized.contains("pending")) {
                foreground = android.graphics.Color.rgb(176, 96, 0);
                background = android.graphics.Color.rgb(254, 247, 224);
            } else if (normalized.contains("return")) {
                foreground = android.graphics.Color.rgb(180, 83, 9);
                background = android.graphics.Color.rgb(255, 237, 213);
            } else {
                foreground = android.graphics.Color.rgb(86, 96, 110);
                background = android.graphics.Color.rgb(237, 240, 244);
            }
            invoiceStatus.setTextColor(foreground);
            invoiceStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(background));
        }

        private String currency(String value) {
            try {
                NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
                format.setMinimumFractionDigits(0);
                format.setMaximumFractionDigits(2);
                return "৳" + format.format(new BigDecimal(value.replace("BDT", "").replace("৳", "").replace(",", "").trim()));
            } catch (NumberFormatException | NullPointerException ignored) {
                return value.startsWith("৳") ? value : "৳" + value;
            }
        }
    }

    private static final class EmployeeGroup {
        final String employeeName;
        final String username;
        final List<SalesInvoice> invoices = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int acceptedCount;
        int pendingCount;
        int returnCount;
        boolean expanded;

        EmployeeGroup(String employeeName, String username) {
            this.employeeName = employeeName == null ? "" : employeeName;
            this.username = username == null ? "" : username;
        }
    }

    private static final class Row {
        final EmployeeGroup group;
        final SalesInvoice invoice;

        private Row(EmployeeGroup group, SalesInvoice invoice) {
            this.group = group;
            this.invoice = invoice;
        }

        static Row employee(EmployeeGroup group) { return new Row(group, null); }
        static Row invoice(SalesInvoice invoice) { return new Row(null, invoice); }
    }
}
