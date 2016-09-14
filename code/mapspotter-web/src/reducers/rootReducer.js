import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import indexMenuReducer from './index/indexMenuLink'
import userReducer from './auth/authInfo'
import studioMenusReducer from './studio/studioMenuReducer'
import solutionsReducer from './studio/solutionsReducer'
import templatesReducer from './studio/templatesReducer'
import operateReducer from './operate/operateReducer'
import mapReducer from './operate/mapReducer'

const rootReducer = combineReducers({
    indexMenu: indexMenuReducer,
    user: userReducer,
    studioMenus:studioMenusReducer,
    solutions: solutionsReducer,
    templates: templatesReducer,
    operate: operateReducer,
    routing: routing,
    map: mapReducer
})

export default rootReducer
