package cn.jason31416.planetlib.map;

import cn.jason31416.planetlib.hook.BlueMapHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

import java.util.HashMap;

public class MapPOIMarker implements MapMarker {
    public String id, label, description, iconAddress=null;
    public int iconAnchorX=0, iconAnchorY=0;
    public SimpleLocation location;
    public void renderBlueMap(MapMarkerSet markerSet){
        if(!BlueMapHook.enabled) return;
        var markers = BlueMapHook.markers.get(markerSet);
        if(markers==null) markers = new HashMap<>();
        if(!markers.containsKey(location.world())) markers.put(location.world(), MarkerSet.builder().label(markerSet.name).build());
        POIMarker.Builder builder = POIMarker.builder()
                .position(location.x(), location.y(), location.z())
                .label(label)
                .detail(description);
        if(iconAddress!=null) builder.icon(iconAddress, iconAnchorX, iconAnchorY);
        markers.get(location.world()).put(id, builder.build());
    }

    public static MapPOIMarker.Builder builder(String id) {
        return new MapPOIMarker.Builder().id(id);
    }

    public static class Builder {
        private String id, label, description, iconAddress=null;
        private int iconAnchorX=0, iconAnchorY=0;
        private SimpleLocation location;
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        public Builder label(String label) {
            this.label = label;
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder iconAddress(String iconAddress) {
            this.iconAddress = iconAddress;
            return this;
        }
        public Builder iconAnchorX(int iconAnchorX) {
            this.iconAnchorX = iconAnchorX;
            return this;
        }
        public Builder iconAnchorY(int iconAnchorY) {
            this.iconAnchorY = iconAnchorY;
            return this;
        }
        public Builder location(SimpleLocation location) {
            this.location = location;
            return this;
        }
        public MapPOIMarker build() {
            MapPOIMarker marker = new MapPOIMarker();
            marker.id = id;
            marker.label = label;
            marker.description = description;
            marker.iconAddress = iconAddress;
            marker.iconAnchorX = iconAnchorX;
            marker.iconAnchorY = iconAnchorY;
            marker.location = location;
            return marker;
        }
    }
}
