import {OPERATE_SOLUTION_INFO,OPERATE_SOLUTION_NAME_EDIT,OPERATE_SOLUTION_LAYERZINDEX_EDIT,OPERATE_SOLUTION_LAYER_ADD,OPERATE_SOLUTION_LAYER_SELECTED,OPERATE_SOLUTION_ALLLAYER_SELECTED,OPERATE_SOLUTION_LAYER_DEL,OPERATE_SOLUTION_LAYER_EDIT,OPERATE_SOLUTION_LAYERNAME_EDIT,OPERATE_SOLUTION_LAYER_VISIBLE,OPERATE_SOLUTION_LAYER_DUPLICATE,OPERATE_LAYER_STYLE_INFO,OPERATE_LAYER_STYLE_UNSELECTED,OPERATE_SOLUTION_PROPERTY_MODIFY,OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY,OPERATE_SOLUTION_LAYER_PAINT_MODIFY,OPERATE_SOLUTION_LAYER_TYPE_MODIFY,OPERATE_SOLUTION_LAYER_FILTER_MODIFY,OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY,OPERATE_LAYER_SOURCE_ATTR_LIST,WAREHOUSE_SOURCES,WAREHOUSE_SOURCE_LAYERS,WAREHOUSE_SOURCES_CONFIRM,WAREHOUSE_SOURCE_LAYER_CHANGE,DISPLAY_INTERFACE_ROUTE} from '../../constants/OperateActionTypes'
import { combineReducers } from 'redux'
import {extendAll} from '../../utils/ObjectUtil'

function solutionReducer(state = {layers: []}, action) {
    switch (action.type) {
        case OPERATE_SOLUTION_INFO:
            return action.ready ? action.fetchData.solution : {}
        case OPERATE_SOLUTION_NAME_EDIT:
            return Object.assign({}, state, action.data)
        case OPERATE_SOLUTION_LAYER_ADD:
            let addlayers = [
                action.layer,
                ...state.layers
            ]
            return Object.assign({}, state, {layers: addlayers})
        case OPERATE_SOLUTION_LAYER_DEL:
            let dellayers = state.layers.filter(layer =>
                layer.id !== action.id
            )
            return Object.assign({}, state, {layers: dellayers})
        case OPERATE_SOLUTION_LAYER_SELECTED:
            let selectedlayers = state.layers.map(layer =>
                layer.id === action.id && action.selected ?
                    Object.assign({}, layer, {selected: true}) :
                    Object.assign({}, layer, {selected: false})
            )
            return Object.assign({}, state, {layers: selectedlayers})
        case OPERATE_SOLUTION_ALLLAYER_SELECTED:
            let allSelectedlayers = state.layers.map(layer => Object.assign({}, layer, {selected: action.selected}))
            return Object.assign({}, state, {layers: allSelectedlayers})
        case OPERATE_SOLUTION_LAYER_EDIT:
            let editlayers = state.layers.map(layer =>
                layer.id === action.id ?
                    Object.assign({}, layer, {id: action.id}) :
                    layer
            )
            return Object.assign({}, state, {layers: editlayers})
        case OPERATE_SOLUTION_LAYER_VISIBLE:
            let visiblelayers = state.layers.map(layer =>
                layer.id === action.id ?
                    Object.assign({}, layer, {layout: Object.assign({}, layer.layout, {visibility: action.visibility})}) :
                    layer
            )
            return Object.assign({}, state, {layers: visiblelayers})
        case OPERATE_SOLUTION_LAYERNAME_EDIT:
            let editnamelayers = state.layers.map(layer =>
                layer.id === action.data.id ?
                    Object.assign({}, layer, action.data) :
                    layer
            )
            return Object.assign({}, state, {layers: editnamelayers})
        case OPERATE_SOLUTION_PROPERTY_MODIFY:
            return Object.assign({}, state, action.data)
        case OPERATE_SOLUTION_LAYERZINDEX_EDIT:
            let editZIndexlayers = state.layers.map(layer => {
                let datas = action.datas
                let editZindex = layer.zindex
                datas.map(data => {
                    if (layer.id == data.id) {
                        editZindex = data.zindex
                    }
                })
                return Object.assign({}, layer, {zindex: editZindex})
            })
            return Object.assign({}, state, {layers: editZIndexlayers})
        case OPERATE_SOLUTION_LAYER_DUPLICATE:
            let duplayers = []
            state.layers.map(layer => {
                if (layer.zindex > action.newLayer.zindex) {
                    duplayers.push(layer)
                }
            })
            duplayers.push(Object.assign({}, action.newLayer))
            state.layers.map(layer => {
                if (layer.zindex <= action.newLayer.zindex) {
                    duplayers.push(layer)
                }
            })
            return Object.assign({}, state, {layers: duplayers})
        case OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY:
            let modifyLayoutLayers = state.layers.map((layer) => {
                    if (layer.id === action.layer.id) {
                        let newLayer = Object.assign({}, action.layer)
                        newLayer.layout[action.k] = action.v
                        return newLayer
                    }
                    return layer
                }
            )
            return Object.assign({}, state, {layers: modifyLayoutLayers})
        case OPERATE_SOLUTION_LAYER_PAINT_MODIFY:
            let modifyPaintLayers = state.layers.map((layer) => {
                    if (layer.id === action.layer.id) {
                        let newLayer = Object.assign({}, action.layer)
                        newLayer.paint[action.k] = action.v
                        return newLayer
                    }
                    return layer
                }
            )
            return Object.assign({}, state, {layers: modifyPaintLayers})
        case OPERATE_SOLUTION_LAYER_TYPE_MODIFY:
            let modifyTypeLayers = state.layers.map((layer) => {
                    if (layer.id === action.oldlayer.id) {
                        return Object.assign({}, action.newlayer, {selected: true})
                    }
                    return layer
                }
            )
            return Object.assign({}, state, {layers: modifyTypeLayers})
        case OPERATE_SOLUTION_LAYER_FILTER_MODIFY:
            let modifyFilterLayers = state.layers.map((layer) => {
                    if (layer.id === action.layer.id) {
                        return Object.assign({}, action.layer, {filter: action.newFilter})
                    }
                    return layer
                }
            )
            return Object.assign({}, state, {layers: modifyFilterLayers})
        case OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY:
            let modifyZoomRangeLayers = state.layers.map((layer) => {
                    if (layer.id === action.layer.id) {
                        return Object.assign({}, action.layer, {minzoom: action.minzoom, maxzoom: action.maxzoom})
                    }
                    return layer
                }
            )
            return Object.assign({}, state, {layers: modifyZoomRangeLayers})
        default:
            return state
    }
}

function layerStyleReducer(state = {}, action) {
    switch (action.type) {
        case OPERATE_LAYER_STYLE_INFO:
            return extendAll({}, action.layer)
        case OPERATE_SOLUTION_LAYERNAME_EDIT:
            return Object.assign({}, state, action.data)
        case OPERATE_SOLUTION_LAYER_VISIBLE:
            return state.id === action.id ?
                Object.assign({}, state, {layout: Object.assign({}, state.layout, {visibility: action.visibility})}) :
                state
        case OPERATE_LAYER_STYLE_UNSELECTED:
            return {}
        case OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY:
            let newLayer = Object.assign({}, action.layer)
            newLayer.layout[action.k] = action.v
            return newLayer
        case OPERATE_SOLUTION_LAYER_PAINT_MODIFY:
            let newLayer1 = Object.assign({}, action.layer)
            newLayer1.paint[action.k] = action.v
            return newLayer1
        case OPERATE_SOLUTION_LAYER_TYPE_MODIFY:
            return Object.assign({}, action.newlayer)
        case OPERATE_SOLUTION_LAYER_FILTER_MODIFY:
            return Object.assign({}, action.layer, {filter: action.newFilter})
        case OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY:
            return Object.assign({}, action.layer, {minzoom: action.minzoom, maxzoom: action.maxzoom})
        default:
            return state
    }
}

function layerSourceAttrsReducer(state = [], action) {
    switch (action.type) {
        case OPERATE_LAYER_SOURCE_ATTR_LIST:
            let attrs = action.ready ? action.fetchData.attrs : []
            return attrs || []
        default:
            return state
    }
}

function warehouseLayerReducer(state = [], action) {
    switch (action.type) {
        case WAREHOUSE_SOURCES:
            return action.ready ? action.fetchData.dataSources : []
        case WAREHOUSE_SOURCES_CONFIRM:
            return state
        case WAREHOUSE_SOURCE_LAYER_CHANGE:
            return state.map((source)=> {
                let changeItems = source["source-layers"].map((layer)=> {
                    return action.layerId === layer.id ?
                        Object.assign({}, layer, {checked: !layer.checked}) : layer
                })
                return Object.assign({}, source, {"source-layers": changeItems})
            })
        case WAREHOUSE_SOURCE_LAYERS:
            return state.map((source)=> {
                if (source.id === action.id) {
                    return Object.assign({}, source, {"source-layers": action.fetchData ? action.fetchData["source-layers"] : []})
                }
                return source
            })
        default:
            return state
    }
}

function displayInterfaceReducer(state = "operate", action) {
    switch (action.type) {
        case DISPLAY_INTERFACE_ROUTE:
            return action.route
        default:
            return state
    }
}

const operateReducer = combineReducers({
    solution: solutionReducer,
    layerStyle: layerStyleReducer,
    layerSourceAttrs: layerSourceAttrsReducer,
    warehouse: warehouseLayerReducer,
    displayInterface: displayInterfaceReducer
})

export default operateReducer