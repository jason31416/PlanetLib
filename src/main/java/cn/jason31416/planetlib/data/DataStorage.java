package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.util.general.Pair;
import lombok.Getter;

import java.io.*;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.*;

public class DataStorage {
    public record QueryResult(ResultSet resultSet, Connection conn) implements AutoCloseable{
        @Override
        public void close() throws Exception {
            conn.close();
        }
    }
    public static final List<Class<?>> supportedPrimitives = List.of(Boolean.class, boolean.class, String.class, int.class, double.class, float.class, short.class, long.class, Integer.class, Double.class, Float.class, Short.class, Long.class);

    public final Map<String, DataTable> tableRecords = new HashMap<>();

    @Getter
    private final HikariDataSource dataSource;

    public DataStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @SneakyThrows
    public void executeStatement(String sql, Object... args){
        try(var conn=getConnection()){
            PreparedStatement st=conn.prepareStatement(sql);
            for(int i=0;i<args.length;i++){
                st.setObject(i+1,args[i]);
            }
            st.execute();
        }
    }
    @SneakyThrows
    public QueryResult executeQuery(String sql, Object... args){
        var conn=getConnection();
        PreparedStatement st=conn.prepareStatement(sql);
        for(int i=0;i<args.length;i++){
            st.setObject(i+1,args[i]);
        }
        var rs = st.executeQuery();
        return new QueryResult(rs, conn);
    }
    @SneakyThrows @Nullable
    public <T> T getItem(String table, String key, String column, Class<T> type){
        var ret = getItem(table, key, column);
        return type.cast(ret);
    }
    @Nullable
    public Boolean getBoolean(String table, String key, String column){
        var ret = getItem(table, key, column, Integer.class);
        if(ret==null) return null;
        return ret==1;
    }
    @Nullable
    public Integer getInt(String table, String key, String column){
        return getItem(table, key, column, Integer.class);
    }
    @Nullable
    public Double getDouble(String table, String key, String column){
        return getItem(table, key, column, Double.class);
    }
    @Nullable
    public String getString(String table, String key, String column){
        return getItem(table, key, column, String.class);
    }

    @SneakyThrows @Nullable
    public Object getItem(String table, String key, String column){
        try(var qr=executeQuery("SELECT "+column+" FROM "+table+" WHERE "+tableRecords.get(table).primaryKey()+" = '"+key+"'")) {
            if (qr.resultSet().next()) {
                if(qr.resultSet().getObject(column) == null) return null;
                if(qr.resultSet().getMetaData().getColumnType(1)== Types.BLOB){
                    return new ObjectInputStream(new ByteArrayInputStream(qr.resultSet().getBytes(column))).readObject();
                }
                return qr.resultSet().getObject(column);
            }
            return null;
        }
    }
    @SneakyThrows
    public void updateItem(String table, String key, String column, Object value){
        Object obj;
        if(supportedPrimitives.contains(value.getClass())){
            obj = value;
        }else{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            obj = new SerialBlob(baos.toByteArray());
        }
        executeStatement("UPDATE "+table+" SET "+column+" = ? WHERE "+tableRecords.get(table).primaryKey()+" = ?", obj, key);
    }
    @SneakyThrows
    public void insertEntry(String table, Map<String, Object> item){
        List<Object> values = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for(String i: item.keySet()){
            if(supportedPrimitives.contains(item.get(i).getClass())) values.add(item.get(i));
            else{
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                ObjectOutputStream oos=new ObjectOutputStream(baos);
                oos.writeObject(item.get(i));
                oos.close();
                values.add(baos.toByteArray());
            }
            keys.add(i);
        }
        StringBuilder brk = new StringBuilder();
        brk.append("(");
        for(int i=0;i<keys.size()-1;i++){
            brk.append("?, ");
        }
        brk.append("?)");
        StringBuilder brk2 = new StringBuilder();
        brk2.append("(");
        for(int i=0;i<keys.size()-1;i++){
            brk2.append(keys.get(i)).append(", ");
        }
        brk2.append(keys.getLast()).append(")");
        try(var conn=getConnection()){
            PreparedStatement st=conn.prepareStatement("REPLACE INTO "+table+" "+brk2.toString()+" VALUES "+brk.toString());
            for(int i=0;i<values.size();i++){
                if(values.get(i) instanceof byte[] barray){
                    st.setBinaryStream(i+1, new ByteArrayInputStream(barray));
                }else {
                    st.setObject(i + 1, values.get(i));
                }
            }
            st.execute();
        }
    }
    private String getTypeName(Class<?> type){
        return switch (type.getSimpleName().toLowerCase(Locale.ROOT)){
            case "boolean" -> "BOOL";
            case "string" -> "VARCHAR(255)";
            case "integer", "int", "short" -> "INT";
            case "long" -> "BIGINT";
            case "float", "double" -> "REAL";
            default -> "BLOB";
        };
    }
    @SneakyThrows
    private void constructTable(DataTable table){
        try(var conn=getConnection()){
            DatabaseMetaData metaData = conn.getMetaData();
            var rs = metaData.getTables(null, null, table.tableName(), new String[] {"TABLE"});
            if(rs.next()){
                var rsnew = metaData.getColumns(null, null, table.tableName(), null);
                List<String> existingColumns = new ArrayList<>();
                while(rsnew.next()){
                    existingColumns.add(rsnew.getString("COLUMN_NAME"));
                }
                for(var column: table.columns()){
                    if(!existingColumns.contains(column.first())){
                        conn.prepareStatement("ALTER TABLE "+table.tableName()+" ADD "+column.first()+" "+getTypeName(column.second())+";")
                                .executeUpdate();
                    }
                }
            }else{
                StringBuilder sb=new StringBuilder();
                sb.append("CREATE TABLE ").append(table.tableName()).append(" (");
                boolean first=true;
                for(var column: table.columns()){
                    if(first){
                        first=false;
                    }else{
                        sb.append(", ");
                    }
                    sb.append(column.first()).append(" ");
                    if(supportedPrimitives.contains(column.second())){
                        sb.append(getTypeName(column.second()));
                    }else{
                        sb.append("BLOB");
                    }
                    if(table.primaryKey().equals(column.first())){
                        sb.append(" PRIMARY KEY");
                    }
                }
                sb.append(")");
                PreparedStatement st=conn.prepareStatement(sb.toString());
                st.execute();
            }
        }
    }

    public void registerTable(String name, String primaryKey, List<Pair<String, Class<?>>> columns) {
        var rec=new DataTable(name, primaryKey, this, columns);
        tableRecords.put(name, rec);
        constructTable(rec);
    }
}
