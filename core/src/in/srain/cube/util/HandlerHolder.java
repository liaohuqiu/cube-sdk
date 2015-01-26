package in.srain.cube.util;

/**
 * A single linked list to wrap <T>
 */
public class HandlerHolder<T> {

    private T mHandler;
    private HandlerHolder mNext;

    private boolean contains(T handler) {
        return mHandler != null && mHandler == handler;
    }

    private HandlerHolder() {

    }

    public boolean hasHandler() {
        return mHandler != null;
    }

    private T getHandler() {
        return mHandler;
    }

    public static <T> void addHandler(HandlerHolder head, T handler) {

        if (null == handler) {
            return;
        }
        if (head == null) {
            return;
        }
        if (null == head.mHandler) {
            head.mHandler = handler;
            return;
        }

        HandlerHolder current = head;
        for (; ; current = current.mNext) {

            // duplicated
            if (current.contains(handler)) {
                return;
            }
            if (current.mNext == null) {
                break;
            }
        }

        HandlerHolder newHolder = new HandlerHolder();
        newHolder.mHandler = handler;
        current.mNext = newHolder;
    }

    public static HandlerHolder create() {
        return new HandlerHolder();
    }

    public static <T> HandlerHolder removeHandler(HandlerHolder head, T handler) {
        if (head == null || handler == null || null == head.mHandler) {
            return head;
        }

        HandlerHolder current = head;
        HandlerHolder pre = null;
        do {

            // delete current: link pre to next, unlink next from current;
            // pre will no change, current move to next element;
            if (current.contains(handler)) {

                // current is head
                if (pre == null) {

                    head = current.mNext;
                    current.mNext = null;

                    current = head;
                } else {

                    pre.mNext = current.mNext;
                    current.mNext = null;
                    current = pre.mNext;
                }
            } else {
                pre = current;
                current = current.mNext;
            }

        } while (current != null);

        if (head == null) {
            head = new HandlerHolder();
        }
        return head;
    }
}
