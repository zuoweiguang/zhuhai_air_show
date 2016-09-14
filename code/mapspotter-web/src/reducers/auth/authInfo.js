import {USER_LOAD,USER_LOGIN,USER_LOGOUT,USER_REGISTER} from '../../constants/AuthActionTypes'

const initialUserState = {
    id: localStorage.userid,
    username: localStorage.username,
    token: localStorage.token
}

function userReducer(state = initialUserState, action) {
    switch (action.type) {
        case USER_LOAD:
            return initialUserState
        case USER_LOGIN:
            return action.user
        case USER_LOGOUT:
            return initialUserState
        case USER_REGISTER:
            return initialUserState

        default:
            return state
    }
}

export default userReducer