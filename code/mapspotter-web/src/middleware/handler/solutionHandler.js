const solutionHandler = {
    genMapStyleLayer: (layer) => {
        let styleLayer = Object.assign({}, {
            id: layer.id,
            type: layer.type,
            source: layer.source,
            'source-layer': layer['source-layer'],
            layout: layer.layout,
            paint: layer.paint,
            maxzoom: layer.maxzoom,
            minzoom: layer.minzoom,
            interactive: true
        })
        if (layer.filter && layer.filter.length > 0) {
            styleLayer.filter = layer.filter
        }
        return styleLayer
    }
}

export default solutionHandler