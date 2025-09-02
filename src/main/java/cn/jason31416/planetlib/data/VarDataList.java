package cn.jason31416.planetlib.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class VarDataList<T> implements DataList<T> {
    @FunctionalInterface
    public interface Provider<T>{
        T get();
    }
    private final String name;
    private final Provider<Collection<T>> provider;
    private final Class<T> dataClass;
    public VarDataList(String name, Class<T> dataClass, Provider<Collection<T>> provider) {
        this.name = name;
        this.provider = provider;
        this.dataClass = dataClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<T> getAllData() {
        return new ArrayList<>(provider.get());
    }

    @Override
    public boolean serialize(Object data, IDataItem dataItem) {
        for(Field field : data.getClass().getDeclaredFields()){
            DataEntry dataEntry = field.getAnnotation(DataEntry.class);
            if(dataEntry!= null){
                try {
                    field.setAccessible(true);
                    if(dataEntry.asUUID()){
                        dataItem.setUUID((UUID) field.get(data));
                    }else{
                        dataItem.put(dataEntry.name().isEmpty()? field.getName() : dataEntry.name(), field.get(data));
                    }
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public T deserialize(IDataItem dataItem) {
        try {
            T data = dataClass.getConstructor().newInstance();
            for (Field field : data.getClass().getDeclaredFields()) {
                DataEntry dataEntry = field.getAnnotation(DataEntry.class);
                if (dataEntry!= null) {
                    field.setAccessible(true);
                    if (dataEntry.asUUID()) {
                        field.set(data, dataItem.getUUID());
                    } else {
                        field.set(data, dataItem.get(dataEntry.name().isEmpty()? field.getName() : dataEntry.name()));
                    }
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<String> getTableKeys(){
        return null;
    }
}
