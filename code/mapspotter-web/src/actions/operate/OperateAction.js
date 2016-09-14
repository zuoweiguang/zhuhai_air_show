import {OPERATE_SOLUTION_INFO,OPERATE_SOLUTION_NAME_EDIT,OPERATE_SOLUTION_LAYERZINDEX_EDIT,OPERATE_SOLUTION_LAYER_ADD,OPERATE_SOLUTION_LAYER_DEL,OPERATE_SOLUTION_LAYER_SELECTED,OPERATE_SOLUTION_ALLLAYER_SELECTED,OPERATE_SOLUTION_LAYER_EDIT,OPERATE_SOLUTION_LAYERNAME_EDIT,OPERATE_SOLUTION_LAYER_VISIBLE,OPERATE_SOLUTION_LAYER_DUPLICATE,OPERATE_LAYER_STYLE_INFO,OPERATE_LAYER_STYLE_UNSELECTED,OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY,OPERATE_SOLUTION_LAYER_PAINT_MODIFY,OPERATE_SOLUTION_LAYER_TYPE_MODIFY,OPERATE_SOLUTION_LAYER_FILTER_MODIFY,OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY,OPERATE_LAYER_SOURCE_ATTR_LIST,WAREHOUSE_SOURCES,WAREHOUSE_SOURCE_LAYERS,WAREHOUSE_SOURCES_CONFIRM,WAREHOUSE_SOURCE_LAYER_CHANGE,DISPLAY_INTERFACE_ROUTE} from '../../constants/OperateActionTypes'
import { CALL_API,CALL_JSON } from '../../constants/MiddlewareInfo'
import {OPERATE_SOLUTION_INFO_URL,OPERATE_LAYER_STYLE_INFO_URL,WAREHOUSE_SOURCES_URL,WAREHOUSE_SOURCE_LAYERS_URL} from '../../config/connectConfig'
import {MAP_LAYER_REORDER} from '../../constants/OperateActionTypes'
import UserTool from '../../utils/UserTool'
import {APP_SERVICE_ROOT} from '../../config/appConfig'
import solutionService from '../../middleware/service/solutionService'

export function solutionInfoAction(id) {
    let url = APP_SERVICE_ROOT + 'solution/' + id
    return {type: OPERATE_SOLUTION_INFO, [CALL_JSON]: {url: url, params: {id: id}, type: 'GET'}}
}

export function solutionNameModifyAction(data) {
    return (dispatch, getState) => {
        solutionService.updateSolution(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_NAME_EDIT, data
        })
    }
}

export function solutionLayerAddAction(layer) {
    return {type: OPERATE_SOLUTION_LAYER_ADD, layer}
}

export function solutionLayerDelAction(id) {
    return (dispatch, getState) => {
        solutionService.delLayer(id).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYER_DEL, id
        })
    }
}

export function solutionLayerSelectedAction(id, selected) {
    return {type: OPERATE_SOLUTION_LAYER_SELECTED, id, selected}
}

export function solutionAllLayerSelectedAction(selected) {
    return {type: OPERATE_SOLUTION_ALLLAYER_SELECTED, selected}
}

export function solutionLayerEditAction(id) {
    return {type: OPERATE_SOLUTION_LAYER_EDIT, id}
}

export function layerStyleInfoAction(layer) {
    return (dispatch, getState) => {
        dispatch(solutionLayerSourceAttrListAction(layer.sourceID, layer['source-layer']))

        return dispatch({
            type: OPERATE_LAYER_STYLE_INFO, layer
        })
    }
}

export function layerStyleInfoUnselectedAction() {
    return {
        type: OPERATE_LAYER_STYLE_UNSELECTED
    }
}

export function solutionLayerEditNameAction(data) {
    return (dispatch, getState) => {
        solutionService.updateLayer(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYERNAME_EDIT, data
        })
    }
}

export function solutionLayerEditZIndexAction(datas) {
    return (dispatch, getState) => {
        datas.map(data => {
            solutionService.updateLayer(data).then(response =>
                response.json()
            ).then(res => {
                console.log(res)
            }).catch((e)=>
                console.log(e)
            )
        })

        let reorder = true
        setTimeout(function () {
            dispatch({type: MAP_LAYER_REORDER, reorder})
        }, 100)

        return dispatch({
            type: OPERATE_SOLUTION_LAYERZINDEX_EDIT, datas
        })
    }
}

export function solutionLayerVisibleAction(id, layout, visibility) {
    let _layout = Object.assign({}, layout, {visibility, visibility})
    return (dispatch, getState) => {
        solutionService.updateLayerProperty({id: id, layout: _layout}).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYER_VISIBLE, id, layout, visibility
        })
    }
}

export function solutionLayerDuplicateAction(srcLayer) {
    let newLayer = Object.assign({}, srcLayer)
    delete newLayer.id
    return (dispatch, getState) => {
        solutionService.addLayer(newLayer).then(response =>
            response.json()
        ).then(res => {
            if (res.errcode == 0 && res.data) {
                newLayer.id = res.data.id
                newLayer.selected = false
                let reorder = true
                setTimeout(function () {
                    dispatch({type: MAP_LAYER_REORDER, reorder})
                }, 100)
                return dispatch({
                    type: OPERATE_SOLUTION_LAYER_DUPLICATE, newLayer
                })
            } else {
                console.log(res)
            }
        }).catch((e)=>
            console.log(e)
        )
    }
}

export function solutionLayerLayoutModify(layer, k, v) {
    let data = Object.assign({}, {
        id: layer.id,
        layout: layer.layout
    })
    data.layout[k] = v
    return (dispatch, getState) => {
        solutionService.updateLayerProperty(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY, layer, k, v
        })
    }
}

export function solutionLayerPaintModify(layer, k, v) {
    let data = Object.assign({}, {
        id: layer.id,
        paint: layer.paint
    })
    data.paint[k] = v
    return (dispatch, getState) => {
        solutionService.updateLayerProperty(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYER_PAINT_MODIFY, layer, k, v
        })
    }
}

export function solutionLayerTypeModify(newlayer, oldlayer) {
    return (dispatch, getState) => {
        solutionService.delLayer(oldlayer.id).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: OPERATE_SOLUTION_LAYER_TYPE_MODIFY, newlayer, oldlayer
        })
    }
}

export function solutionLayerFilterModify(layer, newFilter) {
    let data = Object.assign({}, {
        id: layer.id,
        filter: newFilter
    })
    return (dispatch, getState) => {
        solutionService.updateLayerProperty(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )
        return dispatch({
            type: OPERATE_SOLUTION_LAYER_FILTER_MODIFY, layer, newFilter
        })
    }

}

export function solutionLayerZoomRangeModify(layer, minzoom, maxzoom) {
    let data = Object.assign({}, {
        id: layer.id,
        minzoom: minzoom,
        maxzoom: maxzoom
    })
    return (dispatch, getState) => {
        solutionService.updateLayerProperty(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )
        return dispatch({
            type: OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY, layer, minzoom, maxzoom
        })
    }

}

export function solutionLayerSourceAttrListAction(sourceID, sourceLayerId) {
    return {
        type: OPERATE_LAYER_SOURCE_ATTR_LIST,
        [CALL_API]: {
            url: APP_SERVICE_ROOT + 'dataSource/layer/' + sourceID + '/' + sourceLayerId,
            params: {},
            type: 'GET'
        }
    }
}

export function warehouseSourcesAction(id) {
    return {
        type: WAREHOUSE_SOURCES,
        [CALL_API]: {url: WAREHOUSE_SOURCES_URL + UserTool.getLoginUserId(), params: {id: id}, type: 'GET'}
    }
}

export function warehouseSourceLayersAction(id) {
    return {
        type: WAREHOUSE_SOURCE_LAYERS,
        id,
        [CALL_JSON]: {url: WAREHOUSE_SOURCE_LAYERS_URL + id, params: {id: id}, type: 'GET'}
    }
}

export function warehouseConfirmAction(id) {
    return {type: WAREHOUSE_SOURCES_CONFIRM, id}
}

export function warehouseSourceLayerChangeAction(layerId) {
    return {type: WAREHOUSE_SOURCE_LAYER_CHANGE, layerId}
}