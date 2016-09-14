import createFetch from '../../utils/fetch'
import UserTool from '../../utils/UserTool'
import {APP_SERVICE_ROOT} from '../../config/appConfig'

const authService = {
    addUser: (data) => createFetch(APP_SERVICE_ROOT + "user/add", "POST", data),
    updateUser: (data) => createFetch(APP_SERVICE_ROOT + "user/update", "POST", data),
    delUser: (id) => createFetch(APP_SERVICE_ROOT + "user/delete", "POST", {id: id}),
    userLogin: (data) => createFetch(APP_SERVICE_ROOT + "user/login", "POST", data),
}

export default authService