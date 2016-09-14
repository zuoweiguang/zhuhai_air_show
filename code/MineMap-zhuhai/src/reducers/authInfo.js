import {USER_LOAD,USER_LOGIN,USER_LOGOUT,USER_REGISTER} from '../constants/AuthActionTypes'
import CookieTool from '../utils/CookieTool'
const initialUserState = {
    id: CookieTool.getCookie("id"),
    username: CookieTool.getCookie("username"),
    password: CookieTool.getCookie("password"),
    email: CookieTool.getCookie("email"),
    token: CookieTool.getCookie("token"),
    autolchevalue:CookieTool.getCookie("autolchevalue")

}

export function userReducer(state = initialUserState, action) {
    switch (action.type) {
        case USER_LOAD:
            return initialUserState
        case USER_LOGIN:
            return action.user
        case USER_LOGOUT:
            return action.user
        case USER_REGISTER:
            return initialUserState
        default:
            return state
    }
}

