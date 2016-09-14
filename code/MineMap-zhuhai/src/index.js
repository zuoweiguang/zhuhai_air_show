import 'babel-polyfill'
import React from 'react'
import { render } from 'react-dom'
import  {Provider} from 'react-redux'
import { syncHistoryWithStore } from 'react-router-redux'
import { Router,IndexRoute, Route, Redirect,browserHistory } from 'react-router'
import rootStore from './store/rootStore'
import AuthUtil from './utils/AuthUtil'
import App from './containers/App'
import DevTools from './containers/index/DevTools'
import HeaderContainer from './containers/HeaderContainer'
import IndexBodyContainer from './containers/index/IndexBodyContainer'
import {APP_ROOT_NAME} from './config/appConfig'

const store = rootStore()
const history = syncHistoryWithStore(browserHistory, store)

let devTool = ""
if (process.env.NODE_ENV === 'production') {
    devTool = ""
} else {
    devTool = <DevTools/>
}


render((
        <Provider store={store}>
            <div>
                <Router history={history}>
                    <Redirect from={APP_ROOT_NAME} to={APP_ROOT_NAME+"index"}/>
                    <Route path={APP_ROOT_NAME} component={App}>
                        <Route path="index" components={{headerView:HeaderContainer,bodyView:IndexBodyContainer}}
                               />


                    </Route>
                </Router>
                {devTool}
            </div>
        </Provider>
    ),
    document.getElementById('root')
)

