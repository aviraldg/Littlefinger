package com.aviraldg.littlefinger;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviraldg.littlefinger.api.models.Expense;
import com.aviraldg.littlefinger.ui.ExpenseFragment;
import com.aviraldg.littlefinger.ui.ExpensesAdapter;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;

public class MainActivityFragment extends Fragment implements ExpensesAdapter.ExpenseListEventListener {
    private RecyclerView recyclerView;
    private ExpensesAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewTouchActionGuardManager recyclerViewTouchActionGuardManager;
    private RecyclerViewSwipeManager recyclerViewSwipeManager;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter wrappedAdapter;

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

        adapter.refresh();
    }

    void bindViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.expense_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    }

    @Override
    public void onExpenseClicked(Expense expense) {
        ExpenseFragment ef = new ExpenseFragment();
        ef.show(getFragmentManager(), "ExpenseFragment");
    }

    @Override
    public void onExpenseListRefreshFailed() {
        View v = getView();
        if(v == null) return;

        Snackbar.make(v, R.string.expenses_refresh_failed, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.refresh();
                    }
                })
                .show();

    }
}
