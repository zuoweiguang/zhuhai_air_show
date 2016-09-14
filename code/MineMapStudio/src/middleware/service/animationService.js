import createFetch from '../../utils/fetch'
import {APP_SERVICE_ROOT} from '../../config/appConfig'

const animationService = {
    getRoadSpirit: (boundParam) => createFetch("http://minemap.navinfo.com/service129/roadspirit/SpiritService", "GET", Object.assign({},boundParam,{query:'linkByNode'})),
    getRoadLinkSpirit: (boundParam) => createFetch("http://minemap.navinfo.com/service129/roadspirit/SpiritService", "GET",Object.assign({},boundParam,{query:'linkByLink'})),
    getRoadLinkBrand: (boundParam) => createFetch("http://minemap.navinfo.com/service129/roadspirit/SpiritService", "GET", Object.assign({},boundParam,{query:'warning'})),
}

export default animationService