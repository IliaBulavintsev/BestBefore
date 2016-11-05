package com.rv150.bestbefore.Activities;

import android.app.DialogFragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.rv150.bestbefore.Dialogs.DeleteAllOverdued;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.RecyclerAdapter;
import com.rv150.bestbefore.Services.StatCollector;
import com.rv150.bestbefore.Models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudnev on 06.09.2016.
 */

// Активити "просроченные продукты"
public class Overdue extends AppCompatActivity {
    private List<Product> overdueList;
    private TextView isEmpty;
    private RecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private int position = -1;
    private Product deletedProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overdue);

        isEmpty = (TextView) findViewById(R.id.isEmptyOverdue);

        recyclerView = (RecyclerView) findViewById(R.id.overdue_list);
        overdueList = new ArrayList<>();
        adapter = new RecyclerAdapter(overdueList);
        setUpRecyclerView();
    }



    @Override
    protected void onResume() {
        super.onResume();

        overdueList = ProductDAO.getOverdueProducts(this); // обновляем overdueList в соотв. с сохраненными данными

        adapter = new RecyclerAdapter(overdueList);
        recyclerView.swapAdapter(adapter, false);

        Typeface font = Typeface.createFromAsset(getAssets(), "san.ttf");
        isEmpty.setTypeface(font);
        if (overdueList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        } else {
            isEmpty.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overdue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_list && !overdueList.isEmpty()) {
            DialogFragment dialog_delete_all = new DeleteAllOverdued();
            dialog_delete_all.show(getFragmentManager(), "ClearList");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void deleteItem() {
        deletedProduct = overdueList.get(position);
        overdueList.remove(position);
        if (overdueList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }

        adapter = new RecyclerAdapter(overdueList);
        recyclerView.swapAdapter(adapter, false);
        ProductDAO.saveOverdueProducts(overdueList, this);
        StatCollector.shareStatistic(this, "deleted one overdue product");
    }

    private void restoreItem() {
        overdueList.add(position, deletedProduct);
        deletedProduct = null;
        position = -1;
        adapter = new RecyclerAdapter(overdueList);
        recyclerView.swapAdapter(adapter, false);
        ProductDAO.saveOverdueProducts(overdueList, this);
        StatCollector.shareStatistic(this, "restored overdue item");

        // Надпись "Список пуст!"
        isEmpty.setVisibility(View.INVISIBLE);
    }



    public void clearList() {
        overdueList.clear();
        ProductDAO.saveOverdueProducts(overdueList, this);
        adapter = new RecyclerAdapter(overdueList);
        recyclerView.swapAdapter(adapter, false);
        isEmpty.setVisibility(View.VISIBLE);
        StatCollector.shareStatistic(this, "deleted all overdue products");
    }


    private void setUpRecyclerView() {
        // Attach the adapter to the recyclerview to populate items


        recyclerView.setAdapter(adapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }


    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                    // we want to cache these and not allocate anything repeatedly in the onChildDraw method
                    Drawable background;
                    Drawable xMark;
                    int xMarkMargin;
                    boolean initiated;

                    private void init() {
                        background = new ColorDrawable(Color.RED);
                        xMark = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_clear_24dp);
                        xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        xMarkMargin = (int) getResources().getDimension(R.dimen.ic_clear_margin);
                        initiated = true;
                    }

                    // not important, we don't want drag & drop
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        position = viewHolder.getAdapterPosition();;
                        deleteItem();
                        View parentLayout = findViewById(R.id.overdue_list);
                        Snackbar snackbar = Snackbar
                                .make(parentLayout, R.string.product_has_been_deleted, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        restoreItem();
                                    }
                                });

                        snackbar.show();
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        View itemView = viewHolder.itemView;

                        // not sure why, but this method get's called for viewholder that are already swiped away
                        if (viewHolder.getAdapterPosition() == -1) {
                            // not interested in those
                            return;
                        }

                        if (!initiated) {
                            init();
                        }

                        // draw red background
                        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        background.draw(c);

                        // draw x mark
                        int itemHeight = itemView.getBottom() - itemView.getTop();
                        int intrinsicWidth = xMark.getIntrinsicWidth();
                        int intrinsicHeight = xMark.getIntrinsicWidth();

                        int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                        int xMarkRight = itemView.getRight() - xMarkMargin;
                        int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                        int xMarkBottom = xMarkTop + intrinsicHeight;
                        xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                        xMark.draw(c);

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }

                };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
         recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }
}

