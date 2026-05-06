package me.brynview.navidrohim.jmws.common.syncing.rules;

public interface CommonRule
{
    String getRegistryKey();
    final class InputTypes<T>
    {
        public static final InputTypes<String> STRING = new InputTypes<>(String.class);
        public static final InputTypes<Boolean> BOOLEAN = new InputTypes<>(Boolean.class);
        public static final InputTypes<Integer> INTEGER = new InputTypes<>(Integer.class);

        private final Class<T> clazz;
        private InputTypes(Class<T> o)
        {
            this.clazz = o;
        }

        public Class<T> getClazz()
        {
            return this.clazz;
        }

        public T cast(Object obj)
        {
            return clazz.cast(obj);
        }

        public String toString()
        {
            return clazz.getSimpleName().toUpperCase();
        }
    }
}
