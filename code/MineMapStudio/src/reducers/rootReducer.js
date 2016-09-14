import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import indexMenuReducer from './index/indexMenuLink'
import {userReducer} from './auth/authInfo'
import {loginReducer} from './auth/loginReducer'
import {registerReducer} from './auth/registerReducer'
import {forgetPasswordReducer} from './auth/forgetPasswordReducer'
import studioMenusReducer from './studio/studioMenuReducer'
import solutionsReducer from './studio/solutionsReducer'
import templatesReducer from './studio/templatesReducer'
import operateReducer from './operate/operateReducer'
import mapReducer from './operate/mapReducer'

const rootReducer = combineReducers({
    indexMenu: indexMenuReducer,
    user: userReducer,
    login:loginReducer,
    register:registerReducer,
    forgetpw:forgetPasswordReducer,
    studioMenus:studioMenusReducer,
    solutions: solutionsReducer,
    templates: templatesReducer,
    operate: operateReducer,
    routing: routing,
    map: mapReducer
})

export default rootReducer
