import {INDEX_MENU_LINK,INDEX_MENU_ROUTE} from '../../constants/IndexActionTypes'
import { combineReducers } from 'redux'
import {APP_ROOT_NAME} from '../../config/appConfig'

const initialLinksState = [
    {
        name: '首页',
        selected: 'selected',
        href: APP_ROOT_NAME,
        id: 'index'
    },
    {
        name: '数据产品',
        selected: '',
        href: APP_ROOT_NAME + 'product',
        id: 'product'
    },
    {
        name: '解决方案',
        selected: '',
        href: APP_ROOT_NAME + 'solution',
        id: 'solution'
    },
    {
        name: '工作台',
        selected: '',
        href: APP_ROOT_NAME + 'studio',
        id: 'studio'
    },
    {
        name: '联系我们',
        selected: '',
        href: APP_ROOT_NAME + 'contact',
        id: 'contact'
    },
]

function indexMenuLinks(state = initialLinksState, action) {
    switch (action.type) {
        case INDEX_MENU_LINK:
            return state.map(link =>
                link.id === action.id ?
                    Object.assign({}, link, {selected: 'selected'}) :
                    Object.assign({}, link, {selected: ''})
            )

        default:
            return state
    }
}

function indexMenuRoute(state = 'index', action) {
    switch (action.type) {
        case INDEX_MENU_ROUTE:
            return action.id

        default:
            return state
    }
}

const indexMenuReducer = combineReducers({
    indexMenuLinks: indexMenuLinks,
    indexMenuRoute: indexMenuRoute
})

export default indexMenuReducer