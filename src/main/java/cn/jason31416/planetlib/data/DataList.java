package cn.jason31416.planetlib.data;

import java.util.List;

public interface DataList<T> {
    String getName();
    List<T> getAllData();
    boolean serialize(Object data, IDataItem dataItem);
    T deserialize(IDataItem dataItem);
}
