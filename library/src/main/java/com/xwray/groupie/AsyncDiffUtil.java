package com.xwray.groupie;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import java.util.List;

/**
 * A wrapper around {@link DiffUtil} that calculates diff in a background thread
 */
class AsyncDiffUtil {

    interface Callback extends ListUpdateCallback {
        /**
         * Called on the main thread before DiffUtil dispatches the result
         */
        @MainThread
        void onDispatchAsyncResult(List<Group> mergedGroups);
    }

    final Callback asyncDiffUtilCallback;
    private int maxScheduledGeneration;

    AsyncDiffUtil(@NonNull Callback callback) {
        this.asyncDiffUtilCallback = callback;
    }

    int getMaxScheduledGeneration() {
        return maxScheduledGeneration;
    }

    void calculateDiff(@NonNull List<? extends Group> oldGroups,
                       @NonNull List<? extends Group> newGroups,
                       @Nullable final OnAsyncUpdateListener onAsyncUpdateListener,
                       boolean detectMoves) {
        // incrementing generation means any currently-running diffs are discarded when they finish
        final int runGeneration = ++maxScheduledGeneration;
        final DiffCallback diffUtilCallback = new DiffCallback(oldGroups, newGroups);
        new DiffTask(this, diffUtilCallback, runGeneration, detectMoves, onAsyncUpdateListener).execute();
    }
}