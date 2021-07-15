package com.xwray.groupie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that holds a list of Groups.
 */
public class GroupAdapter<VH extends GroupieViewHolder> extends RecyclerView.Adapter<VH> implements GroupDataObserver, AsyncDiffUtil.Callback {

    private final List<Group> groups = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private int spanCount = 1;
    private Item lastItemForViewTypeLookup;

    private final AsyncDiffUtil asyncDiffUtil = new AsyncDiffUtil(this);

    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            try {
                return getItem(position).getSpanSize(spanCount, position);
            } catch (IndexOutOfBoundsException e) {
                // Bug in support lib?  TODO investigate further
                return spanCount;
            }
        }
    };

    @NonNull
    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return spanSizeLookup;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public int getSpanCount() {
        return spanCount;
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     * <p>
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     * <br/> <br/>
     * If you want to receive a callback once the update is complete call the
     * {@link #updateAsync(List, boolean, OnAsyncUpdateListener)} version
     * <p>
     * This will default detectMoves to true.
     *
     * @param newGroups List of {@link Group}
     */
    public void updateAsync(@NonNull final List<Group> newGroups) {
        this.updateAsync(newGroups, true, null);
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     * <p>
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     * <br/> <br/>
     * <p>
     * This will default detectMoves to true.
     *
     * @param newGroups List of {@link Group}
     * @see #updateAsync(List, boolean, OnAsyncUpdateListener)
     */
    public void updateAsync(@NonNull final List<? extends Group> newGroups, @Nullable final OnAsyncUpdateListener onAsyncUpdateListener) {
        this.updateAsync(newGroups, true, onAsyncUpdateListener);
    }

    /**
     * Updates the adapter with a new list that will be diffed on a background thread
     * and displayed once diff results are calculated.
     * <p>
     * NOTE: This update method is NOT compatible with partial updates (change notifications
     * driven by individual groups and items).  If you update using this method, all partial
     * updates will no longer work and you must use this method to update exclusively.
     *
     * @param newGroups             List of {@link Group}
     * @param onAsyncUpdateListener Optional callback for when the async update is complete
     * @param detectMoves           Boolean is passed to {@link DiffUtil#calculateDiff(DiffUtil.Callback, boolean)}. Set to true
     *                              if you want DiffUtil to detect moved items.
     */
    public void updateAsync(@NonNull final List<? extends Group> newGroups, boolean detectMoves, @Nullable final OnAsyncUpdateListener onAsyncUpdateListener) {
        // Fast simple first insert
        if (groups.isEmpty()) {
            update(newGroups, detectMoves);
            if (onAsyncUpdateListener != null) {
                onAsyncUpdateListener.onUpdateComplete();
            }
            return;
        }

        asyncDiffUtil.calculateDiff(groups, newGroups, onAsyncUpdateListener, detectMoves);
    }

    /**
     * Replaces the groups within the adapter without using DiffUtil, and therefore without animations.
     * <p>
     * For animation support, use {@link GroupAdapter#update(List)} or {@link GroupAdapter#updateAsync(List)} instead.
     *
     * @param newGroups List of {@link Group}
     */
    public void replaceAll(@NonNull final List<? extends Group> newGroups) {
        setNewGroups(newGroups);
        notifyDataSetChanged();
    }

    /**
     * Updates the adapter with a new list that will be diffed on the <em>main</em> thread
     * and displayed once diff results are calculated. Not recommended for huge lists.
     * <p>
     * This will default detectMoves to true.
     *
     * @param newGroups List of {@link Group}
     */
    public void update(@NonNull final List<? extends Group> newGroups) {
        update(newGroups, true);
    }

    /**
     * Updates the adapter with a new list that will be diffed on the <em>main</em> thread
     * and displayed once diff results are calculated. Not recommended for huge lists.
     *
     * @param newGroups   List of {@link Group}
     * @param detectMoves is passed to {@link DiffUtil#calculateDiff(DiffUtil.Callback, boolean)}. Set to false
     *                    if you don't want DiffUtil to detect moved items.
     */
    public void update(@NonNull final List<? extends Group> newGroups, boolean detectMoves) {
        final List<Group> oldGroups = new ArrayList<>(groups);
        DiffCallback diffCallback = new DiffCallback(oldGroups, newGroups);

        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                diffCallback,
                detectMoves
        );

        setNewGroups(diffCallback.mergeGroups());
        diffResult.dispatchUpdatesTo((ListUpdateCallback) this);
    }

    /**
     * Optionally register an {@link OnItemClickListener} that listens to click at the root of
     * each Item where {@link Item#isClickable()} returns true
     *
     * @param onItemClickListener The click listener to set
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Optionally register an {@link OnItemLongClickListener} that listens to long click at the root of
     * each Item where {@link Item#isLongClickable()} returns true
     *
     * @param onItemLongClickListener The long click listener to set
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onDispatchAsyncResult(List<Group> mergedGroups) {
        setNewGroups(mergedGroups);
    }

    @Override
    public void onInserted(int position, int count) {
        notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        notifyItemRangeChanged(position, count, payload);
    }

    @Override
    @NonNull
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Item<VH> item = getItemForViewType(viewType);
        View itemView = inflater.inflate(item.getLayout(), parent, false);
        return item.createViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Never called (all binds go through the version with payload)
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        Item contentItem = getItem(position);
        contentItem.bind(holder, position, payloads, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        Item contentItem = holder.getItem();
        contentItem.unbind(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull VH holder) {
        Item contentItem = holder.getItem();
        return contentItem.isRecyclable();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        super.onViewAttachedToWindow(holder);
        Item item = getItem(holder);
        //noinspection unchecked
        item.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
        super.onViewDetachedFromWindow(holder);
        Item item = getItem(holder);
        //noinspection unchecked
        item.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemViewType(int position) {
        lastItemForViewTypeLookup = getItem(position);
        return lastItemForViewTypeLookup.getViewType();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public @NonNull
    Item getItem(@NonNull VH holder) {
        return holder.getItem();
    }

    public @NonNull
    Item getItem(int position) {
        return GroupUtils.getItem(groups, position);
    }

    public int getAdapterPosition(@NonNull Item contentItem) {
        int count = 0;
        for (Group group : groups) {
            int index = group.getPosition(contentItem);
            if (index >= 0) return index + count;
            count += group.getItemCount();
        }
        return -1;
    }

    /**
     * The position in the flat list of individual items at which the group starts
     */
    public int getAdapterPosition(@NonNull Group group) {
        int index = groups.indexOf(group);
        if (index == -1) return -1;
        int position = 0;
        for (int i = 0; i < index; i++) {
            position += groups.get(i).getItemCount();
        }
        return position;
    }

    /**
     * Returns the number of top-level groups present in the adapter.
     */
    public int getGroupCount() {
        return groups.size();
    }

    /**
     * This returns the total number of items contained in all groups in this adapter
     */
    @Override
    public int getItemCount() {
        return GroupUtils.getItemCount(groups);
    }

    /**
     * This returns the total number of items contained in the top level group at the passed index
     */
    public int getItemCountForGroup(int groupIndex) {
        if (groupIndex >= groups.size()) {
            throw new IndexOutOfBoundsException("Requested group index " + groupIndex + " but there are " + groups.size() + " groups");
        }
        return groups.get(groupIndex).getItemCount();
    }

    /**
     * This returns the total number of items contained in the top level group at the passed index
     *
     * @deprecated This method has been deprecated in favour of {@link #getItemCountForGroup(int)}. Please use that method instead.
     */
    @Deprecated
    public int getItemCount(int groupIndex) {
        return getItemCountForGroup(groupIndex);
    }

    public void clear() {
        for (Group group : groups) {
            group.unregisterGroupDataObserver(this);
        }
        groups.clear();
        notifyDataSetChanged();
    }

    public void add(@NonNull Group group) {
        int itemCountBeforeGroup = getItemCount();
        group.registerGroupDataObserver(this);
        groups.add(group);
        notifyItemRangeInserted(itemCountBeforeGroup, group.getItemCount());
    }

    /**
     * Adds the contents of the list of groups, in order, to the end of the adapter contents.
     * All groups in the list must be non-null.
     */
    public void addAll(@NonNull List<? extends Group> groups) {
        if (groups.contains(null)) throw new RuntimeException("List of groups can't contain null!");
        int itemCountBeforeGroup = getItemCount();
        int additionalSize = 0;
        for (Group group : groups) {
            additionalSize += group.getItemCount();
            group.registerGroupDataObserver(this);
        }
        this.groups.addAll(groups);
        notifyItemRangeInserted(itemCountBeforeGroup, additionalSize);
    }

    public void remove(@NonNull Group group) {
        int position = groups.indexOf(group);
        remove(position, group);
    }

    public void removeAll(@NonNull List<? extends Group> groups) {
        for (Group group : groups) {
            remove(group);
        }
    }

    /**
     * Remove a Group at a raw adapter position
     *
     * @param position raw adapter position of Group to remove
     */
    public void removeGroupAtAdapterPosition(int position) {
        Group group = getGroupAtAdapterPosition(position);
        remove(position, group);
    }

    /**
     * Remove a Group at a raw adapter position.
     *
     * @param adapterPosition raw adapter position of Group to remove.
     * @deprecated This method has been deprecated in favor of {@link #removeGroupAtAdapterPosition(int)}. Please use that method instead.
     */
    @Deprecated
    public void removeGroup(int adapterPosition) {
        removeGroupAtAdapterPosition(adapterPosition);
    }

    private void remove(int position, @NonNull Group group) {
        int itemCountBeforeGroup = getItemCountBeforeGroup(position);
        group.unregisterGroupDataObserver(this);
        groups.remove(position);
        notifyItemRangeRemoved(itemCountBeforeGroup, group.getItemCount());
    }

    public void add(int index, @NonNull Group group) {
        group.registerGroupDataObserver(this);
        groups.add(index, group);
        int itemCountBeforeGroup = getItemCountBeforeGroup(index);
        notifyItemRangeInserted(itemCountBeforeGroup, group.getItemCount());
    }

    /**
     * Get group, given a top level group position. If you want to get a group at an adapter position
     * then use {@link #getGroupAtAdapterPosition(int)}
     *
     * @param position Top level group position
     * @return Group at that position or throws {@link IndexOutOfBoundsException}
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public Group getTopLevelGroup(int position) {
        return groups.get(position);
    }

    /**
     * Get group, given a raw adapter position. If you want to get a top level group by position
     * then use {@link #getTopLevelGroup(int)}
     *
     * @param position raw adapter position
     * @return Group at that position or throws {@link IndexOutOfBoundsException}
     */
    @NonNull
    public Group getGroupAtAdapterPosition(int position) {
        int previous = 0;
        int size;
        for (Group group : groups) {
            size = group.getItemCount();
            if (position - previous < size) return group;
            previous += group.getItemCount();
        }
        throw new IndexOutOfBoundsException("Requested position " + position + " in group adapter " +
                "but there are only " + previous + " items");
    }

    /**
     * Get group, given a raw adapter position. If you want to get a top level group by position
     * then use {@link #getTopLevelGroup(int)}
     *
     * @param adapterPosition raw adapter position
     * @return Group at that position or throws {@link IndexOutOfBoundsException}
     * @deprecated This method is deprecated and has been replaced with {@link #getGroupAtAdapterPosition(int)}. Please use that method instead.
     */
    @Deprecated
    @NonNull
    public Group getGroup(int adapterPosition) {
        return getGroupAtAdapterPosition(adapterPosition);
    }

    /**
     * Returns the Group which contains this item or throws an {@link IndexOutOfBoundsException} if not present.
     * This is the item's <b>direct</b> parent, not necessarily one of the top level groups present in this adapter.
     *
     * @param contentItem Item to find the parent group for.
     * @return Parent group of this item.
     */
    @NonNull
    public Group getGroup(Item contentItem) {
        for (Group group : groups) {
            if (group.getPosition(contentItem) >= 0) {
                return group;
            }
        }
        throw new IndexOutOfBoundsException("Item is not present in adapter or in any group");
    }

    @Override
    public void onChanged(@NonNull Group group) {
        notifyItemRangeChanged(getAdapterPosition(group), group.getItemCount());
    }

    @Override
    public void onItemInserted(@NonNull Group group, int position) {
        notifyItemInserted(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position) {
        notifyItemChanged(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position, Object payload) {
        notifyItemChanged(getAdapterPosition(group) + position, payload);
    }

    @Override
    public void onItemRemoved(@NonNull Group group, int position) {
        notifyItemRemoved(getAdapterPosition(group) + position);
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeChanged(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount, Object payload) {
        notifyItemRangeChanged(getAdapterPosition(group) + positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeInserted(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeInserted(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemRangeRemoved(@NonNull Group group, int positionStart, int itemCount) {
        notifyItemRangeRemoved(getAdapterPosition(group) + positionStart, itemCount);
    }

    @Override
    public void onItemMoved(@NonNull Group group, int fromPosition, int toPosition) {
        int groupAdapterPosition = getAdapterPosition(group);
        notifyItemMoved(groupAdapterPosition + fromPosition, groupAdapterPosition + toPosition);
    }

    @Override
    public void onDataSetInvalidated() {
        notifyDataSetChanged();
    }

    /**
     * This idea was copied from Epoxy. :wave: Bright idea guys!
     * <p>
     * Find the model that has the given view type so we can create a viewholder for that model.
     * <p>
     * To make this efficient, we rely on the RecyclerView implementation detail that {@link
     * GroupAdapter#getItemViewType(int)} is called immediately before {@link
     * GroupAdapter#onCreateViewHolder(android.view.ViewGroup, int)}. We cache the last model
     * that had its view type looked up, and unless that implementation changes we expect to have a
     * very fast lookup for the correct model.
     * <p>
     * To be safe, we fallback to searching through all models for a view type match. This is slow and
     * shouldn't be needed, but is a guard against RecyclerView behavior changing.
     */
    private Item<VH> getItemForViewType(int viewType) {
        if (lastItemForViewTypeLookup != null
                && lastItemForViewTypeLookup.getViewType() == viewType) {
            // We expect this to be a hit 100% of the time
            return lastItemForViewTypeLookup;
        }

        // To be extra safe in case RecyclerView implementation details change...
        for (int i = 0; i < getItemCount(); i++) {
            Item item = getItem(i);
            if (item.getViewType() == viewType) {
                return item;
            }
        }

        throw new IllegalStateException("Could not find model for view type: " + viewType);
    }

    private int getItemCountBeforeGroup(int groupIndex) {
        int count = 0;
        for (Group group : groups.subList(0, groupIndex)) {
            count += group.getItemCount();
        }
        return count;
    }

    private void setNewGroups(@NonNull List<? extends Group> newGroups) {
        for (Group group : groups) {
            group.unregisterGroupDataObserver(this);
        }

        groups.clear();
        groups.addAll(newGroups);

        for (Group group : newGroups) {
            group.registerGroupDataObserver(this);
        }
    }

}