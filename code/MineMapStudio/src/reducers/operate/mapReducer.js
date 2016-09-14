import {MAP_INIT,MAP_LAYER_REORDER,MAP_FLY,MAP_FLY_STEP,MAP_CONTROLL_CITYINFO_LIST,MAP_CONTROLL_CITYPANEL_VISIBLE} from '../../constants/OperateActionTypes'
import { combineReducers } from 'redux'

let initState = {
    mapInit: false,
    mapLayerLoading: false,
    reorder: false,
    fly: false,
    flyStep: 0
}
function mapInitReduce(state = initState, action) {
    switch (action.type) {
        case MAP_INIT:
            let newState = {}
            if (action.mapInit != null) {
                newState.mapInit = action.mapInit
            }
            if (action.mapLayerLoading != null) {
                newState.mapLayerLoading = action.mapLayerLoading
            }
            return Object.assign({}, state, newState)
        case MAP_LAYER_REORDER:
            return Object.assign({}, state, {reorder: action.reorder})
        case MAP_FLY:
            return Object.assign({}, state, {fly: action.fly})
        case MAP_FLY_STEP:
            return Object.assign({}, state, {flyStep: action.flyStep})
        default:
            return state;
    }
}

let controllState = {
    cityInfoList: {},
    cityPanelVisible: false
}
function mapControllReduce(state = controllState, action) {
    switch (action.type) {
        case MAP_CONTROLL_CITYINFO_LIST:
            return Object.assign({}, state, {cityInfoList: action.ready ? action.fetchData : {}})
        case MAP_CONTROLL_CITYPANEL_VISIBLE:
            return Object.assign({}, state, {cityPanelVisible: action.visible})
        default:
            return state;
    }
}

const mapReducer = combineReducers({
    init: mapInitReduce,
    controll: mapControllReduce
})

export default mapReducer