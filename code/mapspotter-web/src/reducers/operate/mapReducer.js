import {MAP_INIT,MAP_LAYER_REORDER,MAP_FLY,MAP_FLY_STEP} from '../../constants/OperateActionTypes'
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

const mapReducer = combineReducers({
    init: mapInitReduce
})

export default mapReducer