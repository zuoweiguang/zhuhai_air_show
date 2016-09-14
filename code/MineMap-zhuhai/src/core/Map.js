export default class Map extends mapboxgl.Map {
    constructor(container, style, center = [116.46, 39.92], zoom = 15, minZoom = 3, maxZoom = 17, bearing = 0, pitch = 60, attributionControl = false) {
        mapboxgl.accessToken = 'pk.eyJ1IjoiZmFuZ2xhbmsiLCJhIjoiY2lpcjc1YzQxMDA5NHZra3NpaDAyODB4eSJ9.z6uZHccXvtyVqA5zmalfGg'
        let options = {
            container: container,
            style: style,
            center: center,
            zoom: zoom,
            maxZoom: 17 || maxZoom,
            minZoom: minZoom,
            attributionControl: attributionControl,
            bearing: bearing,
            pitch: pitch
        }
        super(options)
    }
}