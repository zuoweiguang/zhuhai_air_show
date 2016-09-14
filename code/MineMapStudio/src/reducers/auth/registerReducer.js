import {REGISTER_BODY_SHOW} from '../../constants/AuthActionTypes'
const initialUserState = {
    registerBodyShow:true
}

export function registerReducer(state = initialUserState, action) {
    switch (action.type) {
        case REGISTER_BODY_SHOW:
            let registerBodyNewState = Object.assign({}, state, {registerBodyShow: action.registerBodyShow})
            return registerBodyNewState
        default:
            return state
    }
}

