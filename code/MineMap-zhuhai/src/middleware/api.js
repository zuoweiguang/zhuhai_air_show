import {CALL_API,CALL_API_ATTR,CALL_JSON} from '../constants/MiddlewareInfo'
import 'isomorphic-fetch'

function callApi(access) {

    let newAccess = Object.assign({}, CALL_API_ATTR, access);

    let fetchParam = {
        method : newAccess.type
    }
    if (newAccess.params && newAccess.type !== "GET") {
        fetchParam.body = JSON.stringify(newAccess.params)
    }
    return fetch(newAccess.url, fetchParam).then(response =>
        response.json().then(json => ({json, response}))
    ).then(({ json, response }) => {
        if (!response.ok || json.errcode !== 0) {
            return Promise.reject(json)
        }
        return json
    })
}

function callJson(access) {

    let newAccess = Object.assign({}, CALL_API_ATTR, access);

    return fetch(newAccess.url).then(response =>
        response.json().then(json => ({json, response}))
    ).then(({ json, response }) => {
        if (!response.ok) {
            return Promise.reject(json)
        }
        return json
    })
}

export default store => next => action => {
    const callAPI = action[CALL_API]
    const callJSON = action[CALL_JSON]

    if (!callAPI && !callJSON) {
        return next(action)
    }

    function makeAction(ready, data) {
        let newAction = Object.assign({}, action, {ready}, data)
        return newAction
    }

    next(makeAction(false))

    if (callAPI) {
        return callApi(callAPI).then(json => {
            next(makeAction(true, {fetchData: json.data}))
        }).catch((e)=>
            console.log(e)
        )
    } else if (callJSON) {
        return callJson(callJSON).then(json => {
            next(makeAction(true, {fetchData: json.data}))
        }).catch((e)=>
            console.log(e)
        )
    }
}