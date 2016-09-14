import {STUDIO_MENU_LINK} from '../../constants/StudioActionTypes'
import {APP_ROOT_NAME} from '../../config/appConfig'
import { combineReducers } from 'redux'

const initialLinksState = [
    {
        name: '方案',
        selected: 'selected',
        href: APP_ROOT_NAME + "studio",
        id: 'solution'
    },
    {
        name: '帮助',
        selected: '',
        href: APP_ROOT_NAME + "studio/help",
        id: 'help'
    },
]

function studioMenusReducer(state = initialLinksState, action) {
    switch (action.type) {
        case STUDIO_MENU_LINK:
            return state.map(link =>
                link.id === action.id ?
                    Object.assign({}, link, {selected: 'selected'}) :
                    Object.assign({}, link, {selected: ''})
            )

        default:
            return state
    }
}

export default studioMenusReducer