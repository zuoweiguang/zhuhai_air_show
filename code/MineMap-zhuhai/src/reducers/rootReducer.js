import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import {userReducer} from './authInfo'

const rootReducer = combineReducers({
    user: userReducer,
    routing: routing
})

export default rootReducer
