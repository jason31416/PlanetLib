package cn.jason31416.planetlib.map;

import cn.jason31416.planetlib.hook.BlueMapHook;
import de.bluecolored.bluemap.api.markers.MarkerSet;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MapMarkerSet {
    public String id, name;
    public List<MapMarker> markers=new ArrayList<>();

    public MapMarkerSet(String id, String name){
        this.id=id;
        this.name=name;
    }

    public void register(){
        BlueMapHook.markers.put(this, new HashMap<>());
    }

    public void render(){
        if(BlueMapHook.enabled){
            if(BlueMapHook.markers.containsKey(this)) for(MarkerSet i: BlueMapHook.markers.get(this).values()){
                i.getMarkers().clear();
            }
            for(MapMarker i: markers){
                i.renderBlueMap(this);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MapMarkerSet m) return m.id.equals(id);
        return false;
    }
    @Override
    public int hashCode(){
        return id.hashCode();
    }
}
