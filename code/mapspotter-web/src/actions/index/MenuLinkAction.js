import {INDEX_MENU_LINK,INDEX_MENU_ROUTE,INDEX_MENU_CLICK} from '../../constants/IndexActionTypes'

export function indexMenuLinkAction(id) {
    return {type: INDEX_MENU_LINK, id}
}

export function indexMenuRouteAction(id) {
    return {type: INDEX_MENU_ROUTE, id}
}

export function indexMenuClickAction(id) {
    return (dispatch, getState) => {
        dispatch(indexMenuLinkAction(id))
        dispatch(indexMenuRouteAction(id))
    }
}