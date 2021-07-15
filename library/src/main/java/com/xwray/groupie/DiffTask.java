package com.xwray.groupie;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

/**
 * An async task implementation that runs {@link DiffUtil#calculateDiff(DiffUtil.Callback)}
 * in a background thread.
 * <p>This task is executed via {@link AsyncDiffUtil#calculateDiff(List, List, OnAsyncUpdateListener, boolean)}.
 */
class DiffTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {
    @NonNull
    private final DiffCallback diffCallback;
    private final WeakReference<AsyncDiffUtil> asyncListDiffer;
    private final int runGeneration;
    private final boolean detectMoves;
    @Nullable
    private WeakReference<OnAsyncUpdateListener> onAsyncUpdateListener;
    private Exception backgroundException = null;

    DiffTask(@NonNull AsyncDiffUtil asyncDiffUtil,
             @NonNull DiffCallback callback,
             int runGeneration,
             boolean detectMoves,
             @Nullable OnAsyncUpdateListener onAsyncUpdateListener) {
        this.diffCallback = callback;
        this.asyncListDiffer = new WeakReference<>(asyncDiffUtil);
        this.runGeneration = runGeneration;
        this.detectMoves = detectMoves;
        if (onAsyncUpdateListener != null) {
            this.onAsyncUpdateListener = new WeakReference<>(onAsyncUpdateListener);
        }
    }

    @Override
    @Nullable
    protected DiffUtil.DiffResult doInBackground(Void... voids) {
        try {
            return DiffUtil.calculateDiff(diffCallback, detectMoves);
        } catch (Exception e) {
            backgroundException = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(@Nullable DiffUtil.DiffResult diffResult) {
        if (backgroundException != null) {
            throw new RuntimeException(backgroundException);
        }
        AsyncDiffUtil async = asyncListDiffer.get();

        if (!shouldDispatchResult(diffResult, async)) {
            return;
        }

        AsyncDiffUtil.Callback asyncDiffUtilCallback = async.asyncDiffUtilCallback;
        asyncDiffUtilCallback.onDispatchAsyncResult(diffCallback.mergeGroups());

        diffResult.dispatchUpdatesTo(asyncDiffUtilCallback);
        if (onAsyncUpdateListener != null && onAsyncUpdateListener.get() != null) {
            onAsyncUpdateListener.get().onUpdateComplete();
        }
    }

    private boolean shouldDispatchResult(@Nullable DiffUtil.DiffResult diffResult, AsyncDiffUtil async) {
        return diffResult != null && async != null && runGeneration == async.getMaxScheduledGeneration();
    }
}