const warehouseHandler = {
    genSolutionLayer: (solution, source, sourceLayer, defaultStyle, zindex) => {
        return Object.assign({}, {
            name: sourceLayer.name,
            datatype: sourceLayer.type,
            type: sourceLayer.type,
            sourceID: source.id,
            source: source.source,
            layout: Object.assign({}, defaultStyle.layout, {visibility: 'visible'}),
            paint: Object.assign({}, defaultStyle.paint),
            filter: [],
            desc: source.desc || "",
            'source-layer': sourceLayer.id,
            maxzoom: source.maxzoom || 19,
            minzoom: source.minzoom || 3,
            sourcemaxzoom: source.maxzoom || 22,
            sourceminzoom: source.minzoom || 3,
            interactive: true,
            soluID: solution.id,
            zindex: zindex || 1
        })
    },
    genSolutionBackGroundLayer: (solutionId, defaultStyle) => {
        return Object.assign({}, {
            name: '背景',
            datatype: 'background',
            type: 'background',
            sourceID: '',
            source: '',
            layout: Object.assign({}, defaultStyle.layout, {visibility: 'visible'}),
            paint: Object.assign({}, defaultStyle.paint),
            filter: [],
            desc: '背景',
            'source-layer': '',
            maxzoom: 22,
            minzoom: 0,
            sourcemaxzoom: 22,
            sourceminzoom: 0,
            interactive: false,
            soluID: solutionId,
            zindex: 0
        })
    }
}

export default warehouseHandler