import {LOGIN_PANEL_SHOW} from '../../constants/AuthActionTypes'
const initialUserState = {
    loginModalIsOpexn: false,
}

export function loginReducer(state = {
    loginModalIsOpexn: false
}, action) {
    switch (action.type) {
        case LOGIN_PANEL_SHOW:
            let loginNewState = Object.assign({}, state, {loginModalIsOpexn: action.loginModalIsOpexn})
            return loginNewState
        default:
            return state
    }
}