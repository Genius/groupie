package com.xwray.groupie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A group which has a list of contents and an optional header and footer.
 */
public class Section extends NestedGroup {
    @Nullable
    private Group header;

    @Nullable
    private Group footer;

    @Nullable
    private Group placeholder;

    private final ArrayList<Group> children = new ArrayList<>();

    private boolean hideWhenEmpty = false;

    private boolean isHeaderAndFooterVisible = true;

    private boolean isPlaceholderVisible = false;

    public Section() {
        this(null, new ArrayList<Group>());
    }

    public Section(@Nullable Group header) {
        this(header, new ArrayList<Group>());
    }

    public Section(@NonNull Collection<? extends Group> children) {
        this(null, children);
    }

    public Section(@Nullable Group header, @NonNull Collection<? extends Group> children) {
        this.header = header;
        if (header != null) {
            header.registerGroupDataObserver(this);
        }
        addAll(children);
    }

    @Override
    public void add(int position, @NonNull Group group) {
        super.add(position, group);
        children.add(position, group);
        final int notifyPosition = getHeaderItemCount() + GroupUtils.getItemCount(children.subList(0, position));
        notifyItemRangeInserted(notifyPosition, group.getItemCount());
        refreshEmptyState();
    }

    @Override
    public void addAll(@NonNull Collection<? extends Group> groups) {
        if (groups.isEmpty()) return;
        super.addAll(groups);
        int position = getItemCountWithoutFooter();
        this.children.addAll(groups);
        notifyItemRangeInserted(position, GroupUtils.getItemCount(groups));
        refreshEmptyState();
    }

    @Override
    public void addAll(int position, @NonNull Collection<? extends Group> groups) {
        if (groups.isEmpty()) {
            return;
        }

        super.addAll(position, groups);
        this.children.addAll(position, groups);

        final int notifyPosition = getHeaderItemCount() + GroupUtils.getItemCount(children.subList(0, position));
        notifyItemRangeInserted(notifyPosition, GroupUtils.getItemCount(groups));
        refreshEmptyState();
    }

    @Override
    public void add(@NonNull Group group) {
        super.add(group);
        int position = getItemCountWithoutFooter();
        children.add(group);
        notifyItemRangeInserted(position, group.getItemCount());
        refreshEmptyState();
    }

    @Override
    public void remove(@NonNull Group group) {
        super.remove(group);
        int position = getItemCountBeforeGroup(group);
        children.remove(group);
        notifyItemRangeRemoved(position, group.getItemCount());
        refreshEmptyState();
    }

    @Override
    public void removeAll(@NonNull Collection<? extends Group> groups) {
        if (groups.isEmpty()) {
            return;
        }

        super.removeAll(groups);
        for (Group group : groups) {
            int position = getItemCountBeforeGroup(group);
            children.remove(group);
            notifyItemRangeRemoved(position, group.getItemCount());
        }
        refreshEmptyState();
    }

    @Override
    public void replaceAll(@NonNull Collection<? extends Group> groups) {
        if (groups.isEmpty()) {
            return;
        }

        super.replaceAll(groups);
        children.clear();
        children.addAll(groups);

        notifyDataSetInvalidated();

        refreshEmptyState();
    }

    /**
     * Get the list of all groups in this section, wrapped in a new {@link ArrayList}. This
     * does <strong>not include headers, footers or placeholders</strong>.
     * @return The list of all groups in this section, wrapped in a new {@link ArrayList}
     */
    public List<Group> getGroups() {
        return new ArrayList<>(this.children);
    }

    /**
     * Remove all existing body content.
     */
    public void clear() {
        if (children.isEmpty()) {
            return;
        }
        removeAll(new ArrayList<>(children));
    }

    /**
     * Replace all existing body content and dispatch fine-grained change notifications to the
     * parent using DiffUtil.
     * <p>
     * Item comparisons are made using:
     * - Item.isSameAs(Item otherItem) (are items the same?)
     * - Item.hasSameContentAs() (are contents the same?)
     * <p>
     * If you don't customize getId() or isSameAs() and hasSameContentAs(), the default implementations will return false,
     * meaning your Group will consider every update a complete change of everything.
     *
     * This will default detectMoves to true.
     *
     * @see #update(Collection, boolean)
     * @param newBodyGroups The new content of the section
     */
    public void update(@NonNull final Collection<? extends Group> newBodyGroups) {
        update(newBodyGroups, true);
    }

    /**
     * Replace all existing body content and dispatch fine-grained change notifications to the
     * parent using DiffUtil.
     * <p>
     * Item comparisons are made using:
     * - Item.isSameAs(Item otherItem) (are items the same?)
     * - Item.hasSameContentAs() (are contents the same?)
     * <p>
     * If you don't customize getId() or isSameAs() and hasSameContentAs(), the default implementations will return false,
     * meaning your Group will consider every update a complete change of everything.
     *
     * @param newBodyGroups The new content of the section
     * @param detectMoves is passed to {@link DiffUtil#calculateDiff(DiffUtil.Callback, boolean)}. Set to false if you
     *                    don't want DiffUtil to detect moved items.
     */
    public void update(@NonNull final Collection<? extends Group> newBodyGroups, boolean detectMoves) {
        final List<Group> oldBodyGroups = new ArrayList<>(children);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(oldBodyGroups, newBodyGroups), detectMoves);
        this.update(newBodyGroups, diffResult);
    }

    /**
     * Overloaded version of update method in which you can pass your own DiffUtil.DiffResult
     * @param newBodyGroups The new content of the section
     * @param diffResult
     */
    public void update(@NonNull final Collection<? extends Group> newBodyGroups, DiffUtil.DiffResult diffResult) {
        super.removeAll(children);
        children.clear();
        children.addAll(newBodyGroups);
        super.addAll(newBodyGroups);

        diffResult.dispatchUpdatesTo(listUpdateCallback);
        refreshEmptyState();
    }

    private ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(getHeaderItemCount() + position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(getHeaderItemCount() + position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            final int headerItemCount = getHeaderItemCount();
            notifyItemMoved(headerItemCount + fromPosition, headerItemCount + toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            notifyItemRangeChanged(getHeaderItemCount() + position, count, payload);
        }
    };

    /**
     * Optional. Set a placeholder for when the section's body is empty.
     * <p>
     * If setHideWhenEmpty(true) is set, then the empty placeholder will not be shown.
     *
     * @param placeholder A placeholder to be shown when there is no body content
     */
    public void setPlaceholder(@NonNull Group placeholder) {
        //noinspection ConstantConditions
        if (placeholder == null)
            throw new NullPointerException("Placeholder can't be null.  Please use removePlaceholder() instead!");
        if (this.placeholder != null) {
            removePlaceholder();
        }
        this.placeholder = placeholder;
        refreshEmptyState();
    }

    public void removePlaceholder() {
        hidePlaceholder();
        this.placeholder = null;
    }

    private void showPlaceholder() {
        if (isPlaceholderVisible || placeholder == null) return;
        isPlaceholderVisible = true;
        notifyItemRangeInserted(getHeaderItemCount(), placeholder.getItemCount());
    }

    private void hidePlaceholder() {
        if (!isPlaceholderVisible || placeholder == null) return;
        isPlaceholderVisible = false;
        notifyItemRangeRemoved(getHeaderItemCount(), placeholder.getItemCount());
    }

    /**
     * Whether a section's contents are visually empty
     *
     * @return
     */
    protected boolean isEmpty() {
        return children.isEmpty() || GroupUtils.getItemCount(children) == 0;
    }

    private void hideDecorations() {
        if (!isHeaderAndFooterVisible && !isPlaceholderVisible) return;
        int count = getHeaderItemCount() + getPlaceholderItemCount() + getFooterItemCount();
        isHeaderAndFooterVisible = false;
        isPlaceholderVisible = false;
        notifyItemRangeRemoved(0, count);
    }

    protected void refreshEmptyState() {
        boolean isEmpty = isEmpty();
        if (isEmpty) {
            if (hideWhenEmpty) {
                hideDecorations();
            } else {
                showPlaceholder();
                showHeadersAndFooters();
            }
        } else {
            hidePlaceholder();
            showHeadersAndFooters();
        }
    }

    private void showHeadersAndFooters() {
        if (isHeaderAndFooterVisible) return;
        isHeaderAndFooterVisible = true;
        notifyItemRangeInserted(0, getHeaderItemCount());
        notifyItemRangeInserted(getItemCountWithoutFooter(), getFooterItemCount());
    }

    private int getBodyItemCount() {
        return isPlaceholderVisible ? getPlaceholderItemCount() : GroupUtils.getItemCount(children);
    }

    private int getItemCountWithoutFooter() {
        return getBodyItemCount() + getHeaderItemCount();
    }

    private int getHeaderCount() {
        return header == null || !isHeaderAndFooterVisible ? 0 : 1;
    }

    private int getHeaderItemCount() {
        return getHeaderCount() == 0 ? 0 : header.getItemCount();
    }

    private int getFooterItemCount() {
        return getFooterCount() == 0 ? 0 : footer.getItemCount();
    }

    private int getFooterCount() {
        return footer == null || !isHeaderAndFooterVisible ? 0 : 1;
    }

    private int getPlaceholderCount() {
        return isPlaceholderVisible ? 1 : 0;
    }

    @Override
    @NonNull
    public Group getGroup(int position) {
        if (isHeaderShown() && position == 0) return header;
        position -= getHeaderCount();
        if (isPlaceholderShown() && position == 0) return placeholder;
        position -= getPlaceholderCount();
        if (position == children.size()) {
            if (isFooterShown()) {
                return footer;
            } else {
                throw new IndexOutOfBoundsException("Wanted group at position " + position +
                        " but there are only " + getGroupCount() + " groups");
            }
        } else {
            return children.get(position);
        }
    }

    @Override
    public int getGroupCount() {
        return getHeaderCount() + getFooterCount() + getPlaceholderCount() + children.size();
    }

    @Override
    public int getPosition(@NonNull Group group) {
        int count = 0;
        if (isHeaderShown()) {
            if (group == header) return count;
        }
        count += getHeaderCount();
        if (isPlaceholderShown()) {
            if (group == placeholder) return count;
        }
        count += getPlaceholderCount();

        int index = children.indexOf(group);
        if (index >= 0) return count + index;
        count += children.size();

        if (isFooterShown()) {
            if (footer == group) {
                return count;
            }
        }

        return -1;
    }

    private boolean isHeaderShown() {
        return getHeaderCount() > 0;
    }

    private boolean isFooterShown() {
        return getFooterCount() > 0;
    }

    private boolean isPlaceholderShown() {
        return getPlaceholderCount() > 0;
    }

    public void setHeader(@NonNull Group header) {
        //noinspection ConstantConditions
        if (header == null)
            throw new NullPointerException("Header can't be null.  Please use removeHeader() instead!");
        if (this.header != null) {
            this.header.unregisterGroupDataObserver(this);
        }

        Group previousHeader = this.header;
        this.header = header;
        header.registerGroupDataObserver(this);
        notifyHeaderItemsChanged(previousHeader, header);
    }

    public void removeHeader() {
        if (this.header == null) {
            return;
        }

        Group header = this.header;
        this.header.unregisterGroupDataObserver(this);
        this.header = null;
        notifyHeaderItemsChanged(header, null);
    }

    private void notifyHeaderItemsChanged(
            @Nullable final Group previousHeader,
            @Nullable final Group newHeader
    ) {
        notifyHeaderOrFooterChanged(0, previousHeader, newHeader);
    }

    public void setFooter(@NonNull Group footer) {
        //noinspection ConstantConditions
        if (footer == null)
            throw new NullPointerException("Footer can't be null.  Please use removeFooter() instead!");
        if (this.footer != null) {
            this.footer.unregisterGroupDataObserver(this);
        }

        Group previousFooter = this.footer;
        this.footer = footer;
        footer.registerGroupDataObserver(this);
        notifyFooterItemsChanged(previousFooter, footer);
    }

    public void removeFooter() {
        if (this.footer == null) {
            return;
        }

        Group previousFooter = this.footer;
        this.footer.unregisterGroupDataObserver(this);
        this.footer = null;
        notifyFooterItemsChanged(previousFooter, null);
    }

    private void notifyFooterItemsChanged(
            @Nullable Group previousFooter,
            @Nullable Group newFooter
    ) {
        notifyHeaderOrFooterChanged(getItemCountWithoutFooter(), previousFooter, newFooter);
    }

    private void notifyHeaderOrFooterChanged(
            final int positionStart,
            @Nullable final Group prevGroup,
            @Nullable final Group newGroup
    ) {
        final int previousHeaderItemCount = prevGroup == null ? 0 : prevGroup.getItemCount();
        final int newHeaderItemCount = newGroup == null ? 0 : newGroup.getItemCount();

        if (previousHeaderItemCount == 0 && newHeaderItemCount == 0) return;
        if (previousHeaderItemCount == 0) {
            notifyItemRangeInserted(positionStart, newHeaderItemCount);
            return;
        } else if (newHeaderItemCount == 0) {
            notifyItemRangeRemoved(positionStart, previousHeaderItemCount);
            return;
        }

        DiffUtil.Callback callback = new DiffCallback(prevGroup, newGroup);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback, true);
        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position + positionStart, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position + positionStart, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition + positionStart, toPosition + positionStart);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                notifyItemRangeChanged(position + positionStart, count, payload);
            }
        });
    }

    public void setHideWhenEmpty(boolean hide) {
        if (hideWhenEmpty == hide) return;
        hideWhenEmpty = hide;
        refreshEmptyState();
    }

    @Override
    public void onItemInserted(@NonNull Group group, int position) {
        super.onItemInserted(group, position);
        refreshEmptyState();
    }

    @Override
    public void onItemRemoved(@NonNull Group group, int position) {
        super.onItemRemoved(group, position);
        refreshEmptyState();
    }

    @Override
    public void onItemRangeInserted(@NonNull Group group, int positionStart, int itemCount) {
        super.onItemRangeInserted(group, positionStart, itemCount);
        refreshEmptyState();
    }

    @Override
    public void onItemRangeRemoved(@NonNull Group group, int positionStart, int itemCount) {
        super.onItemRangeRemoved(group, positionStart, itemCount);
        refreshEmptyState();
    }

    private int getPlaceholderItemCount() {
        if (isPlaceholderVisible && placeholder != null) {
            return placeholder.getItemCount();
        }
        return 0;
    }
}
