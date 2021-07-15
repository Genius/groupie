package com.xwray.groupie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DiffCallback extends DiffUtil.Callback {
    private final int oldBodyItemCount;
    private final int newBodyItemCount;
    private final List<Item> oldList;
    private final List<Item> newList;

    private final Map<Integer, Integer> changeMap = new HashMap<>();

    DiffCallback(Collection<? extends Group> oldGroups, Collection<? extends Group> newGroups) {
        this.oldBodyItemCount = GroupUtils.getItemCount(oldGroups);
        this.newBodyItemCount = GroupUtils.getItemCount(newGroups);
        this.oldList = flatToItem(oldGroups);
        this.newList = flatToItem(newGroups);
    }

    @Override
    public int getOldListSize() {
        return oldBodyItemCount;
    }

    @Override
    public int getNewListSize() {
        return newBodyItemCount;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Item oldItem = GroupUtils.getItem(oldList, oldItemPosition);
        Item newItem = GroupUtils.getItem(newList, newItemPosition);
        return newItem.isSameAs(oldItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Item oldItem = GroupUtils.getItem(oldList, oldItemPosition);
        Item newItem = GroupUtils.getItem(newList, newItemPosition);
        boolean sameContent = newItem.hasSameContentAs(oldItem);

        //false means changed
        if (sameContent) {
            changeMap.put(newItemPosition, oldItemPosition);
        }
        return sameContent;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Item oldItem = GroupUtils.getItem(oldList, oldItemPosition);
        Item newItem = GroupUtils.getItem(newList, newItemPosition);
        return oldItem.getChangePayload(newItem);
    }

    @NonNull
    public List<Group> mergeGroups() {
        ArrayList<Group> resultList = new ArrayList<>();

        for (int i = 0; i < newList.size(); i++) {
            Integer position = changeMap.get(i);

            if (position == null) {
                resultList.add(newList.get(i));
            } else {
                resultList.add(oldList.get(position));
            }
        }
        return resultList;
    }

    private List<Item> flatToItem(Collection<? extends Group> groups) {
        List<Item> result = new ArrayList<>();
        for (Group group : groups) {
            final int size = group.getItemCount();
            for (int i = 0; i < size; i++) {
                result.add(group.getItem(i));
            }
        }
        return result;
    }
}