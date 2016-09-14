import {MAP_INIT,MAP_LAYER_REORDER,MAP_FLY,MAP_FLY_STEP,MAP_CONTROLL_CITYINFO_LIST,MAP_CONTROLL_CITYPANEL_VISIBLE} from '../../constants/OperateActionTypes'
import {MAP_CONTROLL_CITYINFO_LIST_URL} from '../../config/connectConfig'
import {CALL_JSON } from '../../constants/MiddlewareInfo'

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

export function mapControllCityInfoListAction() {
    return {
        type: MAP_CONTROLL_CITYINFO_LIST,
        [CALL_JSON]: {url: MAP_CONTROLL_CITYINFO_LIST_URL}
    }
}

export function mapControllCityPanelVisibleAction(visible = false) {
    return {type: MAP_CONTROLL_CITYPANEL_VISIBLE,visible}
}