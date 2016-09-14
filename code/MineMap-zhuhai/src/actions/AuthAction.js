import {USER_LOAD,USER_LOGIN,USER_LOGOUT,USER_REGISTER,USER_CHECK_USERNAME,LOGIN_PANEL_SHOW,REGISTER_BODY_SHOW,CHANGE_PASSWORD} from '../constants/AuthActionTypes'
import CookieTool from '../utils/CookieTool'
import Base64Util from '../utils/Base64Util'
export function userLoadAction() {
    return {type: USER_LOAD}
}

export function userLoginAction(user) {
    return (dispatch, getState) => {
        CookieTool.addCookie("id",user.id,24)
        CookieTool.addCookie("username",user.username,24)
        CookieTool.addCookie("password",Base64Util.encode(user.password),24)
        CookieTool.addCookie("email",user.email,24)
        CookieTool.addCookie("token",Base64Util.encode(user.token),24)
        CookieTool.addCookie("autolchevalue",user.autolchevalue,24)

        return dispatch(
            {type: USER_LOGIN, user}
        )

    }
}

export function userLogoutAction() {
    return (dispatch, getState) => {

            CookieTool.deleteCookie("id")
            CookieTool.deleteCookie("username")
            CookieTool.deleteCookie("password")
            CookieTool.deleteCookie("email")
            CookieTool.deleteCookie("token")

        let user = {
            id: CookieTool.getCookie("id"),
            username: CookieTool.getCookie("username"),
            password: CookieTool.getCookie(Base64Util.decode("password")),
            email: CookieTool.getCookie("email"),
            token: CookieTool.getCookie(Base64Util.decode("token")),
            autolchevalue:CookieTool.getCookie("autolchevalue")
        }
        return dispatch(
            {type: USER_LOGOUT, user}
        )
    }
}

export function userRegisterAction(id) {
    return {type: USER_REGISTER, id}
}



export function loginModelShowAction(loginModalIsOpexn) {
    return {type: LOGIN_PANEL_SHOW, loginModalIsOpexn}
}

export function RegisterBodyShowAction(registerBodyShow) {
    return {type: REGISTER_BODY_SHOW, registerBodyShow}
}

export function ChangePasswordAction(passwordstate) {
    return {type: CHANGE_PASSWORD, passwordstate}
}

