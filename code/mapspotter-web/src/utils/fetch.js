import fetch from 'isomorphic-fetch'

function jsonToUrl(paramObj) {
    let queryStr = ""
    if (paramObj) {
        Object.keys(paramObj).map((key)=> {
            let value = paramObj[key]
            if(typeof(value) != 'string'){
                value = JSON.stringify(value)
            }
            queryStr += `${key}=${value}&`
        })
    }
    if (queryStr.endsWith("&")) {
        queryStr = queryStr.substr(0, queryStr.length - 1)
    }

    return queryStr
}

function createFetch(url, method, paramObj) {
    const baseUrl = `${url}`

    if (method === "GET") {
        const queryStr = jsonToUrl(paramObj)
        let fullUrl = baseUrl
        if (queryStr && queryStr.length > 0) {
            fullUrl = `${baseUrl}?${queryStr}`
        }
        return fetch(fullUrl, {method: "GET"})
    } else if (method === "POST") {
        return fetch(baseUrl, {
            method: "POST",
            body: JSON.stringify(paramObj)
        })
    }

    throw new Error(`not support method ${method}`)
}

export default createFetch
