package in.srain.cube.views.list;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class LazyViewHolderCreator<T> implements ViewHolderCreator<T> {

    private final Constructor<?> mConstructor;
    private Object[] mInstanceObjects;

    private LazyViewHolderCreator(Constructor<?> constructor, Object[] instanceObjects) {
        mConstructor = constructor;
        mInstanceObjects = instanceObjects;
    }

    public static <ItemDataType> ViewHolderCreator<ItemDataType> create(final Object enclosingInstance, final Class<?> cls, final Object... args) {
        if (cls == null) {
            throw new IllegalArgumentException("ViewHolderClass is null.");
        }

        // top class
        boolean isEnclosingInstanceClass = false;
        if (cls.getEnclosingClass() != null && !Modifier.isStatic(cls.getModifiers())) {
            isEnclosingInstanceClass = true;
        }

        // inner instance class should pass enclosing class, so +1
        int argsLen = isEnclosingInstanceClass ? args.length + 1 : args.length;

        final Object[] instanceObjects = new Object[argsLen];

        int copyStart = 0;
        // if it is inner instance class, first argument should be the enclosing class instance
        if (isEnclosingInstanceClass) {
            instanceObjects[0] = enclosingInstance;
            copyStart = 1;
        }

        // has copy construction parameters
        if (args.length > 0) {
            System.arraycopy(args, 0, instanceObjects, copyStart, args.length);
        }

        // fill the types
        final Class[] parameterTypes = new Class[argsLen];
        for (int i = 0; i < instanceObjects.length; i++) {
            parameterTypes[i] = instanceObjects[i].getClass();
        }

        Constructor<?> constructor = null;
        try {
            constructor = cls.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (constructor == null) {
            throw new IllegalArgumentException("ViewHolderClass can not be initiated");
        }

        ViewHolderCreator lazyCreator = new LazyViewHolderCreator(constructor, instanceObjects);
        return lazyCreator;
    }

    @Override
    public ViewHolderBase<T> createViewHolder(int position) {
        Object object = null;
        try {
            boolean isAccessible = mConstructor.isAccessible();
            if (!isAccessible) {
                mConstructor.setAccessible(true);
            }
            object = mConstructor.newInstance(mInstanceObjects);
            if (!isAccessible) {
                mConstructor.setAccessible(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (object == null || !(object instanceof ViewHolderBase)) {
            throw new IllegalArgumentException("ViewHolderClass can not be initiated");
        }
        return (ViewHolderBase<T>) object;
    }
}
