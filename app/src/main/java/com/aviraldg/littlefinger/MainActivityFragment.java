package com.aviraldg.littlefinger;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviraldg.littlefinger.api.LittlefingerApi;
import com.aviraldg.littlefinger.api.models.ApiData;
import com.aviraldg.littlefinger.api.models.Expense;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityFragment extends Fragment implements ExpensesAdapter.ExpenseListEventListener {
    private static final long REFRESH_DELAY = 10*1000;
    private static LittlefingerApi api = LittlefingerApplication.getApi();
    private RecyclerView recyclerView;
    private ExpensesAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewTouchActionGuardManager recyclerViewTouchActionGuardManager;
    private RecyclerViewSwipeManager recyclerViewSwipeManager;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter wrappedAdapter;
    private ContentLoadingProgressBar expensesLoadingProgressBar;
    private Snackbar snackbar;
    private Timer timer = new Timer();

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        initUi();
    }

    void initUi() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ExpensesAdapter(swipeRefreshLayout);
        adapter.setExpenseClickListener(this);

        recyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        recyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        recyclerViewTouchActionGuardManager.setEnabled(true);

        recyclerViewSwipeManager = new RecyclerViewSwipeManager();

        wrappedAdapter = recyclerViewSwipeManager.createWrappedAdapter(adapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        recyclerView.setAdapter(wrappedAdapter);
        recyclerView.setItemAnimator(animator);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        recyclerViewTouchActionGuardManager.attachRecyclerView(recyclerView);
        recyclerViewSwipeManager.attachRecyclerView(recyclerView);
    }

    void bindViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.expense_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        expensesLoadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.expenses_loading_progress_bar);
    }

    @Override
    public void onStart() {
        super.onStart();

        adapter.refresh();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if(activity == null) return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.refresh();
                    }
                });
            }
        }, 0, REFRESH_DELAY);
    }

    @Override
    public void onStop() {
        super.onStop();

        timer.purge();
    }

    @Override
    public void onExpenseChanged(final Expense expense, final ApiData data) {
        api.updateExpenses(data)
                .enqueue(new Callback<ApiData>() {
                    @Override
                    public void onResponse(Call<ApiData> call, Response<ApiData> response) {
                        if(response.isSuccess()) {
                            View v = getView();
                            if(v != null) {
                                Snackbar.make(v, "Expenses updated successfully", Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            handleFailure();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiData> call, Throwable t) {
                        handleFailure();
                    }

                    private void handleFailure() {
                        View v = getView();
                        if(v != null) {
                            String oldState = expense.getOldState();
                            final String state = expense.getState();

                            // Revert change
                            expense.setState(oldState);
                            adapter.notifyDataSetChanged(); // TODO notify only for changed item

                            Snackbar.make(v, "Failed to update expenses", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            expense.setState(state);
                                            adapter.notifyDataSetChanged(); // TODO notify only for changed item

                                            onExpenseChanged(expense, data);
                                        }
                                    }).show();
                        }


                    }
                });
    }

    @Override
    public void onExpenseListRefreshed(List<Expense> expenses) {
        expensesLoadingProgressBar.hide();

        if(snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onExpenseListRefreshFailed() {
        expensesLoadingProgressBar.hide();

        View v = getView();
        if(v == null) return;

        snackbar = Snackbar.make(v, R.string.expenses_refresh_failed, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.refresh();
                    }
                });
        snackbar.show();
    }
}
