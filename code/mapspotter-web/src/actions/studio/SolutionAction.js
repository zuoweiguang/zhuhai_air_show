import {STUDIO_SOLUTION_LIST,STUDIO_SOLUTION_ADD,STUDIO_SOLUTION_DEL,STUDIO_SOLUTION_EDIT,STUDIO_SOLUTION_MODIFY,STUDIO_SOLUTION_COPY,STUDIO_TEMPLATE_LIST,STUDIO_TEMPLATE_SELECT} from '../../constants/StudioActionTypes'
import {DISPLAY_INTERFACE_ROUTE} from '../../constants/OperateActionTypes'
import { CALL_API,CALL_JSON } from '../../constants/MiddlewareInfo'
import {STUDIO_SOLUTION_LIST_URL} from '../../config/connectConfig'
import solutionService from '../../middleware/service/solutionService'

export function solutionListAction(userId) {
    return {
        type: STUDIO_SOLUTION_LIST,
        [CALL_API]: {url: STUDIO_SOLUTION_LIST_URL + userId, params: {}, type: 'GET'}
    }
}

export function solutionAddAction(data) {
    return {type: STUDIO_SOLUTION_ADD, data}
}

export function solutionDelAction(id) {
    return (dispatch, getState) => {
        solutionService.delSolution(id).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: STUDIO_SOLUTION_DEL, id
        })
    }
}

export function solutionEditAction(id) {
    return {type: STUDIO_SOLUTION_EDIT, id}
}

export function solutionModifyAction(data) {
    return (dispatch, getState) => {
        solutionService.updateSolution(data).then(response =>
            response.json()
        ).then(res => {
            console.log(res)
        }).catch((e)=>
            console.log(e)
        )

        return dispatch({
            type: STUDIO_SOLUTION_MODIFY, data
        })
    }
}

export function solutionCopyAction(srcSolution, copySolution) {
    return (dispatch, getState) => {
        solutionService.getSolution(srcSolution.id).then(response =>
            response.json()
        ).then(res => {
            if (res.data && res.data.solution) {
                let copySoluID = copySolution.id
                const srcLayers = res.data.solution.layers
                srcLayers.map(layer=> {
                    let copyLayer = Object.assign({}, layer, {soluID: copySoluID})
                    delete copyLayer.id
                    solutionService.addLayer(copyLayer).then(rp =>
                        rp.json()
                    ).then(rs => {
                        console.log(rs)
                    }).catch((e)=>
                        console.log(e)
                    )
                })
                return dispatch({
                    type: STUDIO_SOLUTION_COPY, copySolution
                })
            }
        }).catch((e)=>
            console.log(e)
        )
    }
}

export function solutionTemplateListAction() {
    return {
        type: STUDIO_TEMPLATE_LIST
    }
}

export function solutionTemplateSelectAction(id) {
    return {type: STUDIO_TEMPLATE_SELECT, id}
}

export function displayInterfaceRouteAction(route) {
    return {type: DISPLAY_INTERFACE_ROUTE, route}
}
