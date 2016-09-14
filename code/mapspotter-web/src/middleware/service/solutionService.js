import createFetch from '../../utils/fetch'
import UserTool from '../../utils/UserTool'
import {APP_SERVICE_ROOT} from '../../config/appConfig'

const solutionService = {
    getSolution: (id) => createFetch(APP_SERVICE_ROOT + "solution/" + id, "GET", {}),
    addSolution: (data) => createFetch(APP_SERVICE_ROOT + "solution/add", "POST", data),
    updateSolution: (data) => createFetch(APP_SERVICE_ROOT + "solution/update", "POST", data),
    delSolution: (id) => createFetch(APP_SERVICE_ROOT + "solution/delete", "POST", {id: id}),
    addLayer: (data) => createFetch(APP_SERVICE_ROOT + "layer/add", "POST", data),
    updateLayer: (data) => createFetch(APP_SERVICE_ROOT + "layer/update", "POST", data),
    delLayer: (id) => createFetch(APP_SERVICE_ROOT + "layer/delete", "POST", {id: id}),
    updateLayerProperty: (data) => createFetch(APP_SERVICE_ROOT + "layer/update", "POST", data),
    //addLayer: (data) => createFetch("http://192.168.50.110:8080/layer/add", "POST", data),
    getStyleInfo: (id) => createFetch(APP_SERVICE_ROOT + "style/" + id, "GET", {}),
    addStyle: (data) => createFetch(APP_SERVICE_ROOT + "style/add", "POST", data),
}

export default solutionService