package com.aviraldg.littlefinger.ui;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviraldg.littlefinger.LittlefingerApplication;
import com.aviraldg.littlefinger.R;
import com.aviraldg.littlefinger.api.LittlefingerApi;
import com.aviraldg.littlefinger.api.models.ApiData;
import com.aviraldg.littlefinger.api.models.Expense;
import com.aviraldg.littlefinger.api.models.State;
import com.aviraldg.littlefinger.databinding.ExpenseItemBinding;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> implements
        SwipeRefreshLayout.OnRefreshListener,
        SwipeableItemAdapter<ExpensesAdapter.ViewHolder> {

    ArrayList<Expense> expenses = new ArrayList<>();
    LittlefingerApi api = LittlefingerApplication.getApi();
    ExpenseListEventListener listener;
    SwipeRefreshLayout swipeRefreshLayout;

    public ExpensesAdapter(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(this);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return expenses.get(position).getId().hashCode();
    }

    public void setExpenseClickListener(ExpenseListEventListener listener) {
        this.listener = listener;
    }

    public void refresh() {
        swipeRefreshLayout.setRefreshing(true);

        api.queryExpenses().enqueue(new Callback<ApiData>() {
            @Override
            public void onResponse(Call<ApiData> call, Response<ApiData> response) {
                if(response.isSuccess()) {
                    expenses.clear();
                    int start = expenses.size();
                    expenses.addAll(response.body().getExpenses());
                    int end = expenses.size() - 1;

                    notifyItemRangeChanged(start, end);

                    if(listener != null) {
                        listener.onExpenseListRefreshed(response.body().getExpenses());
                    }
                } else {
                    if(listener != null)
                        listener.onExpenseListRefreshFailed();
                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ApiData> call, Throwable t) {
                t.printStackTrace();
                if(listener != null)
                    listener.onExpenseListRefreshFailed();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
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

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
        SwipeResultAction action;

        State state = State.forName(holder.expense.getState());
        int count = State.getStateCount();

        switch (result) {
            case SwipeableItemConstants.RESULT_SWIPED_LEFT:
                action = new SwipeMarkState(position, State.forId((state.getId() + count - 1) % count));
                break;

            case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                action = new SwipeMarkState(position, State.forId((state.getId() + count+ 1) % count));
                break;

            case SwipeableItemConstants.RESULT_CANCELED:
            default:
                action = null;
                break;
        }

        return action;
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        if(!expenses.get(position).isExpanded())
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        else
            return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {

        State state = State.forName(holder.expense.getState()), newState;

        int count = State.getStateCount();

        switch(type) {
            case SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND: {
                int id = (state.getId() + count - 1) % count;
                newState = State.forId(id);
                holder.rightIcon.getHierarchy().setPlaceholderImage(newState.getDrawableResource());
                break;
            }

            case SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND: {
                int id = (state.getId() + count + 1) % count;
                newState = State.forId(id);
                holder.leftIcon.getHierarchy().setPlaceholderImage(newState.getDrawableResource());
                break;
            }

            case SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
            default:
                newState = State.forName(State.UNVERIFIED);
                break;
        }

        holder.itemView.setBackgroundResource(newState.getColorResource());
    }

    public interface ExpenseListEventListener {
        void onExpenseChanged(Expense expense, ApiData data);
        void onExpenseListRefreshed(List<Expense> expenses);
        void onExpenseListRefreshFailed();
    }

    class ViewHolder extends AbstractSwipeableItemViewHolder implements View.OnClickListener {
        ExpenseItemBinding binding;
        Expense expense;
        View statusSwitcher;
        SimpleDraweeView leftIcon;
        SimpleDraweeView rightIcon;

        public ViewHolder(ExpenseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.findViewById(R.id.container).setOnClickListener(this);
            bindViews(itemView);

        }

        private void bindViews(View v) {
            statusSwitcher = v.findViewById(R.id.status_switcher);

            View.OnClickListener onMarkStatus = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Button tags contain corresponding state
                    String state = (String) v.getTag();
                    expense.setState(state);
                    expense.setExpanded(false);
                    notifyItemChanged(expenses.indexOf(expense));

                    if(listener != null) {
                        ApiData apiData = new ApiData();
                        apiData.setExpenses(expenses);
                        listener.onExpenseChanged(expense, apiData);
                    }
                }
            };

            v.findViewById(R.id.mark_fraud).setOnClickListener(onMarkStatus);
            v.findViewById(R.id.mark_unverified).setOnClickListener(onMarkStatus);
            v.findViewById(R.id.mark_verified).setOnClickListener(onMarkStatus);

            leftIcon = (SimpleDraweeView) v.findViewById(R.id.left_icon);
            rightIcon = (SimpleDraweeView) v.findViewById(R.id.right_icon);
        }

        public void setExpense(Expense expense) {
            this.expense = expense;
            binding.setExpense(expense);
            ColorDrawable cd = new ColorDrawable();
            int color = State.forName(expense.getState()).getColorResource();
            cd.setColor(itemView.getResources().getColor(color));
            RoundingParams rp = binding.expenseIcon.getHierarchy().getRoundingParams();
            GenericDraweeHierarchy gdh = new GenericDraweeHierarchyBuilder(itemView.getResources())
                    .setBackground(cd)
                    .setRoundingParams(rp)
                    .build();
            gdh.setPlaceholderImage(expense.getIcon());
            binding.expenseIcon.setHierarchy(gdh);
            setStatusSwitcherState(expense.isExpanded(), 0);
        }

        @Override
        public void onClick(View v) {
            setStatusSwitcherState(!expense.isExpanded(), 300);
        }

        @Override
        public View getSwipeableContainerView() {
            return itemView.findViewById(R.id.container);
        }

        public void setStatusSwitcherState(boolean expanded, long duration) {
            if(expanded) {
                statusSwitcher.setVisibility(View.VISIBLE);
                statusSwitcher.setAlpha(0);
                statusSwitcher.animate().alpha(0.9f).setDuration(duration).start();
                expense.setExpanded(true);
            } else {
                statusSwitcher.setAlpha(0.9f);
                statusSwitcher.animate().alpha(0).setDuration(duration).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        statusSwitcher.setVisibility(View.GONE);
                        expense.setExpanded(false);
                    }
                }).start();
            }
        }
    }

    class SwipeMarkState extends SwipeResultActionDefault {
        public SwipeMarkState(int position, State state) {
            Expense expense = expenses.get(position);
            expense.setState(state.getName());
            expense.setExpanded(false);
            notifyItemChanged(expenses.indexOf(expense));

            if(listener != null) {
                ApiData apiData = new ApiData();
                apiData.setExpenses(expenses);
                listener.onExpenseChanged(expense, apiData);
            }
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
        }
    }
}
