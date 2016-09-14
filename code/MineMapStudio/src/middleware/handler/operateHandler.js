import env from '../../core/env'
import * as OperateActionTypes from '../../constants/OperateActionTypes'

const operateHandler = {
    /**新增一条操作记录**/
    addOperateRecord: (actionType, param = {}) => {
        let paramObj = Object.assign({}, {
            oldValue: param.oldValue,
            newValue: param.newValue,
            layer: param.layer || {},
            solution: param.solution || {},
            key: param.key,
            oldMinzoom: param.oldMinzoom,
            oldMaxzoom:param.oldMaxzoom,
            newMinzoom: param.newMinzoom,
            newMaxzoom:param.newMaxzoom,
        })
        let record = Object.assign({}, {
            actionType: actionType,
            param: paramObj
        })
        env.operateRecords.push(record)
    },

    /**撤销最后一条操作记录**/
    cancelOperateRecord: (actions) => {
        if (!env.operateRecords || env.operateRecords.length == 0) {
            return
        }
        let record = env.operateRecords[env.operateRecords.length - 1]
        switch (record.actionType) {
            case OperateActionTypes.OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY:
                if (env.map) {
                    env.map.setLayoutProperty(record.param.layer.id, record.param.key, record.param.oldValue)
                    actions.solutionLayerLayoutModify(record.param.layer, record.param.key, record.param.oldValue)
                }
                break
            case OperateActionTypes.OPERATE_SOLUTION_LAYER_PAINT_MODIFY:
                if (env.map) {
                    env.map.setPaintProperty(record.param.layer.id, record.param.key, record.param.oldValue)
                    actions.solutionLayerPaintModify(record.param.layer, record.param.key, record.param.oldValue)
                }
                break
            case OperateActionTypes.OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY:
                if (env.map) {
                    env.map.setLayerZoomRange(record.param.layer.id, record.param.oldMinzoom, record.param.oldMaxzoom)
                    actions.solutionLayerZoomRangeModify(record.param.layer, record.param.oldMinzoom, record.param.oldMaxzoom)
                }
                break
            case OperateActionTypes.OPERATE_SOLUTION_LAYER_FILTER_MODIFY:
                if (env.map) {
                    if (record.param.oldValue.length > 0) {
                        env.map.setFilter(record.param.layer.id, record.param.oldValue)
                    } else {
                        env.map.setFilter(record.param.layer.id, ['all'])
                    }
                    actions.solutionLayerFilterModify(record.param.layer, record.param.oldValue)
                }
                break
            default:
                break
        }
        env.operateRecords.pop()
    },
}

export default operateHandler