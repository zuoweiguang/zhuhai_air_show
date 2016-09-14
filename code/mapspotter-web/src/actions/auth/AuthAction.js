import {USER_LOAD,USER_LOGIN,USER_LOGOUT,USER_REGISTER} from '../../constants/AuthActionTypes'

export function userLoadAction() {
    return {type: USER_LOAD}
}

export function userLoginAction(user) {
    return (dispatch, getState) => {
        localStorage.userid = user.id || ""
        localStorage.username = user.username || ""
        localStorage.token = user.username || ""

        return dispatch(
            {type: USER_LOGIN, user}
        )
    }
}

export function userLogoutAction() {
    return (dispatch, getState) => {
        localStorage.userid = ""
        localStorage.username = ""
        localStorage.token = ""

        return dispatch(
            {type: USER_LOGOUT}
        )
    }
}

export function userRegisterAction(id) {
    return {type: USER_REGISTER, id}
}