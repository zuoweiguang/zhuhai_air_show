import { createStore, applyMiddleware,compose } from 'redux'
import thunkMiddleware from 'redux-thunk'
import createLogger from 'redux-logger'
import { routerReducer, routerMiddleware } from 'react-router-redux'
import rootReducer from '../reducers/rootReducer'
import DevTools from '../containers/index/DevTools'
import api from '../middleware/api'

export default function rootStore(history,preloadedState) {
    if (process.env.NODE_ENV === 'production') {
        return createStore(
            rootReducer,
            preloadedState,
            applyMiddleware(routerMiddleware(history),thunkMiddleware,api)
        )
    } else {
        const store = createStore(
            rootReducer,
            preloadedState,
            compose(
                applyMiddleware(routerMiddleware(history),thunkMiddleware, api,createLogger()),
                DevTools.instrument()
            )
        )

        if (module.hot) {
            // Enable Webpack hot module replacement for reducers
            module.hot.accept('../reducers/rootReducer', () => {
                const nextReducer = require('../reducers/rootReducer').default
                store.replaceReducer(nextReducer)
            })
        }

        return store
    }
}
