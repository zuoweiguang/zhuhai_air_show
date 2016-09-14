import {MAP_INIT,MAP_LAYER_REORDER,MAP_FLY,MAP_FLY_STEP} from '../../constants/OperateActionTypes'

export function mapInitStateChangeAction(mapInit = null, mapLayerLoading = null) {
    return {type: MAP_INIT, mapInit, mapLayerLoading}
}

export function mapReorderLayers(reorder = false) {
    return {type: MAP_LAYER_REORDER, reorder}
}

export function mapFlyAction(fly = false) {
    return {type: MAP_FLY, fly}
}

export function mapFlyStepAction(flyStep = 0) {
    return {type: MAP_FLY_STEP, flyStep}
}