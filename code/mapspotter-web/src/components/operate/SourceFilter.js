import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'

export default class SourceFilter extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
        this.handleCombineFilterChange = this.handleCombineFilterChange.bind(this)
        this.handleSingleFilterChange = this.handleSingleFilterChange.bind(this)
        this.handleSingleFilterValueChange = this.handleSingleFilterValueChange.bind(this)
        this.handleStyleBtnClick = this.handleStyleBtnClick.bind(this)
        this.handleSingleFilterValueAddClick = this.handleSingleFilterValueAddClick.bind(this)
        this.handleSingleFilterValueDelClick = this.handleSingleFilterValueDelClick.bind(this)
        this.handleAttrFilterItemList = this.handleAttrFilterItemList.bind(this)
        this.genRenderComponent = this.genRenderComponent.bind(this)
        this.genCombineFilters = this.genCombineFilters.bind(this)
        this.state = {filters: []}
    }

    handleStyleChange(id, value) {
        if (id.startsWith('combine-filter')) {
            this.handleCombineFilterChange(id, value)
        } else if (id.startsWith('filter-attr-')) {
            this.handleSingleFilterChange(id, value)
        } else if (id.startsWith('filter-value-')) {
            this.handleSingleFilterValueChange(id, value)
        } else if (id.startsWith('filter-btn-')) {

        }
    }

    handleStyleBtnClick(id, value) {
        if (id.startsWith('filter-value-')) {
            this.handleSingleFilterValueDelClick(id, value)
        } else if (id.startsWith('filter-btn-')) {
            this.handleSingleFilterValueAddClick(id, value)
        }
    }

    genCombineFilters(filter, initCombineFilterValue) {
        let filters = []
        let combineFilterValue = initCombineFilterValue
        if (!initCombineFilterValue) {
            if (filter.length == 0) {
                combineFilterValue = 'no'
            } else {
                if (filter[0] && typeof(filter[0]) == 'string' && ['all', 'any', 'none'].indexOf(filter[0]) > -1) {
                    combineFilterValue = filter[0]
                } else {
                    combineFilterValue = 'all'
                }
            }
        }
        if (combineFilterValue == 'no') {
            filters = []
        } else if (combineFilterValue != 'no' && filter.length > 0) {
            if (filter[0] && typeof(filter[0]) == 'string' && ['all', 'any', 'none'].indexOf(filter[0]) > -1) {
                filters = [...filter]
                filters[0] = combineFilterValue
            } else {
                filters = [combineFilterValue, filter]
            }
        } else {
            filters = []
        }
        if (filters.length == 1) {
            if (filters[0] && typeof(filters[0]) == 'string' && ['all', 'any', 'none'].indexOf(filters[0]) > -1) {
                filters = []
            }
        }
        return filters
    }

    handleCombineFilterChange(id, value) {
        const layerStyle = this.props.layerStyle
        const filter = layerStyle.filter || []
        let filters = this.genCombineFilters(filter, value)
        this.props.actions.solutionLayerFilterModify(layerStyle, filters)
        if (filters.length > 0) {
            env.map.setFilter(layerStyle.id, filters)
        } else {
            env.map.setFilter(layerStyle.id, ['all'])
        }
    }

    handleSingleFilterChange(id, value) {
        const layerStyle = this.props.layerStyle
        const layerSourceAttrs = this.props.layerSourceAttrs
        const filter = layerStyle.filter || []
        let filters = this.genCombineFilters(filter)
        const idarr = id.split('-')
        const key = idarr[2]
        const oldValue = idarr[3]
        if (filters.length == 0) {
            if (value == 'has' || value == '!has') {
                filters = [value, key]
            } else {
                let v = ''
                layerSourceAttrs.map(attr=> {
                    if (attr.id == key) {
                        v = Object.values(attr.attr)[0]
                    }
                })
                filters = [value, key, v]
            }
        } else {
            filters = filters.filter(fl=> {
                if (fl instanceof Array) {
                    if (fl[1] == key) {
                        if (fl[0] == oldValue) {
                            return false
                        }
                        if (value != 'no' && fl[0] == value) {
                            return false
                        }
                        return false
                    }
                    return true
                }
                return true
            })
            if (value != 'no') {
                let newFilter = []
                if (value == 'has' || value == '!has') {
                    newFilter = [value, key]
                } else {
                    let v = ''
                    layerSourceAttrs.map(attr=> {
                        if (attr.id == key) {
                            v = Object.values(attr.attr)[0]
                        }
                    })
                    newFilter = [value, key, v]
                }
                filters = [...filters, newFilter]
            }
        }
        filters = this.genCombineFilters(filters)
        this.props.actions.solutionLayerFilterModify(layerStyle, filters)
        if (filters.length > 0) {
            env.map.setFilter(layerStyle.id, filters)
        } else {
            env.map.setFilter(layerStyle.id, ['all'])
        }
    }

    handleSingleFilterValueChange(id, value) {
        const layerStyle = this.props.layerStyle
        const layerSourceAttrs = this.props.layerSourceAttrs
        const filter = layerStyle.filter || []
        let filters = this.genCombineFilters(filter)
        const idarr = id.split('-')
        const key = idarr[2]
        const filterValue = idarr[3]
        const oldValue = idarr[4]
        if (['==', '!=', '>', '>=', '<', '<='].indexOf(filterValue) > -1) {
            filters = filters.map(fl=> {
                if (fl instanceof Array) {
                    if (fl[1] == key && fl[0] == filterValue) {
                        let isNumber = false
                        layerSourceAttrs.map(attr=> {
                            if (attr.id == key) {
                                if (typeof Object.values(attr.attr)[0] == 'number') {
                                    isNumber = true
                                }
                            }
                        })
                        if (isNumber) {
                            value = Number.parseFloat(value)
                            if (Number.isInteger(value)) {
                                value = Number.parseInt(value)
                            }
                        }
                        return [fl[0], fl[1], value]
                    }
                    return fl
                }
                return fl
            })
            filters = this.genCombineFilters(filters)
            this.props.actions.solutionLayerFilterModify(layerStyle, filters)
            if (filters.length > 0) {
                env.map.setFilter(layerStyle.id, filters)
            } else {
                env.map.setFilter(layerStyle.id, [])
            }
        } else if (['in', '!in'].indexOf(filterValue) > -1) {
            filters = filters.map(fl=> {
                if (fl instanceof Array) {
                    if (fl[1] == key && fl[0] == filterValue) {
                        let isNumber = false
                        layerSourceAttrs.map(attr=> {
                            if (attr.id == key) {
                                if (typeof Object.values(attr.attr)[0] == 'number') {
                                    isNumber = true
                                }
                            }
                        })
                        let valueList = []
                        if (value) {
                            value.map(v=> {
                                if (isNumber) {
                                    v = Number.parseFloat(v)
                                    if (Number.isInteger(v)) {
                                        v = Number.parseInt(v)
                                    }
                                }
                                valueList.push(v)
                            })
                        }
                        return [fl[0], fl[1], ...valueList]
                    }
                    return fl
                }
                return fl
            })
            filters = this.genCombineFilters(filters)
            this.props.actions.solutionLayerFilterModify(layerStyle, filters)
            if (filters.length > 0) {
                env.map.setFilter(layerStyle.id, filters)
            } else {
                env.map.setFilter(layerStyle.id, ['all'])
            }
        }
    }

    handleSingleFilterValueAddClick(id, value) {
        const layerStyle = this.props.layerStyle
        const layerSourceAttrs = this.props.layerSourceAttrs
        const filter = layerStyle.filter || []
        let filters = this.genCombineFilters(filter)
        const idarr = id.split('-')
        const key = idarr[2]
        const filterValue = idarr[3]
        if (['in', '!in'].indexOf(filterValue) > -1) {
            filters = filters.map(fl=> {
                if (fl instanceof Array) {
                    if (fl[1] == key && fl[0] == filterValue) {
                        let set = new Set()
                        fl.map((l, i)=> {
                            if (i > 1) {
                                set.add(l)
                            }
                        })
                        let isNumber = false
                        layerSourceAttrs.map(attr=> {
                            if (attr.id == key) {
                                if (typeof Object.values(attr.attr)[0] == 'number') {
                                    isNumber = true
                                }
                            }
                        })
                        if (isNumber) {
                            value = Number.parseFloat(value)
                            if (Number.isInteger(value)) {
                                value = Number.parseInt(value)
                            }
                        }
                        set.add(value)
                        return [fl[0], fl[1], ...set]
                    }
                    return fl
                }
                return fl
            })
            filters = this.genCombineFilters(filters)
            this.props.actions.solutionLayerFilterModify(layerStyle, filters)
        }
    }

    handleSingleFilterValueDelClick(id, value) {
        const layerStyle = this.props.layerStyle
        const layerSourceAttrs = this.props.layerSourceAttrs
        const filter = layerStyle.filter || []
        let filters = this.genCombineFilters(filter)
        const idarr = id.split('-')
        const key = idarr[2]
        const filterValue = idarr[3]
        if (['in', '!in'].indexOf(filterValue) > -1) {
            filters = filters.map(fl=> {
                if (fl instanceof Array) {
                    if (fl[1] == key && fl[0] == filterValue) {
                        let set = new Set()
                        fl.map((l, i)=> {
                            if (i > 1) {
                                set.add(l)
                            }
                        })
                        let isNumber = false
                        layerSourceAttrs.map(attr=> {
                            if (attr.id == key) {
                                if (typeof Object.values(attr.attr)[0] == 'number') {
                                    isNumber = true
                                }
                            }
                        })
                        if (isNumber) {
                            value = Number.parseFloat(value)
                            if (Number.isInteger(value)) {
                                value = Number.parseInt(value)
                            }
                        }
                        set.delete(value)
                        return [fl[0], fl[1], ...set]
                    }
                    return fl
                }
                return fl
            })
            filters = this.genCombineFilters(filters)
            this.props.actions.solutionLayerFilterModify(layerStyle, filters)
        }
    }

    handleAttrFilterItemList(filter) {
        const layerSourceAttrs = this.props.layerSourceAttrs
        let targetList = []
        if (!layerSourceAttrs || layerSourceAttrs.length == 0) {
            return targetList
        }
        let filters = this.genCombineFilters(filter)
        //combine-filter
        if (filters.length == 0) {
            targetList.push({id: 'combine-filter', value: "no"})
        } else {
            targetList.push({id: 'combine-filter', value: filters[0]})
        }

        //filter-attr
        layerSourceAttrs.map(attr=> {
            let attrFilters = []
            filters.map(fl=> {
                if (fl instanceof Array && fl.length >= 2) {
                    if (fl[1] == attr.id) {
                        attrFilters.push(fl)
                    }
                }
            })
            if (attrFilters.length > 0) {
                attrFilters.map(fl=> {
                    if (['has', '!has'].indexOf(fl[0]) > -1) {
                        targetList.push({
                            id: 'filter-attr-' + attr.id + '-' + fl[0],
                            key: attr.id,
                            value: fl[0],
                            name: attr.name
                        })
                    } else if (['==', '!=', '>', '>=', '<', '<='].indexOf(fl[0]) > -1) {
                        targetList.push({
                            id: 'filter-attr-' + attr.id + '-' + fl[0],
                            key: attr.id,
                            value: fl[0],
                            name: attr.name
                        })
                        targetList.push({
                            id: 'filter-value-' + attr.id + '-' + fl[0] + '-' + fl[2],
                            key: attr.id,
                            value: fl[2],
                            attr: attr.attr
                        })
                    } else if (['in', '!in'].indexOf(fl[0]) > -1) {
                        targetList.push({
                            id: 'filter-attr-' + attr.id + '-' + fl[0],
                            key: attr.id,
                            value: fl[0],
                            name: attr.name
                        })
                        let valueList = []
                        fl.map((l, i)=> {
                            if (i > 1) {
                                valueList.push(l)
                            }
                        })
                        targetList.push({
                            id: 'filter-value-' + attr.id + '-' + fl[0],
                            key: attr.id,
                            attr: attr.attr,
                            multi: 'multi',
                            value: valueList
                        })
                    }
                })
            } else {
                targetList.push({id: 'filter-attr-' + attr.id + '-no', key: attr.id, value: 'no', name: attr.name})
            }
        })

        return targetList
    }

    genRenderComponent(filterObj) {
        if (filterObj.id == 'combine-filter') {
            return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                     contentItem={{title:"组合过滤条件",type:"select",id:filterObj.id,defaultValue:filterObj.value,
                inputItems:[{title:"",value:"no"},{title:"全部包含",value:"all"},{title:"包含任意一个",value:"any"},{title:"不包含任意一个",value:"none"}]}}/>
        } else if (filterObj.id.startsWith('filter-attr-')) {
            return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                     contentItem={{title:filterObj.name,type:"select",id:filterObj.id,defaultValue:filterObj.value,
                inputItems:[{title:"",value:"no"},{title:"等于",value:"=="},{title:"不等于",value:"!="},{title:"大于",value:">"},{title:"大于等于",value:">="},{title:"小于",value:"<"},{title:"小于等于",value:"<="},{title:"包含",value:"in"},{title:"不包含",value:"!in"},{title:"存在",value:"has"},{title:"不存在",value:"!has"}]}}/>
        } else if (filterObj.id.startsWith('filter-value-')) {
            if (filterObj.btn && filterObj.attr) {
                let attrItemValueList = []
                for (let [k,v] of Object.entries(filterObj.attr)) {
                    attrItemValueList.push({title: k, value: v})
                }
                return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                         handleStyleBtnClick={this.handleStyleBtnClick}
                                         contentItem={{title:'',type:"select-btn-group",id:filterObj.id,defaultValue:filterObj.value,inputItems:attrItemValueList,disabled:'disabled',btnTitle:'删除',btnIcon:'glyphicon-remove'}}/>
            } else if (!filterObj.btn && filterObj.attr) {
                let attrItemValueList = []
                for (let [k,v] of Object.entries(filterObj.attr)) {
                    attrItemValueList.push({title: k, value: v})
                }
                let type = 'select'
                let style = {}
                if (filterObj.multi == 'multi') {
                    type = 'multiselect'
                }
                return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                         contentItem={{title:'',type:type,style:style, id:filterObj.id,defaultValue:filterObj.value,inputItems:attrItemValueList}}/>
            } else if (filterObj.btn && !filterObj.attr) {
                return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                         handleStyleBtnClick={this.handleStyleBtnClick}
                                         contentItem={{title:'',type:"input-btn-group",id:filterObj.id,disabled:'disabled',defaultValue:filterObj.value,inputItems:{},btnTitle:'删除',btnIcon:'glyphicon-remove'}}/>
            }
            return ''
        } else if (filterObj.id.startsWith('filter-btn-')) {
            let attrItemValueList = []
            for (let [k,v] of Object.entries(filterObj.attr)) {
                attrItemValueList.push({title: k, value: v})
            }
            return <StyleContentItem key={filterObj.id} handleStyleChange={this.handleStyleChange}
                                     handleStyleBtnClick={this.handleStyleBtnClick}
                                     contentItem={{title:'',type:"select-btn-group",id:filterObj.id,multiple:"multiple",defaultValue:filterObj.value,inputItems:attrItemValueList,btnTitle:'增加条目',btnIcon:'glyphicon-plus'}}/>
        }
    }

    render() {
        const {layerStyle,layerSourceAttrs,actions} = this.props
        const filter = layerStyle.filter || []
        let attrFilterItems = this.handleAttrFilterItemList(filter)
        let attrRenderItems = []
        if (attrFilterItems && attrFilterItems.length > 0) {
            attrFilterItems.map(item=> {
                attrRenderItems.push(this.genRenderComponent(item))
            })
        } else {
            attrRenderItems = <div className="content-item">
                <div className="item-header">无</div>
                <div className="item-body"></div>
            </div>
        }
        return <div className="content-body">
            {attrRenderItems}
        </div>
    }
}

SourceFilter.propTypes = {
    layerStyle: PropTypes.object.isRequired
}