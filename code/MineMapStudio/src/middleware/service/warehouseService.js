import createFetch from '../../utils/fetch'
import UserTool from '../../utils/UserTool'
import {APP_ROOT_NAME,APP_SERVICE_ROOT} from '../../config/appConfig'

const warehouseService = {
    getUserSourceList: () => createFetch(APP_SERVICE_ROOT + "dataSource/list/" + UserTool.getLoginUserId(), "GET", {}),
    getSourceInfo: (id) => createFetch(APP_SERVICE_ROOT + "dataSource/" + id, "GET", {}),
    getDefaultStyleInfo: (type) => createFetch(APP_ROOT_NAME + "app/data/studio/style-" + type + ".json", "GET", {}),
}

export default warehouseService