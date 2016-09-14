import {STUDIO_TEMPLATE_LIST,STUDIO_TEMPLATE_SELECT} from '../../constants/StudioActionTypes'
import { combineReducers } from 'redux'

const templateList = [{
    id: "bright",
    desc: "显示包含行政区划、道路的2D/3D市街图",
    name: "城市地图",
    icon: "app/images/studio/solution/1.png",
    selected: true
}, {
    id: "basic",
    desc: "对用户出行轨迹、自有道路数据的算法分析，实现缺失道路的数据挖掘",
    name: "地理挖掘",
    icon: "app/images/studio/solution/2.png",
    selected: false
}, {
    id: "street",
    desc: "轨迹数据，配合路网的基础图层，可对用户的轨迹数据进行查看、编辑",
    name: "轨迹地图",
    icon: "app/images/studio/solution/3.png",
    selected: false
}, {
    id: "satellite",
    desc: "实时查看最新资三影像资料，验证缺失道路区域的覆盖情况",
    name: "资三地图",
    icon: "app/images/studio/solution/4.png",
    selected: false
}, {
    id: "dark",
    desc: "实时查看最新卫星影像资料，验证缺失道路区域的覆盖情况",
    name: "影像地图",
    icon: "app/images/studio/solution/5.png",
    selected: false
}]

function templatesReducer(state = templateList, action) {
    switch (action.type) {
        case STUDIO_TEMPLATE_LIST:
            return templateList

        case STUDIO_TEMPLATE_SELECT:
            return state.map(template =>
                template.id === action.id ?
                    Object.assign({}, template, {selected: true}) :
                    Object.assign({}, template, {selected: false})
            )

        default:
            return state
    }
}

export default templatesReducer