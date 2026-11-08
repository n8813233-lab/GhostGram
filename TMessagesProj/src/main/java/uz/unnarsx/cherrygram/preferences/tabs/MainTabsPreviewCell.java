/**
 * GhostGram C4 (PLAN.md 14.3): LOCAL implementation.
 *
 * The donor's MainTabsPreferencesEntry references MainTabsPreviewCell, but
 * that class is absent from the donor's published source tree - the same
 * situation as the classes locally implemented in C2 (PLAN.md 12.0) and the
 * Extra shim in C3 (PLAN.md 13.6). Only the surface the settings entry
 * actually calls is implemented here:
 *   setEditMode(boolean), setOnTabsChangedListener(Runnable),
 *   setTabs(List<Tab>, Context, ResourcesProvider, int, boolean, boolean).
 *
 * Behaviour: a horizontal strip of the current tabs rendered through
 * MainTabsManager.createTabView. In edit mode a tap toggles a tab's enabled
 * state (CHATS is always enabled, matching MainTabsManager.loadTabs) and a
 * horizontal drag reorders them. Every change is written back through
 * MainTabsManager.saveTabs (the donor entry itself never saves; without this
 * call the settings page would lose edits), then the listener runs so the
 * entry can rebuild fragment views. No network, no remote data.
 */

package uz.unnarsx.cherrygram.preferences.tabs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.glass.GlassTabView;

import java.util.List;

import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager;

public class MainTabsPreviewCell extends FrameLayout {

    private final RecyclerView listView;
    private final TabsAdapter adapter;
    private boolean editMode;
    private Runnable tabsChangedListener;

    public MainTabsPreviewCell(Context context) {
        super(context);
        listView = new RecyclerView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        adapter = new TabsAdapter();
        listView.setAdapter(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                    return false;
                }
                adapter.moveTab(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return editMode;
            }
        });
        touchHelper.attachToRecyclerView(listView);
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48));
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void setOnTabsChangedListener(Runnable listener) {
        this.tabsChangedListener = listener;
    }

    public void setTabs(List<MainTabsManager.Tab> tabs, Context context,
                        Theme.ResourcesProvider resourceProvider, int currentAccount,
                        boolean fromSettings, boolean showSearch) {
        adapter.setData(tabs, context, resourceProvider, currentAccount, fromSettings, showSearch);
    }

    private void notifyChanged() {
        if (adapter.tabs != null) {
            MainTabsManager.INSTANCE.saveTabs(adapter.tabs);
        }
        if (tabsChangedListener != null) {
            tabsChangedListener.run();
        }
    }

    private class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.TabHolder> {
        private List<MainTabsManager.Tab> tabs;
        private Context context;
        private Theme.ResourcesProvider resourceProvider;
        private int currentAccount;
        private boolean fromSettings;
        private boolean showSearch;

        void setData(List<MainTabsManager.Tab> tabs, Context context,
                     Theme.ResourcesProvider resourceProvider, int currentAccount,
                     boolean fromSettings, boolean showSearch) {
            this.tabs = tabs;
            this.context = context;
            this.resourceProvider = resourceProvider;
            this.currentAccount = currentAccount;
            this.fromSettings = fromSettings;
            this.showSearch = showSearch;
            notifyDataSetChanged();
        }

        void moveTab(int from, int to) {
            if (tabs == null || from < 0 || to < 0 || from >= tabs.size() || to >= tabs.size()) {
                return;
            }
            MainTabsManager.Tab tab = tabs.remove(from);
            tabs.add(to, tab);
            notifyItemMoved(from, to);
            notifyChanged();
        }

        @NonNull
        @Override
        public TabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout wrapper = new FrameLayout(parent.getContext());
            wrapper.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new TabHolder(wrapper);
        }

        @Override
        public void onBindViewHolder(@NonNull TabHolder holder, int position) {
            MainTabsManager.Tab tab = tabs.get(position);
            holder.wrapper.removeAllViews();
            GlassTabView tabView = MainTabsManager.INSTANCE.createTabView(
                    context, resourceProvider, currentAccount, tab.getType(), fromSettings, showSearch);
            tabView.setAlpha(tab.enabled ? 1f : 0.35f);
            holder.wrapper.addView(tabView, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));
            holder.wrapper.setOnClickListener(v -> {
                if (!editMode) {
                    return;
                }
                // CHATS is force-enabled by MainTabsManager.loadTabs; keep it that way.
                if (tab.getType() == MainTabsManager.TabType.CHATS) {
                    return;
                }
                tab.enabled = !tab.enabled;
                notifyItemChanged(holder.getAdapterPosition());
                notifyChanged();
            });
        }

        @Override
        public int getItemCount() {
            return tabs == null ? 0 : tabs.size();
        }

        class TabHolder extends RecyclerView.ViewHolder {
            final FrameLayout wrapper;

            TabHolder(FrameLayout wrapper) {
                super(wrapper);
                this.wrapper = wrapper;
            }
        }
    }
}
