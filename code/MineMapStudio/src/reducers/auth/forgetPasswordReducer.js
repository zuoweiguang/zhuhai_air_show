import {CHANGE_PASSWORD} from '../../constants/AuthActionTypes'
const initialUserState = {
    passwordstate: true
}

export function forgetPasswordReducer(state =initialUserState, action) {
    switch (action.type) {
        case CHANGE_PASSWORD:
            let passwordNewState = Object.assign({}, state, {passwordstate: action.passwordstate})
            return passwordNewState
        default:
            return state
    }
}


