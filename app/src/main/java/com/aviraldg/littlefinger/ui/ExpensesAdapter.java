package com.aviraldg.littlefinger.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.aviraldg.littlefinger.LittlefingerApplication;
import com.aviraldg.littlefinger.api.LittlefingerApi;
import com.aviraldg.littlefinger.api.models.ApiResponse;
import com.aviraldg.littlefinger.api.models.Expense;
import com.aviraldg.littlefinger.databinding.ExpenseItemBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> {
    ArrayList<Expense> expenses = new ArrayList<>();

    class ViewHolder extends RecyclerView.ViewHolder {
        ExpenseItemBinding binding;

        public ViewHolder(ExpenseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setExpense(Expense expense) {
            binding.setExpense(expense);
            binding.expenseIcon.getHierarchy().setPlaceholderImage(expense.getIcon());
        }
    }

    LittlefingerApi api = LittlefingerApplication.getApi();

    public void refresh() {
        api.queryExpenses().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.isSuccess()) {
                    int start = expenses.size();
                    expenses.addAll(response.body().getExpenses());
                    int end = expenses.size() - 1;

                    notifyItemRangeInserted(start, end);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ExpenseItemBinding binding = ExpenseItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setExpense(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }
}
